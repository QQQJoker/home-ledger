package com.joker.homeledger.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joker.homeledger.core.common.StatsPeriodFilter
import com.joker.homeledger.core.common.StatsScope

@Composable
fun StatsPeriodPanel(
    value: StatsPeriodFilter,
    onChange: (StatsPeriodFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatsScope.entries.forEach { scope ->
                FilterChip(
                    selected = value.scope == scope,
                    onClick = { onChange(value.copy(scope = scope)) },
                    label = { Text(scope.label) }
                )
            }
        }
        if (value.scope != StatsScope.ALL) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { onChange(value.previous()) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "上一期")
                }
                Text(value.periodTitle(), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { onChange(value.next()) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "下一期")
                }
            }
        }
    }
}
