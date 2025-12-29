package com.example.ecampus.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecampus.data.model.ItemCard
import com.example.ecampus.data.repository.AppRepository
import com.example.ecampus.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repo: AppRepository
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<ItemCard>>>(UiState.Loading)
    val state: StateFlow<UiState<List<ItemCard>>> = _state

    private var observeJob: Job? = null

    init {
        observe()
    }

    private fun observe() {
        _state.value = UiState.Loading
        observeJob?.cancel()
        observeJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.observeFavoriteItems().collect { list ->
                    _state.value = if (list.isEmpty()) UiState.Empty else UiState.Success(list)
                }
            } catch (t: Throwable) {
                _state.value = UiState.Error("加载收藏失败", t)
            }
        }
    }

    fun refresh() {
        observe()
    }
}
