package com.example.ims_app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import com.example.ims_app.screens.AttendanceScreen
import com.example.ims_app.screens.DashboardScreen
import com.example.ims_app.screens.TimetableScreen

object Routes {
    const val Dashboard = "dashboard"
    const val Timetable = "timetable"
    const val Attendance = "attendance"
}

private data class NavItem(val route: String, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImsAppNav(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.Dashboard

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("IMS Prototype • ${DemoRepository.activeRole.label}") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = { navController.navigateSingleTop(item.route) },
                        icon = {
                            when (item.route) {
                                Routes.Dashboard -> Icon(Icons.Filled.Dashboard, contentDescription = item.label)
                                Routes.Timetable -> Icon(Icons.AutoMirrored.Filled.EventNote, contentDescription = item.label)
                                else -> Icon(Icons.Filled.PeopleAlt, contentDescription = item.label)
                            }
                        },
                        label = { Text(item.label) }
                    )
                }
            }
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
                    onNavigateToAttendance = { navController.navigateSingleTop(Routes.Attendance) }
                )
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

private val navItems = listOf(
    NavItem(Routes.Dashboard, "Dashboard"),
    NavItem(Routes.Timetable, "Timetable"),
    NavItem(Routes.Attendance, "Attendance")
)

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
    }
}
