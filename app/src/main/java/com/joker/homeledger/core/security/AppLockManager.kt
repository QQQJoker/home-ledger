package com.joker.homeledger.core.security

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(): Boolean = getPin().isNotBlank()

    fun setPin(pin: String) {
        prefs.edit().putString(KEY_PIN, pin).apply()
    }

    fun clearPin() {
        prefs.edit().remove(KEY_PIN).apply()
    }

    fun verifyPin(pin: String): Boolean = pin == getPin()

    fun getPin(): String = prefs.getString(KEY_PIN, "") ?: ""

    fun getTimeoutMinutes(): Int = prefs.getInt(KEY_TIMEOUT_MINUTES, 5)

    fun setTimeoutMinutes(value: Int) {
        prefs.edit().putInt(KEY_TIMEOUT_MINUTES, value.coerceIn(1, 60)).apply()
    }

    fun markBackgroundAt(now: Long = System.currentTimeMillis()) {
        prefs.edit().putLong(KEY_LAST_BACKGROUND_AT, now).apply()
    }

    fun markUnlocked() {
        prefs.edit().putLong(KEY_LAST_BACKGROUND_AT, -1L).apply()
    }

    fun shouldRequireUnlock(now: Long = System.currentTimeMillis()): Boolean {
        if (!isEnabled()) return false
        val backgroundAt = prefs.getLong(KEY_LAST_BACKGROUND_AT, -1L)
        if (backgroundAt <= 0L) return false
        val timeoutMillis = getTimeoutMinutes() * 60_000L
        return now - backgroundAt >= timeoutMillis
    }

    companion object {
        private const val PREFS_NAME = "home_ledger_lock"
        private const val KEY_PIN = "pin_code"
        private const val KEY_TIMEOUT_MINUTES = "timeout_minutes"
        private const val KEY_LAST_BACKGROUND_AT = "last_background_at"
    }
}
