package com.example.ecampus.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_items")
data class CachedItemEntity(
    @PrimaryKey val itemId: String,
    val title: String,
    val price: Double,
    val coverUrl: String,
    val campus: String,
    val ownerId: String,
    val ownerName: String,
    val createdAt: Long,
    val cachedAt: Long
)
