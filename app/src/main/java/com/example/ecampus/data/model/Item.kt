package com.example.ecampus.data.model

data class Item(
    val id: String,
    val title: String,
    val price: Double,
    val category: String,
    val campus: String,
    val description: String,
    val imageUrls: List<String>,
    val contact: String,
    val ownerId: String,
    val ownerName: String,
    val createdAt: Long
)
