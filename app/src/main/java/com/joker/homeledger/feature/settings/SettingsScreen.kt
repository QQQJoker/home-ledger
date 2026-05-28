package com.joker.homeledger.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onCategoryManage: () -> Unit,
    onAccountManage: () -> Unit,
    onBackup: () -> Unit,
    onAppLock: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("我的", style = MaterialTheme.typography.headlineSmall)
        SettingsItem(title = "分类管理", subtitle = "预设隐藏、自定义增删改、置顶", onClick = onCategoryManage)
        SettingsItem(title = "账户管理", subtitle = "新增账户、停用、有关联流水不可删", onClick = onAccountManage)
        SettingsItem(title = "数据备份与恢复", subtitle = "AES-GCM 加密备份，全量覆盖恢复", onClick = onBackup)
        SettingsItem(title = "应用锁", subtitle = "PIN 解锁，后台超时自动上锁", onClick = onAppLock)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("关于", style = MaterialTheme.typography.titleMedium)
                Text("家账本 v1.0.3", style = MaterialTheme.typography.bodyMedium)
                Text("数据仅保存在本机，不上传服务器。", style = MaterialTheme.typography.bodySmall)
                Text("卸载应用会清除本地数据，请定期备份。", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SettingsItem(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
