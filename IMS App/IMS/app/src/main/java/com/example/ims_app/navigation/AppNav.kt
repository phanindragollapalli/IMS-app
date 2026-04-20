package com.example.ims_app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ims_app.data.DemoRepository
import com.example.ims_app.data.UserRole
import com.example.ims_app.screens.AdminControlsScreen
import com.example.ims_app.screens.AttendanceScreen
import com.example.ims_app.screens.DashboardScreen
import com.example.ims_app.screens.SettingsScreen
import com.example.ims_app.screens.TimetableScreen

object Routes {
    const val Dashboard = "dashboard"
    const val Timetable = "timetable"
    const val Attendance = "attendance"
    const val AdminControls = "admin_controls"
    const val Settings = "settings"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImsAppNav(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.Dashboard

    val isAdmin = DemoRepository.activeRole == UserRole.Admin

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentRoute) {
                            Routes.AdminControls -> "Admin Controls"
                            Routes.Settings -> "Settings"
                            Routes.Timetable -> "Timetable"
                            Routes.Attendance -> "Attendance"
                            else -> "IMS Prototype \u2022 ${DemoRepository.activeRole.label}"
                        }
                    )
                },
                navigationIcon = {
                    if (currentRoute != Routes.Dashboard) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (isAdmin && currentRoute == Routes.Dashboard) {
                        IconButton(onClick = { navController.navigateSingleTop(Routes.AdminControls) }) {
                            Icon(
                                imageVector = Icons.Filled.AdminPanelSettings,
                                contentDescription = "Admin Controls"
                            )
                        }
                    }
                    if (currentRoute == Routes.Dashboard) {
                        IconButton(onClick = { navController.navigateSingleTop(Routes.Settings) }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    }
                    TextButton(onClick = onLogout) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.Dashboard,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.Dashboard) {
                DashboardScreen(
                    repository = DemoRepository,
                    onNavigateToTimetable = { navController.navigateSingleTop(Routes.Timetable) },
                    onNavigateToAttendance = { navController.navigateSingleTop(Routes.Attendance) },
                )
            }
            composable(Routes.Settings) {
                SettingsScreen(repository = DemoRepository)
            }
            composable(Routes.AdminControls) {
                AdminControlsScreen(repository = DemoRepository)
            }
            composable(Routes.Timetable) {
                TimetableScreen(repository = DemoRepository)
            }
            composable(Routes.Attendance) {
                AttendanceScreen(repository = DemoRepository)
            }
        }
    }
}

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
    }
}
