package com.joker.homeledger.feature.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joker.homeledger.core.model.TransactionType
import com.joker.homeledger.ui.components.DateTimeSelector
import com.joker.homeledger.ui.components.DropdownSelector

@Composable
fun EntryScreen(
    onBack: () -> Unit,
    viewModel: EntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.shouldClose) {
        if (uiState.shouldClose) onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (uiState.isEditMode) "编辑流水" else "记一笔",
            style = MaterialTheme.typography.headlineSmall
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TransactionType.entries.forEach { type ->
                Row {
                    RadioButton(
                        selected = uiState.type == type,
                        onClick = { viewModel.updateType(type) }
                    )
                    Text(text = if (type == TransactionType.EXPENSE) "支出" else "收入")
                }
            }
        }

        OutlinedTextField(
            value = uiState.amountInput,
            onValueChange = viewModel::updateAmount,
            label = { Text("金额（元）") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        DropdownSelector(
            label = "分类",
            options = uiState.categoryOptions,
            selectedId = uiState.selectedCategoryId,
            onSelected = viewModel::updateCategory
        )

        DropdownSelector(
            label = "账户",
            options = uiState.accountOptions,
            selectedId = uiState.selectedAccountId,
            onSelected = viewModel::updateAccount
        )

        DateTimeSelector(
            occurredAtMillis = uiState.occurredAtMillis,
            onOccurredAtChange = viewModel::updateOccurredAt
        )

        OutlinedTextField(
            value = uiState.noteInput,
            onValueChange = viewModel::updateNote,
            label = { Text("备注（可选）") },
            modifier = Modifier.fillMaxWidth()
        )

        uiState.errorMessage?.let { message ->
            Text(text = message, color = MaterialTheme.colorScheme.error)
        }
        if (uiState.saveSuccess) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "保存成功",
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Button(
            onClick = viewModel::save,
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (uiState.isSaving) "保存中..." else "保存")
        }

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("返回")
        }
    }
}
