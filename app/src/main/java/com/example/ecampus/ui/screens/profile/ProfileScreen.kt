package com.example.ecampus.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ecampus.utils.ServiceLocator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit

@Composable
fun ProfileScreen(
    onMyItemsClick: () -> Unit
) {
    var nickname by remember { mutableStateOf(ServiceLocator.nickname()) }
    var isEditing by remember { mutableStateOf(nickname.isBlank()) }

    Column(Modifier.padding(16.dp)) {
        Text("我的页面")

        Row(Modifier.padding(top = 12.dp)) {
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("昵称") },
                readOnly = !isEditing,
                modifier = Modifier.weight(1f)
            )

            if (!isEditing) {
                IconButton(
                    onClick = { isEditing = true },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                }
            }
        }

        if (isEditing) {
            Button(
                onClick = {
                    ServiceLocator.setNickname(nickname)
                    isEditing = false
                },
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text("保存")
            }
        }

        Button(onClick = onMyItemsClick, modifier = Modifier.padding(top = 16.dp)) {
            Text("我的发布")
        }
    }
}
