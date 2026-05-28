package com.joker.homeledger.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joker.homeledger.core.common.DateFormatUtils
import com.joker.homeledger.core.common.DateRangeFilter
import com.joker.homeledger.core.common.PeriodPreset
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeFilterPanel(
    value: DateRangeFilter,
    onChange: (DateRangeFilter) -> Unit,
    presets: List<PeriodPreset> = listOf(
        PeriodPreset.TODAY,
        PeriodPreset.THIS_WEEK,
        PeriodPreset.THIS_MONTH,
        PeriodPreset.THIS_YEAR
    ),
    modifier: Modifier = Modifier
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("时间", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            presets.forEach { preset ->
                FilterChip(
                    selected = !value.useCustomRange && value.preset == preset,
                    onClick = {
                        onChange(
                            value.copy(
                                preset = preset,
                                useCustomRange = false
                            )
                        )
                    },
                    label = { Text(preset.label) }
                )
            }
            FilterChip(
                selected = value.useCustomRange,
                onClick = { onChange(value.copy(useCustomRange = true)) },
                label = { Text("自定义") }
            )
        }
        if (value.useCustomRange) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { showStartPicker = true }) {
                    Text(
                        text = value.customStartAt?.let { DateFormatUtils.formatDate(it) } ?: "开始日期"
                    )
                }
                Text("~")
                TextButton(onClick = { showEndPicker = true }) {
                    Text(
                        text = value.customEndAt?.let { DateFormatUtils.formatDate(it) } ?: "结束日期"
                    )
                }
            }
        }
    }

    if (showStartPicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = state.selectedDateMillis ?: return@TextButton
                        onChange(value.copy(customStartAt = startOfDay(millis)))
                        showStartPicker = false
                    }
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (showEndPicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = state.selectedDateMillis ?: return@TextButton
                        onChange(value.copy(customEndAt = endOfDay(millis)))
                        showEndPicker = false
                    }
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

private fun startOfDay(millis: Long): Long {
    val zone = ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
    return date.atStartOfDay(zone).toInstant().toEpochMilli()
}

private fun endOfDay(millis: Long): Long {
    val zone = ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
    return date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
}
