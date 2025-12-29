package com.example.ecampus.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.ecampus.ui.screens.detail.DetailViewModel
import com.example.ecampus.ui.screens.favorites.FavoritesViewModel
import com.example.ecampus.ui.screens.home.HomeViewModel
import com.example.ecampus.ui.screens.myitems.MyItemsViewModel
import com.example.ecampus.ui.screens.publish.PublishViewModel

class AppViewModelFactory(
    private val itemId: String? = null,
    private val serviceLocator: ServiceLocator = ServiceLocator
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val repo = serviceLocator.repository()

        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repo) as T
            modelClass.isAssignableFrom(FavoritesViewModel::class.java) -> FavoritesViewModel(repo) as T
            modelClass.isAssignableFrom(MyItemsViewModel::class.java) -> MyItemsViewModel(repo) as T
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                val id = requireNotNull(itemId) { "缺少商品ID" }
                DetailViewModel(repo, id) as T
            }
            modelClass.isAssignableFrom(PublishViewModel::class.java) -> PublishViewModel(repo) as T
            else -> throw IllegalArgumentException("未知 ViewModel: ${modelClass.name}")
        }
    }
}
