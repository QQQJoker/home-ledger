package com.joker.homeledger.feature.account

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
import com.joker.homeledger.core.common.MoneyFormatter
import com.joker.homeledger.core.database.entity.AccountEntity

@Composable
fun AccountManageScreen(
    onBack: () -> Unit,
    viewModel: AccountManageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("账户管理", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = viewModel::openAddDialog, modifier = Modifier.fillMaxWidth()) {
            Text("新增账户")
        }
        uiState.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.accounts, key = { it.id }) { account ->
                AccountRow(
                    account = account,
                    onToggleDisabled = { viewModel.toggleDisabled(account) },
                    onEdit = { viewModel.openEditDialog(account) },
                    onDelete = { viewModel.deleteAccount(account) }
                )
            }
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("返回") }
    }

    val dialog = uiState.dialog
    if (dialog.showDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDialog,
            title = { Text(if (dialog.editingAccount == null) "新增账户" else "编辑账户") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dialog.inputName,
                        onValueChange = viewModel::updateInputName,
                        label = { Text("账户名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dialog.inputBalance,
                        onValueChange = viewModel::updateInputBalance,
                        label = { Text("账户余额（元）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text(
                                if (dialog.editingAccount == null) {
                                    "新增后记账会在此基础上增减"
                                } else {
                                    "编辑余额不会自动重算历史流水"
                                }
                            )
                        }
                    )
                }
            },
            confirmButton = { TextButton(onClick = viewModel::saveAccount) { Text("保存") } },
            dismissButton = { TextButton(onClick = viewModel::dismissDialog) { Text("取消") } }
        )
    }
}

@Composable
private fun AccountRow(
    account: AccountEntity,
    onToggleDisabled: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = account.name + if (account.isPreset) "（内置）" else "",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "余额：¥${MoneyFormatter.centToYuan(account.balanceCent)} · ${if (account.isDisabled) "已停用" else "可用"}",
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onToggleDisabled) {
                    Text(if (account.isDisabled) "启用" else "停用")
                }
                TextButton(onClick = onEdit) { Text("编辑") }
                if (!account.isPreset) {
                    TextButton(onClick = onDelete) { Text("删除") }
                }
            }
        }
    }
}
