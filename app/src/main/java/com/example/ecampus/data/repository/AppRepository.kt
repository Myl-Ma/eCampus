package com.example.ecampus.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.ecampus.data.local.db.dao.CachedItemDao
import com.example.ecampus.data.local.db.dao.FavoriteDao
import com.example.ecampus.data.local.db.entity.CachedItemEntity
import com.example.ecampus.data.local.db.entity.FavoriteEntity
import com.example.ecampus.data.model.Item
import com.example.ecampus.data.model.ItemCard
import com.example.ecampus.data.remote.SupabaseApi
import com.example.ecampus.data.remote.SupabaseInsertItemRequest
import com.example.ecampus.data.remote.SupabaseRemote
import com.example.ecampus.data.remote.createdAtEpochMillis
import com.example.ecampus.data.remote.createdAtEpochMillisDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

data class HomeItemsResult(
    val items: List<ItemCard>,
    val isCache: Boolean
)

class AppRepository(
    private val supabaseApi: SupabaseApi,
    private val fakeRepository: FakeRepository,
    private val favoriteDao: FavoriteDao,
    private val cachedItemDao: CachedItemDao
) {
    fun observeFavoriteIds(): Flow<List<String>> = favoriteDao.observeFavoriteIds()

    fun observeIsFavorite(itemId: String): Flow<Boolean> = favoriteDao.observeIsFavorite(itemId)

    suspend fun toggleFavorite(itemId: String) {
        withContext(Dispatchers.IO) {
            val isFav = favoriteDao.isFavorite(itemId)
            if (isFav) {
                favoriteDao.delete(itemId)
            } else {
                favoriteDao.insert(FavoriteEntity(itemId = itemId, createdAt = System.currentTimeMillis()))
            }
        }
    }

    suspend fun setFavorite(itemId: String, isFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            if (isFavorite) {
                favoriteDao.insert(FavoriteEntity(itemId = itemId, createdAt = System.currentTimeMillis()))
            } else {
                favoriteDao.delete(itemId)
            }
        }
    }

    fun observeHomeItemsWithSource(): Flow<HomeItemsResult> = flow {
        val isCache = try {
            syncHomeFromRemote()
            false
        } catch (_: Throwable) {
            ensureHomeCache()
            true
        }

        emitAll(
            cachedItemDao.observeAll().map { list ->
                HomeItemsResult(
                    items = list.map { it.toItemCard() },
                    isCache = isCache
                )
            }
        )
    }

    fun observeHomeItems(): Flow<List<ItemCard>> = observeHomeItemsWithSource().map { it.items }

    fun observeFavoriteItems(): Flow<List<ItemCard>> = combine(
        favoriteDao.observeFavorites(),
        cachedItemDao.observeAll()
    ) { favorites, cached ->
        val cacheMap = cached.associateBy { it.itemId }
        favorites.mapNotNull { fav ->
            cacheMap[fav.itemId]?.toItemCard()
        }
    }

    suspend fun getItemDetail(itemId: String): Item? = withContext(Dispatchers.IO) {
        val remote = runCatching {
            supabaseApi.getItemDetail(itemId = "eq.$itemId")
        }.getOrNull()?.firstOrNull()

        if (remote != null) {
            return@withContext Item(
                id = remote.id,
                title = remote.title,
                price = remote.price,
                category = remote.category,
                campus = remote.campus,
                description = remote.description,
                imageUrls = remote.imageUrls.orEmpty(),
                contact = remote.contact,
                ownerId = remote.ownerId.orEmpty(),
                ownerName = remote.ownerName.orEmpty(),
                createdAt = remote.createdAtEpochMillisDetail()
            )
        }

        val cached = cachedItemDao.getById(itemId)
        if (cached != null) {
            return@withContext Item(
                id = cached.itemId,
                title = cached.title,
                price = cached.price,
                category = "",
                campus = cached.campus,
                description = "",
                imageUrls = listOf(cached.coverUrl).filter { it.isNotBlank() },
                contact = "",
                ownerId = cached.ownerId,
                ownerName = cached.ownerName,
                createdAt = cached.createdAt
            )
        }

        fakeRepository.getItemById(itemId)
    }

    suspend fun refreshHomeFromRemote() {
        syncHomeFromRemote()
    }

    suspend fun fetchMyItemsFromRemote(ownerId: String): List<ItemCard> = withContext(Dispatchers.IO) {
        val list = supabaseApi.getMyItems(ownerId = "eq.$ownerId")
        list.map { dto ->
            ItemCard(
                itemId = dto.id,
                title = dto.title,
                price = dto.price,
                campus = dto.campus,
                createdAt = dto.createdAtEpochMillis(),
                coverUrl = dto.coverUrl ?: "",
                ownerId = dto.ownerId ?: ownerId,
                ownerName = dto.ownerName.orEmpty()
            )
        }
    }

    suspend fun publishItem(
        contentResolver: ContentResolver,
        imageUris: List<Uri>,
        title: String,
        price: Double,
        category: String,
        campus: String,
        description: String,
        contact: String,
        ownerId: String,
        ownerName: String
    ) {
        require(imageUris.isNotEmpty()) { "至少选择 1 张图片" }

        withContext(Dispatchers.IO) {
            val bucket = SupabaseRemote.DEFAULT_BUCKET

            val uploadedUrls = try {
                imageUris.mapIndexed { index, uri ->
                    val contentType = contentResolver.getType(uri) ?: "image/jpeg"
                    val ext = when (contentType.lowercase()) {
                        "image/png" -> "png"
                        "image/webp" -> "webp"
                        else -> "jpg"
                    }
                    val path = "items/${ownerId}/${System.currentTimeMillis()}_${index}_${UUID.randomUUID()}.$ext"
                    val body = SupabaseRemote.streamingRequestBody(
                        contentResolver = contentResolver,
                        uri = uri,
                        contentType = contentType
                    )

                    supabaseApi.uploadObject(
                        bucket = bucket,
                        path = path,
                        contentType = contentType,
                        body = body
                    )

                    SupabaseRemote.publicObjectUrl(bucket = bucket, path = path)
                }
            } catch (t: Throwable) {
                throw RuntimeException("图片上传失败（Storage 权限/RLS）", t)
            }

            val coverUrl = uploadedUrls.first()
            try {
                supabaseApi.insertItem(
                    SupabaseInsertItemRequest(
                        title = title,
                        price = price,
                        category = category,
                        campus = campus,
                        description = description,
                        contact = contact,
                        ownerId = ownerId,
                        ownerName = ownerName,
                        imageUrls = uploadedUrls,
                        coverUrl = coverUrl
                    )
                )
            } catch (t: Throwable) {
                throw RuntimeException("写入商品失败（items 表权限/RLS/字段）", t)
            }

            syncHomeFromRemote()
        }
    }

    private suspend fun syncHomeFromRemote() {
        withContext(Dispatchers.IO) {
            val list = supabaseApi.getItems()
            val now = System.currentTimeMillis()
            val entities = list.map { dto ->
                CachedItemEntity(
                    itemId = dto.id,
                    title = dto.title,
                    price = dto.price,
                    coverUrl = dto.coverUrl ?: "",
                    campus = dto.campus,
                    ownerId = dto.ownerId ?: "",
                    ownerName = dto.ownerName.orEmpty(),
                    createdAt = dto.createdAtEpochMillis(),
                    cachedAt = now
                )
            }

            cachedItemDao.clearAll()
            cachedItemDao.insertAll(entities)
        }
    }

    private suspend fun ensureHomeCache() {
        withContext(Dispatchers.IO) {
            val count = cachedItemDao.count()
            if (count > 0) return@withContext

            val list = fakeRepository.getItems()
            val now = System.currentTimeMillis()
            val entities = list.map { item ->
                CachedItemEntity(
                    itemId = item.id,
                    title = item.title,
                    price = item.price,
                    coverUrl = item.imageUrls.firstOrNull() ?: "",
                    campus = item.campus,
                    ownerId = item.ownerId,
                    ownerName = item.ownerName,
                    createdAt = item.createdAt,
                    cachedAt = now
                )
            }
            cachedItemDao.insertAll(entities)
        }
    }
}

private fun CachedItemEntity.toItemCard(): ItemCard = ItemCard(
    itemId = itemId,
    title = title,
    price = price,
    campus = campus,
    createdAt = createdAt,
    coverUrl = coverUrl,
    ownerId = ownerId,
    ownerName = ownerName
)

private fun Item.toItemCard(): ItemCard = ItemCard(
    itemId = id,
    title = title,
    price = price,
    campus = campus,
    createdAt = createdAt,
    coverUrl = imageUrls.firstOrNull() ?: "",
    ownerId = ownerId,
    ownerName = ownerName
)
