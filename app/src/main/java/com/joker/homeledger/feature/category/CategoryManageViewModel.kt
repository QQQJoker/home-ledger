package com.joker.homeledger.feature.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joker.homeledger.core.data.repository.CategoryRepository
import com.joker.homeledger.core.database.entity.CategoryEntity
import com.joker.homeledger.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryManageUiState(
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val categories: List<CategoryEntity> = emptyList(),
    val message: String? = null,
    val showAddDialog: Boolean = false,
    val editingCategory: CategoryEntity? = null,
    val inputName: String = ""
)

@HiltViewModel
class CategoryManageViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val selectedType = MutableStateFlow(TransactionType.EXPENSE)
    private val dialogState = MutableStateFlow(Triple(false, null as CategoryEntity?, ""))
    private val messageState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CategoryManageUiState> = kotlinx.coroutines.flow.combine(
        selectedType.flatMapLatest { type ->
            categoryRepository.observeAllByType(type.name)
        },
        selectedType,
        dialogState,
        messageState
    ) { categories, type, (showDialog, editing, inputName), message ->
        CategoryManageUiState(
            selectedType = type,
            categories = categories,
            message = message,
            showAddDialog = showDialog,
            editingCategory = editing,
            inputName = inputName
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoryManageUiState())

    fun selectType(type: TransactionType) {
        selectedType.value = type
    }

    fun openAddDialog() {
        dialogState.value = Triple(true, null, "")
    }

    fun openEditDialog(category: CategoryEntity) {
        if (category.isPreset) return
        dialogState.value = Triple(true, category, category.name)
    }

    fun updateInputName(name: String) {
        val (show, editing, _) = dialogState.value
        dialogState.value = Triple(show, editing, name)
    }

    fun dismissDialog() {
        dialogState.value = Triple(false, null, "")
    }

    fun saveCategory() {
        val (showDialog, editing, name) = dialogState.value
        if (!showDialog) return
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            messageState.value = "分类名称不能为空"
            return
        }
        val type = selectedType.value.name
        viewModelScope.launch {
            val excludeId = editing?.id ?: -1L
            if (categoryRepository.isNameDuplicated(type, trimmed, excludeId)) {
                messageState.value = "同类型下分类名称已存在"
                return@launch
            }
            val now = System.currentTimeMillis()
            runCatching {
                if (editing == null) {
                    val maxOrder = uiState.value.categories.maxOfOrNull { it.sortOrder } ?: 0
                    categoryRepository.addCategory(
                        CategoryEntity(
                            name = trimmed,
                            type = type,
                            isPreset = false,
                            sortOrder = maxOrder + 1,
                            createdAt = now,
                            updatedAt = now
                        )
                    )
                } else {
                    categoryRepository.updateCategory(
                        editing.copy(name = trimmed, updatedAt = now)
                    )
                }
            }.onSuccess {
                messageState.value = "保存成功"
                dismissDialog()
            }.onFailure {
                messageState.value = "保存失败"
            }
        }
    }

    fun toggleHidden(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.setHidden(category, !category.isHidden)
        }
    }

    fun pinCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.pinToTop(category)
            messageState.value = "已置顶"
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        if (category.isPreset) return
        viewModelScope.launch {
            if (!categoryRepository.canDelete(category.id)) {
                messageState.value = "该分类下仍有流水，无法删除"
                return@launch
            }
            runCatching { categoryRepository.deleteCategory(category.id) }
                .onSuccess { messageState.value = "已删除" }
                .onFailure { messageState.value = "删除失败" }
        }
    }
}
