package com.joker.homeledger.feature.ledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TransactionDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: TransactionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("流水详情", style = MaterialTheme.typography.headlineSmall)
        if (uiState.loading) {
            Text("加载中...")
        } else if (uiState.item == null) {
            Text("记录不存在")
        } else {
            val item = uiState.item!!
            DetailLine("类型", uiState.typeLabel)
            DetailLine("金额", uiState.amountText)
            DetailLine("分类", item.categoryName)
            DetailLine("账户", item.accountName)
            DetailLine("发生时间", uiState.occurredText)
            DetailLine("备注", item.transaction.note.orEmpty().ifBlank { "无" })

            Button(
                onClick = { onEdit(item.transaction.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("编辑")
            }
            OutlinedButton(
                onClick = viewModel::showDeleteDialog,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("删除")
            }
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("返回")
        }
        uiState.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            title = { Text("确认删除") },
            text = { Text("删除后不可恢复，确定要删除这条流水吗？") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteDialog) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
