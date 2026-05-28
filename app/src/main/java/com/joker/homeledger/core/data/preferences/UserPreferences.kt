package com.joker.homeledger.core.data.preferences

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLastAccountId(): Long? {
        val value = prefs.getLong(KEY_LAST_ACCOUNT_ID, -1L)
        return if (value < 0L) null else value
    }

    fun setLastAccountId(accountId: Long) {
        prefs.edit().putLong(KEY_LAST_ACCOUNT_ID, accountId).apply()
    }

    companion object {
        private const val PREFS_NAME = "home_ledger_prefs"
        private const val KEY_LAST_ACCOUNT_ID = "last_account_id"
    }
}
