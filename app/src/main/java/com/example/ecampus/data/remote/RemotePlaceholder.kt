package com.example.ecampus.data.remote

import android.content.ContentResolver
import android.net.Uri
import com.example.ecampus.BuildConfig
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import okio.BufferedSink
import okio.source
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class SupabaseItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("price") val price: Double,
    @SerializedName("cover_url") val coverUrl: String?,
    @SerializedName("campus") val campus: String,
    @SerializedName("owner_id") val ownerId: String?,
    @SerializedName("owner_name") val ownerName: String?,
    @SerializedName("created_at") val createdAt: String
)

data class SupabaseItemDetailDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("price") val price: Double,
    @SerializedName("category") val category: String,
    @SerializedName("campus") val campus: String,
    @SerializedName("description") val description: String,
    @SerializedName("image_urls") val imageUrls: List<String>?,
    @SerializedName("contact") val contact: String,
    @SerializedName("owner_id") val ownerId: String?,
    @SerializedName("owner_name") val ownerName: String?,
    @SerializedName("created_at") val createdAt: String
)

data class SupabaseInsertItemRequest(
    @SerializedName("title") val title: String,
    @SerializedName("price") val price: Double,
    @SerializedName("category") val category: String,
    @SerializedName("campus") val campus: String,
    @SerializedName("description") val description: String,
    @SerializedName("contact") val contact: String,
    @SerializedName("owner_id") val ownerId: String,
    @SerializedName("owner_name") val ownerName: String,
    @SerializedName("image_urls") val imageUrls: List<String>,
    @SerializedName("cover_url") val coverUrl: String
)

internal fun SupabaseItemDto.createdAtEpochMillis(): Long {
    return runCatching {
        parseIso8601ToEpochMillis(createdAt)
    }.getOrDefault(0L)
}

internal fun SupabaseItemDetailDto.createdAtEpochMillisDetail(): Long {
    return runCatching {
        parseIso8601ToEpochMillis(createdAt)
    }.getOrDefault(0L)
}

private fun parseIso8601ToEpochMillis(value: String): Long {
    val v = value.trim()
    if (v.isBlank()) return 0L

    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )

    for (p in patterns) {
        try {
            val sdf = SimpleDateFormat(p, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = sdf.parse(v)
            if (date != null) return date.time
        } catch (_: Throwable) {
        }
    }
    return 0L
}

interface SupabaseApi {
    @GET("rest/v1/items")
    suspend fun getItems(
        @Query("select") select: String = "id,title,price,cover_url,campus,owner_id,owner_name,created_at",
        @Query("order") order: String = "created_at.desc"
    ): List<SupabaseItemDto>

    @GET("rest/v1/items")
    suspend fun getMyItems(
        @Query("owner_id") ownerId: String,
        @Query("select") select: String = "id,title,price,cover_url,campus,owner_id,owner_name,created_at",
        @Query("order") order: String = "created_at.desc"
    ): List<SupabaseItemDto>

    @GET("rest/v1/items")
    suspend fun getItemDetail(
        @Query("id") itemId: String,
        @Query("select") select: String = "id,title,price,category,campus,description,image_urls,contact,owner_id,owner_name,created_at",
        @Query("limit") limit: Int = 1
    ): List<SupabaseItemDetailDto>

    @Headers(
        "Content-Type: application/json",
        "Prefer: return=representation"
    )
    @POST("rest/v1/items")
    suspend fun insertItem(
        @Body body: SupabaseInsertItemRequest
    ): List<SupabaseItemDto>

    @PUT("storage/v1/object/{bucket}/{path}")
    suspend fun uploadObject(
        @Path("bucket") bucket: String,
        @Path(value = "path", encoded = true) path: String,
        @Header("Content-Type") contentType: String,
        @Header("x-upsert") upsert: String = "true",
        @Body body: RequestBody
    )
}

internal object SupabaseRemote {
    const val DEFAULT_BUCKET = "item-images"

    fun createApi(): SupabaseApi {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(createAuthInterceptor())
            .addInterceptor(logging)
            .build()

        val baseUrl = normalizeBaseUrl(BuildConfig.SUPABASE_URL)

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseApi::class.java)
    }

    fun publicObjectUrl(bucket: String, path: String): String {
        val baseUrl = normalizeBaseUrl(BuildConfig.SUPABASE_URL).removeSuffix("/")
        return "$baseUrl/storage/v1/object/public/$bucket/$path"
    }

    fun streamingRequestBody(
        contentResolver: ContentResolver,
        uri: Uri,
        contentType: String
    ): RequestBody {
        return object : RequestBody() {
            override fun contentType() = contentType.toMediaTypeOrNull()

            override fun writeTo(sink: BufferedSink) {
                contentResolver.openInputStream(uri)?.use { input ->
                    sink.writeAll(input.source())
                } ?: throw IllegalStateException("无法打开图片流")
            }
        }
    }

    private fun createAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val key = BuildConfig.SUPABASE_ANON_KEY
            val request = chain.request().newBuilder()
                .header("apikey", key)
                .header("Authorization", "Bearer $key")
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
    }

    private fun normalizeBaseUrl(url: String): String {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return "https://example.invalid/"

        val lower = trimmed.lowercase()
        val isHttp = lower.startsWith("http://") || lower.startsWith("https://")
        if (!isHttp) return "https://example.invalid/"

        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }
}
