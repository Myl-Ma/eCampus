package com.example.ecampus.data.repository

import com.example.ecampus.data.model.Item
import kotlinx.coroutines.delay

class FakeRepository {
    private val items: List<Item> = (1..10).map { i ->
        Item(
            id = i.toString(),
            title = "商品#$i",
            price = 10.0 * i,
            category = "其他",
            campus = if (i % 2 == 0) "北校区" else "南校区",
            description = "这是用于演示的假数据商品#$i。",
            imageUrls = listOf(
                "https://picsum.photos/seed/${i}/600/400"
            ),
            contact = "手机号：1380000${String.format("%04d", i)}",
            ownerId = "用户_$i",
            ownerName = "用户_$i",
            createdAt = System.currentTimeMillis() - i * 60_000L
        )
    }

    fun peekItems(): List<Item> = items

    suspend fun getItems(): List<Item> {
        delay(500)
        return items
    }

    suspend fun getItemById(id: String): Item? {
        delay(300)
        return items.find { it.id == id }
    }
}
