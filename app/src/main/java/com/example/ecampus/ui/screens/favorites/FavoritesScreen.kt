package com.example.ecampus.ui.screens.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecampus.ui.components.ItemCardItem
import com.example.ecampus.utils.AppViewModelFactory
import com.example.ecampus.utils.UiState

@Composable
fun FavoritesScreen(
    onItemClick: (String) -> Unit,
    vm: FavoritesViewModel = viewModel(factory = AppViewModelFactory())
) {
    val state by vm.state.collectAsState()

    when (val s = state) {
        is UiState.Loading -> Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            CircularProgressIndicator(Modifier.padding(16.dp))
        }
        is UiState.Empty -> Column(Modifier.fillMaxSize()) {
            Text("暂无收藏", Modifier.padding(16.dp))
            Button(
                onClick = { vm.refresh() },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text("刷新")
            }
        }
        is UiState.Error -> Column(Modifier.fillMaxSize()) {
            Text("出错了：${s.message}", Modifier.padding(16.dp))
            Button(
                onClick = { vm.refresh() },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text("重试")
            }
        }
        is UiState.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp)
            ) {
                items(s.data, key = { it.itemId }) { item ->
                    ItemCardItem(item = item, onClick = { onItemClick(item.itemId) })
                }
            }
        }
    }
}
