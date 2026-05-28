package com.joker.homeledger.feature.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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

@Composable
fun AppLockSetupScreen(
    onBack: () -> Unit,
    viewModel: AppLockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("应用锁", style = MaterialTheme.typography.headlineSmall)
        Text(
            "开启后，应用进入后台超过 ${uiState.timeoutMinutes} 分钟再回到前台时需要输入 PIN。",
            style = MaterialTheme.typography.bodySmall
        )
        OutlinedTextField(
            value = uiState.pin,
            onValueChange = viewModel::updatePin,
            label = { Text("PIN（4-6位数字）") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.confirmPin,
            onValueChange = viewModel::updateConfirmPin,
            label = { Text("确认 PIN") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.timeoutMinutes.toString(),
            onValueChange = { value ->
                value.toIntOrNull()?.let(viewModel::updateTimeout)
            },
            label = { Text("后台超时（分钟）") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = viewModel::enableLock, modifier = Modifier.fillMaxWidth()) {
            Text(if (uiState.enabled) "更新应用锁" else "开启应用锁")
        }
        if (uiState.enabled) {
            TextButton(onClick = viewModel::disableLock, modifier = Modifier.fillMaxWidth()) {
                Text("关闭应用锁")
            }
        }
        uiState.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("返回") }
    }
}
