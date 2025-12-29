package com.example.ecampus.ui.screens.publish

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ecampus.utils.AppViewModelFactory
import com.example.ecampus.utils.ServiceLocator
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions

@Composable
fun PublishScreen(
    onPublishSuccess: () -> Unit,
    vm: PublishViewModel = viewModel(factory = AppViewModelFactory())
) {
    val context = LocalContext.current
    val uiState by vm.uiState.collectAsState()
    val selectedImages by vm.selectedImages.collectAsState()

    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var campus by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }

    var cropQueue by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var croppedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var pendingCropUri by remember { mutableStateOf<Uri?>(null) }

    fun buildCropOptions(uri: Uri): CropImageContractOptions {
        return CropImageContractOptions(
            uri,
            CropImageOptions().apply {
                fixAspectRatio = true
                aspectRatioX = 4
                aspectRatioY = 3
                activityTitle = "裁剪图片"
                cropMenuCropButtonTitle = "完成"
            }
        )
    }

    val cropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uri = result.uriContent
            if (uri != null) {
                croppedImages = croppedImages + uri
                vm.setSelectedImages(croppedImages)
            }
        }

        val remaining = cropQueue.drop(1)
        cropQueue = remaining
        pendingCropUri = remaining.firstOrNull()
    }

    LaunchedEffect(pendingCropUri) {
        val uri = pendingCropUri ?: return@LaunchedEffect
        cropLauncher.launch(buildCropOptions(uri))
    }

    val categoryOptions = remember {
        listOf(
            "数码",
            "书籍",
            "生活",
            "学习用品",
            "服饰",
            "其他"
        )
    }

    val pickImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            val list = uris
            if (list.isEmpty()) return@rememberLauncherForActivityResult

            croppedImages = emptyList()
            vm.setSelectedImages(emptyList())
            cropQueue = list
            pendingCropUri = list.first()
        }
    )

    LaunchedEffect(uiState) {
        if (uiState is PublishUiState.Success) {
            title = ""
            price = ""
            category = ""
            campus = ""
            description = ""
            contact = ""
            vm.clearAll()
            onPublishSuccess()
        }
    }

    Column(
        Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
    ) {
        Text("发布商品")
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth()) {
            Button(
                onClick = { pickImagesLauncher.launch("image/*") },
                enabled = uiState !is PublishUiState.Loading
            ) {
                Text(if (selectedImages.isEmpty()) "选择图片" else "重新选择图片")
            }
            Spacer(Modifier.width(12.dp))
            Text("已选 ${selectedImages.size} 张")
        }

        if (selectedImages.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            ImagePreviewRow(selectedImages)
        }

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("标题") },
            enabled = uiState !is PublishUiState.Loading
        )

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("价格") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = uiState !is PublishUiState.Loading
        )

        Spacer(Modifier.height(12.dp))
        CategoryPicker(
            options = categoryOptions,
            value = category,
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it },
            onSelect = { selected ->
                category = selected
            },
            enabled = uiState !is PublishUiState.Loading
        )

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = campus,
            onValueChange = { campus = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("校区") },
            enabled = uiState !is PublishUiState.Loading
        )

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("描述") },
            enabled = uiState !is PublishUiState.Loading
        )

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = contact,
            onValueChange = { contact = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("联系方式") },
            enabled = uiState !is PublishUiState.Loading
        )

        Spacer(Modifier.height(16.dp))
        when (val s = uiState) {
            is PublishUiState.Loading -> {
                Row(Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(Modifier.padding(end = 12.dp))
                    Text("发布中...")
                }
            }
            is PublishUiState.Error -> {
                Text("出错了：${s.message}")
                if (s.throwable?.message?.isNotBlank() == true) {
                    Spacer(Modifier.height(6.dp))
                    Text("详细信息：${s.throwable?.message}")
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        vm.publish(
                            contentResolver = context.contentResolver,
                            ownerId = ServiceLocator.userId(),
                            ownerName = ServiceLocator.nickname().ifBlank { "同学" },
                            imageUris = selectedImages,
                            title = title,
                            priceText = price,
                            category = category,
                            campus = campus,
                            description = description,
                            contact = contact
                        )
                    }
                ) {
                    Text("重试")
                }
            }
            else -> {
                Button(
                    onClick = {
                        vm.publish(
                            contentResolver = context.contentResolver,
                            ownerId = ServiceLocator.userId(),
                            ownerName = ServiceLocator.nickname().ifBlank { "同学" },
                            imageUris = selectedImages,
                            title = title,
                            priceText = price,
                            category = category,
                            campus = campus,
                            description = description,
                            contact = contact
                        )
                    },
                    enabled = uiState !is PublishUiState.Loading
                ) {
                    Text("发布")
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CategoryPicker(
    options: List<String>,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit,
    enabled: Boolean
) {
    Box {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = {},
            label = { Text("分类") },
            singleLine = true,
            readOnly = true,
            enabled = enabled,
            trailingIcon = {
                IconButton(onClick = { if (enabled) onExpandedChange(true) }, enabled = enabled) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Composable
private fun ImagePreviewRow(uris: List<Uri>) {
    LazyRow {
        items(uris, key = { it.toString() }) { uri ->
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .height(80.dp)
                    .width(80.dp)
            )
        }
    }
}
