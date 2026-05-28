package com.joker.homeledger.feature.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joker.homeledger.core.database.entity.CategoryEntity
import com.joker.homeledger.core.model.TransactionType

@Composable
fun CategoryManageScreen(
    onBack: () -> Unit,
    viewModel: CategoryManageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("分类管理", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TransactionType.entries.forEach { type ->
                FilterChip(
                    selected = uiState.selectedType == type,
                    onClick = { viewModel.selectType(type) },
                    label = { Text(if (type == TransactionType.EXPENSE) "支出" else "收入") }
                )
            }
        }
        Button(onClick = viewModel::openAddDialog, modifier = Modifier.fillMaxWidth()) {
            Text("新增分类")
        }
        uiState.message?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.categories, key = { it.id }) { category ->
                CategoryRow(
                    category = category,
                    onToggleHidden = { viewModel.toggleHidden(category) },
                    onPin = { viewModel.pinCategory(category) },
                    onEdit = { viewModel.openEditDialog(category) },
                    onDelete = { viewModel.deleteCategory(category) }
                )
            }
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("返回")
        }
    }

    if (uiState.showAddDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDialog,
            title = { Text(if (uiState.editingCategory == null) "新增分类" else "编辑分类") },
            text = {
                OutlinedTextField(
                    value = uiState.inputName,
                    onValueChange = viewModel::updateInputName,
                    label = { Text("分类名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::saveCategory) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDialog) { Text("取消") }
            }
        )
    }
}

@Composable
private fun CategoryRow(
    category: CategoryEntity,
    onToggleHidden: () -> Unit,
    onPin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = category.name + if (category.isPreset) "（预设）" else "",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = if (category.isHidden) "已隐藏" else "显示中",
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onToggleHidden) {
                    Text(if (category.isHidden) "显示" else "隐藏")
                }
                TextButton(onClick = onPin) { Text("置顶") }
                if (!category.isPreset) {
                    TextButton(onClick = onEdit) { Text("编辑") }
                    TextButton(onClick = onDelete) { Text("删除") }
                }
            }
        }
    }
}
