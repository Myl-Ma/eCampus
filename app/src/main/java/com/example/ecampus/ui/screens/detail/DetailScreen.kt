package com.example.ecampus.ui.screens.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ecampus.utils.AppViewModelFactory
import com.example.ecampus.utils.UiState
import com.example.ecampus.utils.toDisplayTime

@Composable
fun DetailScreen(
    itemId: String,
    vm: DetailViewModel = viewModel(factory = AppViewModelFactory(itemId = itemId))
) {
    val state by vm.state.collectAsState()
    val isFavorite by vm.isFavorite.collectAsState()

    when (val s = state) {
        is UiState.Loading -> CircularProgressIndicator(Modifier.padding(16.dp))
        is UiState.Error -> Text("出错了：${s.message}", Modifier.padding(16.dp))
        is UiState.Empty -> Text("暂无数据", Modifier.padding(16.dp))
        is UiState.Success -> {
            val item = s.data
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                AsyncImage(
                    model = item.imageUrls.firstOrNull(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                )
                Text("标题：${item.title}")
                Text("价格：￥${item.price}")
                Text("分类：${item.category}")
                Text("校区：${item.campus}")
                Text("描述：${item.description}")
                Text("联系方式：${item.contact}")
                if (item.ownerName.isNotBlank()) {
                    Text("发布者：${item.ownerName}")
                }
                Text("创建时间：${item.createdAt.toDisplayTime()}")

                Button(
                    onClick = { vm.toggleFavorite() },
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(if (isFavorite) "取消收藏" else "加入收藏")
                }
            }
        }
    }
}
