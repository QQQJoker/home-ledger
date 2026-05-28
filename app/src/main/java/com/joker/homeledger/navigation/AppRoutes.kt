package com.joker.homeledger.navigation

object AppRoutes {
    const val ENTRY = "entry?transactionId={transactionId}"
    const val LEDGER_DETAIL = "ledger/detail/{transactionId}"
    const val CATEGORY_MANAGE = "settings/categories"
    const val ACCOUNT_MANAGE = "settings/accounts"
    const val BACKUP = "settings/backup"
    const val APP_LOCK = "settings/lock"
    const val UNLOCK = "unlock"

    fun entry(transactionId: Long = -1L): String = "entry?transactionId=$transactionId"
    fun ledgerDetail(transactionId: Long): String = "ledger/detail/$transactionId"
}
