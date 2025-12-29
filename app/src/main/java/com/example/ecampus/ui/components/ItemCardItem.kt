package com.example.ecampus.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.ecampus.data.model.ItemCard
import com.example.ecampus.utils.toDisplayTime

@Composable
fun ItemCardItem(
    item: ItemCard,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(12.dp)) {
            AsyncImage(
                model = item.coverUrl.ifBlank { "https://picsum.photos/seed/placeholder/600/400" },
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(8.dp))
            )
            Text(item.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            Text("价格：￥${item.price}")
            Text("校区：${item.campus}")
            if (item.ownerName.isNotBlank()) {
                Text("发布者：${item.ownerName}")
            }
            Text("时间：${item.createdAt.toDisplayTime()}")
        }
    }
}
