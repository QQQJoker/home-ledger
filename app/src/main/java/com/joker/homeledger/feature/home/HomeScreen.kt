package com.joker.homeledger.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joker.homeledger.core.common.DateLabelUtils
import com.joker.homeledger.core.common.MoneyFormatter
import com.joker.homeledger.core.database.model.TransactionListItem
import com.joker.homeledger.core.model.TransactionType

@Composable
fun HomeScreen(
    onAddEntryClick: () -> Unit,
    onViewAllLedger: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "本月概览", style = MaterialTheme.typography.headlineSmall)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("收入: ¥${MoneyFormatter.centToYuan(uiState.summary.incomeCent)}")
                Text("支出: ¥${MoneyFormatter.centToYuan(uiState.summary.expenseCent)}")
                Text("结余: ¥${MoneyFormatter.centToYuan(uiState.summary.balanceCent)}")
            }
        }
        Button(onClick = onAddEntryClick, modifier = Modifier.fillMaxWidth()) {
            Text("记一笔")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "最近流水", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "查看更多",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onViewAllLedger)
            )
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.latestTransactions, key = { it.transaction.id }) { item ->
                HomeTransactionRow(item = item, onClick = { onTransactionClick(item.transaction.id) })
            }
        }
    }
}

@Composable
private fun HomeTransactionRow(item: TransactionListItem, onClick: () -> Unit) {
    val isExpense = item.transaction.type == TransactionType.EXPENSE.name
    val amountColor = if (isExpense) Color(0xFFC62828) else Color(0xFF2E7D32)
    val prefix = if (isExpense) "-" else "+"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(item.categoryName)
                Text(
                    "${item.accountName} · ${DateLabelUtils.timeLabel(item.transaction.occurredAt)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text("$prefix¥${MoneyFormatter.centToYuan(item.transaction.amountCent)}", color = amountColor)
        }
    }
}
