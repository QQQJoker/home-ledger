package com.joker.homeledger.feature.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun BackupScreen(
    onBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        val password = viewModel.consumeExportPassword()
        if (uri != null && password != null) {
            viewModel.export(context.contentResolver, uri, password)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.requestRestore(uri)
        }
    }

    if (uiState.showPasswordDialog) {
        BackupPasswordDialog(
            mode = uiState.dialogMode,
            password = uiState.dialogPassword,
            confirmPassword = uiState.dialogConfirmPassword,
            message = uiState.message,
            onPasswordChange = viewModel::updateDialogPassword,
            onConfirmPasswordChange = viewModel::updateDialogConfirmPassword,
            onDismiss = viewModel::dismissPasswordDialog,
            onConfirm = {
                when (uiState.dialogMode) {
                    BackupPasswordDialogMode.EXPORT -> {
                        viewModel.confirmExportPassword {
                            exportLauncher.launch("home-ledger-backup.hlbk")
                        }
                    }
                    BackupPasswordDialogMode.RESTORE -> {
                        viewModel.confirmRestorePassword(context.contentResolver)
                    }
                    null -> Unit
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("数据备份与恢复", style = MaterialTheme.typography.headlineSmall)
        Text(
            "备份文件使用 AES-GCM 加密。导出时设置密码，恢复时需输入相同密码。恢复会全量覆盖当前数据。",
            style = MaterialTheme.typography.bodySmall
        )
        Button(
            onClick = viewModel::requestExport,
            enabled = !uiState.isWorking,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("导出加密备份")
        }
        Button(
            onClick = { importLauncher.launch(arrayOf("application/octet-stream", "*/*")) },
            enabled = !uiState.isWorking,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("从备份恢复（覆盖）")
        }
        uiState.message?.let {
            if (!uiState.showPasswordDialog) {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("返回")
        }
    }
}

@Composable
private fun BackupPasswordDialog(
    mode: BackupPasswordDialogMode?,
    password: String,
    confirmPassword: String,
    message: String?,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val isExport = mode == BackupPasswordDialogMode.EXPORT
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isExport) "设置备份密码" else "输入备份密码") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (isExport) {
                        "请设置至少 6 位密码，用于加密备份文件。"
                    } else {
                        "请输入导出该备份时设置的密码。"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text(if (isExport) "备份密码" else "密码") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (isExport) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = { Text("确认密码") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                message?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(if (isExport) "下一步" else "恢复")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
