package com.joker.homeledger.feature.ledger

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joker.homeledger.core.common.DateLabelUtils
import com.joker.homeledger.core.common.MoneyFormatter
import com.joker.homeledger.core.database.entity.AccountEntity
import com.joker.homeledger.core.database.entity.CategoryEntity
import com.joker.homeledger.core.database.model.TransactionListItem
import com.joker.homeledger.core.model.LedgerFilter
import com.joker.homeledger.core.model.TransactionType
import com.joker.homeledger.core.model.TypeFilter
import com.joker.homeledger.ui.components.DateRangeFilterPanel
import com.joker.homeledger.ui.components.DropdownSelectorGeneric

@Composable
fun LedgerScreen(
    onTransactionClick: (Long) -> Unit,
    viewModel: LedgerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "流水", style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = viewModel::toggleFilterPanel) {
                Icon(Icons.Default.FilterList, contentDescription = "筛选")
            }
        }

        if (uiState.showFilterPanel) {
            LedgerFilterPanel(
                filter = uiState.filter,
                accounts = uiState.accounts,
                categories = when (uiState.filter.typeFilter) {
                    TypeFilter.INCOME -> uiState.incomeCategories
                    TypeFilter.EXPENSE -> uiState.expenseCategories
                    TypeFilter.ALL -> uiState.expenseCategories + uiState.incomeCategories
                },
                onDateRangeChange = viewModel::updateDateRange,
                onTypeChange = viewModel::updateTypeFilter,
                onCategoryChange = viewModel::updateCategoryFilter,
                onAccountChange = viewModel::updateAccountFilter,
                onClear = viewModel::clearFilters
            )
        }

        if (uiState.sections.isEmpty()) {
            Text(
                text = "暂无流水",
                modifier = Modifier.padding(top = 24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.sections.forEach { section ->
                    item(key = "header-${section.title}") {
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(section.items, key = { it.transaction.id }) { item ->
                        TransactionRow(
                            item = item,
                            onClick = { onTransactionClick(item.transaction.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LedgerFilterPanel(
    filter: LedgerFilter,
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    onDateRangeChange: (com.joker.homeledger.core.common.DateRangeFilter) -> Unit,
    onTypeChange: (TypeFilter) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    onAccountChange: (Long?) -> Unit,
    onClear: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DateRangeFilterPanel(
            value = filter.dateRange,
            onChange = onDateRangeChange
        )
        DropdownSelectorGeneric(
            label = "类型",
            options = TypeFilter.entries.map { it to it.label },
            selected = filter.typeFilter,
            onSelected = onTypeChange
        )
        DropdownSelectorGeneric(
            label = "账户",
            options = listOf(null to "全部") + accounts.map { it.id to it.name },
            selected = filter.accountId,
            onSelected = onAccountChange
        )
        DropdownSelectorGeneric(
            label = "分类",
            options = listOf(null to "全部") + categories.map { it.id to it.name },
            selected = filter.categoryId,
            onSelected = onCategoryChange
        )
        Text(
            text = "重置筛选",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onClear)
        )
    }
}

@Composable
private fun TransactionRow(
    item: TransactionListItem,
    onClick: () -> Unit
) {
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
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.categoryName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${item.accountName} · ${DateLabelUtils.timeLabel(item.transaction.occurredAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                item.transaction.note?.takeIf { it.isNotBlank() }?.let { note ->
                    Text(
                        text = note,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Text(
                text = "$prefix¥${MoneyFormatter.centToYuan(item.transaction.amountCent)}",
                color = amountColor,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
