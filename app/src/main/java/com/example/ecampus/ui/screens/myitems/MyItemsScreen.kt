package com.example.ecampus.ui.screens.myitems

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
import com.example.ecampus.data.model.ItemCard
import com.example.ecampus.ui.components.ItemCardItem
import com.example.ecampus.utils.AppViewModelFactory
import com.example.ecampus.utils.UiState

@Composable
fun MyItemsScreen(
    onItemClick: (String) -> Unit,
    vm: MyItemsViewModel = viewModel(factory = AppViewModelFactory())
) {
    val state by vm.state.collectAsState()

    when (val s = state) {
        is UiState.Loading -> Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            CircularProgressIndicator(Modifier.padding(16.dp))
        }

        is UiState.Empty -> Column(Modifier.fillMaxSize()) {
            Text("你还没有发布过商品", Modifier.padding(16.dp))
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

        is UiState.Success -> Column(Modifier.fillMaxSize()) {
            Button(
                onClick = { vm.refresh() },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("刷新")
            }
            ItemList(items = s.data, onItemClick = onItemClick)
        }
    }
}

@Composable
private fun ItemList(items: List<ItemCard>, onItemClick: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp)
    ) {
        items(items, key = { it.itemId }) { item ->
            ItemCardItem(
                item = item,
                onClick = { onItemClick(item.itemId) }
            )
        }
    }
}
