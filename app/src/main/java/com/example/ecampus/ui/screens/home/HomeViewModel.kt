package com.example.ecampus.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecampus.data.model.ItemCard
import com.example.ecampus.data.repository.AppRepository
import com.example.ecampus.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repo: AppRepository
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<ItemCard>>>(UiState.Loading)
    val state: StateFlow<UiState<List<ItemCard>>> = _state

    private val _isShowingCache = MutableStateFlow(false)
    val isShowingCache: StateFlow<Boolean> = _isShowingCache.asStateFlow()

    private var observeJob: Job? = null

    init {
        observe()
    }

    private fun observe() {
        _state.value = UiState.Loading
        observeJob?.cancel()
        observeJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.observeHomeItemsWithSource().collect { result ->
                    _isShowingCache.value = result.isCache
                    _state.value = if (result.items.isEmpty()) UiState.Empty else UiState.Success(result.items)
                }
            } catch (t: Throwable) {
                _state.value = UiState.Error("加载首页列表失败：${t.message ?: "未知错误"}", t)
            }
        }
    }

    fun refresh() {
        _state.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.refreshHomeFromRemote()
            } catch (t: Throwable) {
                _state.value = UiState.Error("刷新首页失败：${t.message ?: "未知错误"}", t)
            }
        }
    }
}
