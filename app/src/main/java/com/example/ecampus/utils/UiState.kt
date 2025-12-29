package com.example.ecampus.utils

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data object Empty : UiState<Nothing>
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
}
