package com.example.ecampus.ui.screens.myitems

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecampus.data.model.ItemCard
import com.example.ecampus.data.repository.AppRepository
import com.example.ecampus.utils.ServiceLocator
import com.example.ecampus.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyItemsViewModel(
    private val repo: AppRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<ItemCard>>>(UiState.Loading)
    val state: StateFlow<UiState<List<ItemCard>>> = _state

    init {
        refresh()
    }

    fun refresh() {
        _state.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ownerId = ServiceLocator.userId()
                val list = repo.fetchMyItemsFromRemote(ownerId)
                _state.value = if (list.isEmpty()) UiState.Empty else UiState.Success(list)
            } catch (t: Throwable) {
                _state.value = UiState.Error("加载我的发布失败", t)
            }
        }
    }
}
