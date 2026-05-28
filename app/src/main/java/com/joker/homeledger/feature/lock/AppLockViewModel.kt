package com.joker.homeledger.feature.lock

import androidx.lifecycle.ViewModel
import com.joker.homeledger.core.security.AppLockManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AppLockSetupUiState(
    val pin: String = "",
    val confirmPin: String = "",
    val timeoutMinutes: Int = 5,
    val enabled: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val appLockManager: AppLockManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AppLockSetupUiState(
            enabled = appLockManager.isEnabled(),
            timeoutMinutes = appLockManager.getTimeoutMinutes()
        )
    )
    val uiState: StateFlow<AppLockSetupUiState> = _uiState.asStateFlow()

    fun updatePin(value: String) {
        if (!value.all { it.isDigit() } || value.length > 6) return
        _uiState.update { it.copy(pin = value, message = null) }
    }

    fun updateConfirmPin(value: String) {
        if (!value.all { it.isDigit() } || value.length > 6) return
        _uiState.update { it.copy(confirmPin = value, message = null) }
    }

    fun updateTimeout(minutes: Int) {
        _uiState.update { it.copy(timeoutMinutes = minutes.coerceIn(1, 60)) }
    }

    fun enableLock() {
        val state = _uiState.value
        if (state.pin.length < 4) {
            _uiState.update { it.copy(message = "PIN 至少4位") }
            return
        }
        if (state.pin != state.confirmPin) {
            _uiState.update { it.copy(message = "两次 PIN 不一致") }
            return
        }
        appLockManager.setPin(state.pin)
        appLockManager.setTimeoutMinutes(state.timeoutMinutes)
        appLockManager.markUnlocked()
        _uiState.update { it.copy(enabled = true, message = "应用锁已开启") }
    }

    fun disableLock() {
        appLockManager.clearPin()
        _uiState.update {
            it.copy(
                enabled = false,
                pin = "",
                confirmPin = "",
                message = "应用锁已关闭"
            )
        }
    }
}
