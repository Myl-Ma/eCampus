package com.example.ecampus.utils

import android.content.Context
import androidx.room.Room
import com.example.ecampus.data.local.db.AppDatabase
import com.example.ecampus.data.remote.SupabaseApi
import com.example.ecampus.data.remote.SupabaseRemote
import com.example.ecampus.data.repository.AppRepository
import com.example.ecampus.data.repository.FakeRepository
import com.tencent.mmkv.MMKV
import java.util.UUID

object ServiceLocator {
    private var database: AppDatabase? = null
    private var repository: AppRepository? = null
    private var supabaseApi: SupabaseApi? = null
    private var mmkv: MMKV? = null
    private var userId: String? = null
    private var nickname: String? = null

    private const val KEY_USER_ID = "user_id"
    private const val KEY_NICKNAME = "nickname"

    fun init(appContext: Context) {
        if (database != null && repository != null) return

        ensureUserId(appContext.applicationContext)

        supabaseApi = SupabaseRemote.createApi()

        val db = Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "ecampus.db"
        ).fallbackToDestructiveMigration().build()

        database = db
        repository = AppRepository(
            supabaseApi = checkNotNull(supabaseApi),
            fakeRepository = FakeRepository(),
            favoriteDao = db.favoriteDao(),
            cachedItemDao = db.cachedItemDao()
        )
    }

    fun userId(): String = checkNotNull(userId) { "userId not initialized" }

    fun nickname(): String {
        val n = nickname
        if (!n.isNullOrBlank()) return n
        val store = checkNotNull(mmkv) { "MMKV not initialized" }
        val persisted = store.decodeString(KEY_NICKNAME, "")
        if (!persisted.isNullOrBlank()) {
            nickname = persisted
            return persisted
        }
        return ""
    }

    fun setNickname(value: String) {
        val store = checkNotNull(mmkv) { "MMKV not initialized" }
        val v = value.trim()
        store.encode(KEY_NICKNAME, v)
        nickname = v
    }

    private fun ensureUserId(appContext: Context) {
        if (userId != null && mmkv != null) return

        val store = MMKV.defaultMMKV()
        mmkv = store

        val existing = store.decodeString(KEY_USER_ID, null)
        if (!existing.isNullOrBlank()) {
            userId = existing
            nickname = store.decodeString(KEY_NICKNAME, "")
            return
        }

        val newId = UUID.randomUUID().toString()
        store.encode(KEY_USER_ID, newId)
        userId = newId
        nickname = store.decodeString(KEY_NICKNAME, "")
    }

    fun repository(): AppRepository = checkNotNull(repository) { "ServiceLocator not initialized" }
}
