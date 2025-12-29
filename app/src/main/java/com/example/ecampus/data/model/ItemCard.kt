package com.example.ecampus.data.model

data class ItemCard(
    val itemId: String,
    val title: String,
    val price: Double,
    val campus: String,
    val createdAt: Long,
    val coverUrl: String,
    val ownerId: String,
    val ownerName: String
)
