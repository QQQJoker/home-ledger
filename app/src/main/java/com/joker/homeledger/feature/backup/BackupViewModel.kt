package com.joker.homeledger.feature.backup

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joker.homeledger.core.backup.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class BackupPasswordDialogMode {
    EXPORT,
    RESTORE
}

data class BackupUiState(
    val message: String? = null,
    val isWorking: Boolean = false,
    val restoreSuccess: Boolean = false,
    val showPasswordDialog: Boolean = false,
    val dialogMode: BackupPasswordDialogMode? = null,
    val dialogPassword: String = "",
    val dialogConfirmPassword: String = "",
    val pendingRestoreUri: Uri? = null
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupManager: BackupManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    private var pendingExportPassword: String? = null

    fun requestExport() {
        _uiState.update {
            it.copy(
                showPasswordDialog = true,
                dialogMode = BackupPasswordDialogMode.EXPORT,
                dialogPassword = "",
                dialogConfirmPassword = "",
                message = null
            )
        }
    }

    fun requestRestore(uri: Uri) {
        _uiState.update {
            it.copy(
                pendingRestoreUri = uri,
                showPasswordDialog = true,
                dialogMode = BackupPasswordDialogMode.RESTORE,
                dialogPassword = "",
                dialogConfirmPassword = "",
                message = null
            )
        }
    }

    fun updateDialogPassword(value: String) {
        _uiState.update { it.copy(dialogPassword = value, message = null) }
    }

    fun updateDialogConfirmPassword(value: String) {
        _uiState.update { it.copy(dialogConfirmPassword = value, message = null) }
    }

    fun dismissPasswordDialog() {
        _uiState.update {
            it.copy(
                showPasswordDialog = false,
                dialogMode = null,
                dialogPassword = "",
                dialogConfirmPassword = "",
                pendingRestoreUri = null
            )
        }
    }

    fun confirmExportPassword(onReady: () -> Unit): Boolean {
        val state = _uiState.value
        if (!validateExportPassword(state.dialogPassword, state.dialogConfirmPassword)) {
            return false
        }
        pendingExportPassword = state.dialogPassword
        _uiState.update {
            it.copy(
                showPasswordDialog = false,
                dialogMode = null,
                dialogPassword = "",
                dialogConfirmPassword = ""
            )
        }
        onReady()
        return true
    }

    fun confirmRestorePassword(contentResolver: ContentResolver) {
        val state = _uiState.value
        if (state.dialogPassword.isBlank()) {
            _uiState.update { it.copy(message = "请输入导出时设置的备份密码") }
            return
        }
        val uri = state.pendingRestoreUri
        if (uri == null) {
            dismissPasswordDialog()
            return
        }
        val password = state.dialogPassword
        _uiState.update {
            it.copy(
                showPasswordDialog = false,
                dialogMode = null,
                dialogPassword = "",
                dialogConfirmPassword = "",
                pendingRestoreUri = null
            )
        }
        restore(contentResolver, uri, password)
    }

    fun consumeExportPassword(): String? {
        val password = pendingExportPassword
        pendingExportPassword = null
        return password
    }

    fun export(contentResolver: ContentResolver, uri: Uri, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, message = null) }
            runCatching {
                backupManager.exportBackup(contentResolver, uri, password)
            }.onSuccess {
                _uiState.update { it.copy(isWorking = false, message = "导出成功") }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isWorking = false, message = error.message ?: "导出失败")
                }
            }
        }
    }

    private fun restore(contentResolver: ContentResolver, uri: Uri, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, message = null, restoreSuccess = false) }
            runCatching {
                backupManager.importBackup(contentResolver, uri, password)
            }.onSuccess {
                _uiState.update {
                    it.copy(isWorking = false, message = "恢复成功", restoreSuccess = true)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isWorking = false,
                        message = error.message ?: "恢复失败，请检查密码是否正确",
                        restoreSuccess = false
                    )
                }
            }
        }
    }

    private fun validateExportPassword(password: String, confirmPassword: String): Boolean {
        if (password.length < 6) {
            _uiState.update { it.copy(message = "密码至少6位") }
            return false
        }
        if (password != confirmPassword) {
            _uiState.update { it.copy(message = "两次密码不一致") }
            return false
        }
        return true
    }
}
