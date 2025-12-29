package com.example.ecampus.ui.screens.publish

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecampus.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed class PublishUiState {
    data object Idle : PublishUiState()
    data object Loading : PublishUiState()
    data class Error(val message: String, val throwable: Throwable? = null) : PublishUiState()
    data object Success : PublishUiState()
}

class PublishViewModel(
    private val repo: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PublishUiState>(PublishUiState.Idle)
    val uiState: StateFlow<PublishUiState> = _uiState.asStateFlow()

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages.asStateFlow()

    fun setSelectedImages(uris: List<Uri>) {
        _selectedImages.value = uris
    }

    fun clearAll() {
        _selectedImages.value = emptyList()
        _uiState.value = PublishUiState.Idle
    }

    fun publish(
        contentResolver: ContentResolver,
        ownerId: String,
        ownerName: String,
        imageUris: List<Uri>,
        title: String,
        priceText: String,
        category: String,
        campus: String,
        description: String,
        contact: String
    ) {
        val price = priceText.toDoubleOrNull()
        if (imageUris.isEmpty()) {
            _uiState.value = PublishUiState.Error("请至少选择 1 张图片")
            return
        }
        if (title.isBlank()) {
            _uiState.value = PublishUiState.Error("请输入标题")
            return
        }
        if (price == null || price < 0) {
            _uiState.value = PublishUiState.Error("请输入正确的价格")
            return
        }
        if (category.isBlank()) {
            _uiState.value = PublishUiState.Error("请输入分类")
            return
        }
        if (campus.isBlank()) {
            _uiState.value = PublishUiState.Error("请输入校区")
            return
        }
        if (description.isBlank()) {
            _uiState.value = PublishUiState.Error("请输入描述")
            return
        }
        if (contact.isBlank()) {
            _uiState.value = PublishUiState.Error("请输入联系方式")
            return
        }

        _uiState.value = PublishUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.publishItem(
                    contentResolver = contentResolver,
                    imageUris = imageUris,
                    title = title.trim(),
                    price = price,
                    category = category.trim(),
                    campus = campus.trim(),
                    description = description.trim(),
                    contact = contact.trim(),
                    ownerId = ownerId,
                    ownerName = ownerName.trim()
                )
                _uiState.value = PublishUiState.Success
            } catch (t: Throwable) {
                val message = buildErrorMessage(t)
                _uiState.value = PublishUiState.Error(message, t)
            }
        }
    }

    private fun buildErrorMessage(t: Throwable): String {
        val contextPrefix = t.message?.takeIf { it.isNotBlank() }

        val http = sequenceOf(t, t.cause, t.cause?.cause)
            .filterNotNull()
            .firstOrNull { it is HttpException } as? HttpException

        if (http != null) {
            val code = http.code()
            val body = runCatching { http.response()?.errorBody()?.string() }.getOrNull()

            val base = if (contextPrefix == null) "发布失败" else "发布失败：$contextPrefix"
            if (!body.isNullOrBlank()) {
                return "$base（HTTP $code）$body"
            }
            val detail = http.message()?.takeIf { it.isNotBlank() }
            return if (detail == null) "$base（HTTP $code）" else "$base（HTTP $code）$detail"
        }

        return if (contextPrefix == null) "发布失败，请重试" else "发布失败：$contextPrefix"
    }
}
