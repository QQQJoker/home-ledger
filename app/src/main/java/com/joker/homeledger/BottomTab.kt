package com.joker.homeledger

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomTab(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    Home(route = "home", labelRes = R.string.tab_home, icon = Icons.Outlined.Home),
    Ledger(route = "ledger", labelRes = R.string.tab_ledger, icon = Icons.Outlined.List),
    Stats(route = "stats", labelRes = R.string.tab_stats, icon = Icons.Outlined.Assessment),
    Settings(route = "settings", labelRes = R.string.tab_settings, icon = Icons.Outlined.Person)
}
