package com.watcher.app.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.watcher.app.ui.WatcherViewModel
import com.watcher.app.ui.screens.AddMonitorScreen
import com.watcher.app.ui.screens.HistoryScreen
import com.watcher.app.ui.screens.MonitorDetailScreen
import com.watcher.app.ui.screens.MonitorListScreen
import com.watcher.app.ui.screens.SettingsScreen

private object Routes {
    const val MONITORS = "monitors"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val ADD_MONITOR = "add_monitor"
    const val MONITOR_DETAIL = "monitor_detail/{id}"
    fun monitorDetail(id: String) = "monitor_detail/$id"
}

private data class BottomTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomTabs = listOf(
    BottomTab(Routes.MONITORS, "Monitors", Icons.Filled.List),
    BottomTab(Routes.HISTORY, "History", Icons.Filled.History),
    BottomTab(Routes.SETTINGS, "Settings", Icons.Filled.Settings),
)

@Composable
fun WatcherNavHost(viewModel: WatcherViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomTabs.map { it.route }) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.MONITORS,
            modifier = androidx.compose.ui.Modifier.padding(bottom = padding.calculateBottomPadding()),
        ) {
            composable(Routes.MONITORS) {
                MonitorListScreen(
                    viewModel = viewModel,
                    onAddMonitor = { navController.navigate(Routes.ADD_MONITOR) },
                    onOpenMonitor = { id -> navController.navigate(Routes.monitorDetail(id)) },
                )
            }
            composable(Routes.HISTORY) { HistoryScreen(viewModel) }
            composable(Routes.SETTINGS) { SettingsScreen(viewModel) }
            composable(Routes.ADD_MONITOR) {
                AddMonitorScreen(
                    viewModel = viewModel,
                    onDone = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                )
            }
            composable(
                Routes.MONITOR_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { backStack ->
                val id = backStack.arguments?.getString("id") ?: return@composable
                MonitorDetailScreen(viewModel = viewModel, monitorId = id, onBack = { navController.popBackStack() })
            }
        }
    }
}
