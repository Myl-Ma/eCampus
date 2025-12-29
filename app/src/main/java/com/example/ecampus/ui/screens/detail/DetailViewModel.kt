package com.example.ecampus.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecampus.data.model.Item
import com.example.ecampus.data.repository.AppRepository
import com.example.ecampus.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repo: AppRepository,
    private val itemId: String
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<Item>>(UiState.Loading)
    val state: StateFlow<UiState<Item>> = _state

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    init {
        load()
        observeFavorite()
    }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val item = repo.getItemDetail(itemId)
                _state.value = item?.let { UiState.Success(it) } ?: UiState.Error("未找到商品：$itemId")
            } catch (t: Throwable) {
                _state.value = UiState.Error("加载详情失败", t)
            }
        }
    }

    private fun observeFavorite() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.observeIsFavorite(itemId).collect { fav ->
                    _isFavorite.value = fav
                }
            } catch (_: Throwable) {
                // ignore: favorite state is secondary
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.toggleFavorite(itemId)
            } catch (t: Throwable) {
                _state.value = UiState.Error("切换收藏失败", t)
            }
        }
    }
}
