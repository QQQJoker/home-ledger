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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joker.homeledger.core.security.AppLockManager

@Composable
fun UnlockScreen(
    appLockManager: AppLockManager,
    onUnlocked: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("家账本", style = MaterialTheme.typography.headlineMedium)
        Text("请输入 PIN 解锁", modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = {
                if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                    pin = it
                    error = null
                }
            },
            label = { Text("PIN") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
        Button(
            onClick = {
                if (appLockManager.verifyPin(pin)) {
                    onUnlocked()
                } else {
                    error = "PIN 不正确"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("解锁")
        }
    }
}
