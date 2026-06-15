package com.joker.homeledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.joker.homeledger.core.database.DatabaseSeeder
import com.joker.homeledger.core.security.AppLockManager
import com.joker.homeledger.feature.account.AccountManageScreen
import com.joker.homeledger.feature.backup.BackupScreen
import com.joker.homeledger.feature.category.CategoryManageScreen
import com.joker.homeledger.feature.entry.EntryScreen
import com.joker.homeledger.feature.home.HomeScreen
import com.joker.homeledger.feature.ledger.LedgerScreen
import com.joker.homeledger.feature.ledger.TransactionDetailScreen
import com.joker.homeledger.feature.lock.AppLockSetupScreen
import com.joker.homeledger.feature.lock.UnlockScreen
import com.joker.homeledger.feature.settings.SettingsScreen
import com.joker.homeledger.feature.stats.StatsScreen
import com.joker.homeledger.navigation.AppRoutes
import com.joker.homeledger.ui.theme.HomeLedgerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var databaseSeeder: DatabaseSeeder

    @Inject
    lateinit var appLockManager: AppLockManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            databaseSeeder.seedIfNeeded(System.currentTimeMillis())
        }
        setContent {
            HomeLedgerTheme {
                AppShell(appLockManager = appLockManager)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        appLockManager.markBackgroundAt()
    }
}

@Composable
private fun AppShell(appLockManager: AppLockManager) {
    var locked by remember {
        mutableStateOf(appLockManager.isEnabled())
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, appLockManager) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                locked = appLockManager.isEnabled() && appLockManager.shouldRequireUnlock()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (locked) {
        UnlockScreen(
            appLockManager = appLockManager,
            onUnlocked = {
                appLockManager.markUnlocked()
                locked = false
            }
        )
    } else {
        HomeLedgerAppRoot()
    }
}

@Composable
private fun HomeLedgerAppRoot() {
    val navController = rememberNavController()
    val tabs = BottomTab.entries.toList()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in tabs.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val currentDestination = navBackStackEntry?.destination
                    tabs.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomTab.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomTab.Home.route) {
                HomeScreen(
                    onAddEntryClick = { navController.navigate(AppRoutes.entry()) },
                    onViewAllLedger = {
                        navController.navigate(BottomTab.Ledger.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onTransactionClick = { id ->
                        navController.navigate(AppRoutes.ledgerDetail(id))
                    }
                )
            }
            composable(BottomTab.Ledger.route) {
                LedgerScreen(
                    onTransactionClick = { id ->
                        navController.navigate(AppRoutes.ledgerDetail(id))
                    }
                )
            }
            composable(BottomTab.Stats.route) {
                StatsScreen(
                    onAddEntryClick = { navController.navigate(AppRoutes.entry()) },
                    onNavigateToLedger = {
                        navController.navigate(BottomTab.Ledger.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(BottomTab.Settings.route) {
                SettingsScreen(
                    onCategoryManage = { navController.navigate(AppRoutes.CATEGORY_MANAGE) },
                    onAccountManage = { navController.navigate(AppRoutes.ACCOUNT_MANAGE) },
                    onBackup = { navController.navigate(AppRoutes.BACKUP) },
                    onAppLock = { navController.navigate(AppRoutes.APP_LOCK) }
                )
            }
            composable(
                route = AppRoutes.ENTRY,
                arguments = listOf(
                    navArgument("transactionId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) {
                EntryScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = AppRoutes.LEDGER_DETAIL,
                arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
            ) {
                TransactionDetailScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(AppRoutes.entry(id)) }
                )
            }
            composable(AppRoutes.CATEGORY_MANAGE) {
                CategoryManageScreen(onBack = { navController.popBackStack() })
            }
            composable(AppRoutes.ACCOUNT_MANAGE) {
                AccountManageScreen(onBack = { navController.popBackStack() })
            }
            composable(AppRoutes.BACKUP) {
                BackupScreen(onBack = { navController.popBackStack() })
            }
            composable(AppRoutes.APP_LOCK) {
                AppLockSetupScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
