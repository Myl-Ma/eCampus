package com.example.ecampus.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ecampus.data.local.db.dao.CachedItemDao
import com.example.ecampus.data.local.db.dao.FavoriteDao
import com.example.ecampus.data.local.db.entity.CachedItemEntity
import com.example.ecampus.data.local.db.entity.FavoriteEntity

@Database(
    entities = [
        FavoriteEntity::class,
        CachedItemEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun cachedItemDao(): CachedItemDao
}
