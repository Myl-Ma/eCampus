package com.example.ecampus.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ecampus.data.local.db.entity.CachedItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedItemDao {
    @Query("SELECT * FROM cached_items ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<CachedItemEntity>>

    @Query("SELECT * FROM cached_items WHERE itemId = :itemId LIMIT 1")
    suspend fun getById(itemId: String): CachedItemEntity?

    @Query("SELECT COUNT(*) FROM cached_items")
    suspend fun count(): Int

    @Query("DELETE FROM cached_items")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<CachedItemEntity>)
}
