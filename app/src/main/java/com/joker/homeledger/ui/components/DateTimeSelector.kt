package com.joker.homeledger.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joker.homeledger.core.common.DateFormatUtils
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeSelector(
    occurredAtMillis: Long,
    onOccurredAtChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = DateFormatUtils.formatDateTime(occurredAtMillis),
            onValueChange = {},
            readOnly = true,
            label = { Text("发生时间") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { showDatePicker = true }) { Text("选择日期") }
            TextButton(onClick = { showTimePicker = true }) { Text("选择时间") }
        }
    }

    if (showDatePicker) {
        val zone = ZoneId.systemDefault()
        val state = rememberDatePickerState(initialSelectedDateMillis = occurredAtMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selected = state.selectedDateMillis ?: return@TextButton
                        val current = Instant.ofEpochMilli(occurredAtMillis).atZone(zone)
                        val selectedDate = Instant.ofEpochMilli(selected).atZone(zone).toLocalDate()
                        val updated = selectedDate.atTime(current.hour, current.minute).atZone(zone)
                        onOccurredAtChange(updated.toInstant().toEpochMilli())
                        showDatePicker = false
                    }
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (showTimePicker) {
        val zone = ZoneId.systemDefault()
        val zoned = Instant.ofEpochMilli(occurredAtMillis).atZone(zone)
        val state = rememberTimePickerState(
            initialHour = zoned.hour,
            initialMinute = zoned.minute,
            is24Hour = true
        )
        DatePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updated = zoned.toLocalDate()
                            .atTime(state.hour, state.minute)
                            .atZone(zone)
                        onOccurredAtChange(updated.toInstant().toEpochMilli())
                        showTimePicker = false
                    }
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("取消") }
            }
        ) {
            TimePicker(state = state)
        }
    }
}
