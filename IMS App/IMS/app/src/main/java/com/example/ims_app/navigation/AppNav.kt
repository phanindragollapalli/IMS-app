package com.example.ims_app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
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
import com.example.ims_app.screens.StubModuleScreen
import com.example.ims_app.screens.TimetableScreen

object Routes {
    const val Dashboard = "dashboard"
    const val Timetable = "timetable"
    const val Attendance = "attendance"
    const val AdminControls = "admin_controls"
    const val Settings = "settings"
    const val Admission = "admission"
    const val StudentDetails = "student_details"
    const val Examinations = "examinations"
    const val ManageUsers = "manage_users"
    const val HumanResources = "human_resources"
    const val Finance = "finance"
    const val Messages = "messages"
    const val ManageNews = "manage_news"
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
                            Routes.Admission -> "Student Admission"
                            Routes.StudentDetails -> "Student Details"
                            Routes.Examinations -> "Examinations"
                            Routes.ManageUsers -> "Manage Users"
                            Routes.HumanResources -> "Human Resources"
                            Routes.Finance -> "Finance"
                            Routes.Messages -> "Messages"
                            Routes.ManageNews -> "Manage News"
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
                    onNavigateToAdmission = { navController.navigateSingleTop(Routes.Admission) },
                    onNavigateToStudentDetails = { navController.navigateSingleTop(Routes.StudentDetails) },
                    onNavigateToExaminations = { navController.navigateSingleTop(Routes.Examinations) },
                    onNavigateToManageUsers = { navController.navigateSingleTop(Routes.ManageUsers) },
                    onNavigateToHumanResources = { navController.navigateSingleTop(Routes.HumanResources) },
                    onNavigateToFinance = { navController.navigateSingleTop(Routes.Finance) },
                    onNavigateToMessages = { navController.navigateSingleTop(Routes.Messages) },
                    onNavigateToManageNews = { navController.navigateSingleTop(Routes.ManageNews) },
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
            composable(Routes.Admission) {
                StubModuleScreen(
                    title = "Student Admission",
                    icon = Icons.Filled.PersonAdd,
                    description = "Manage student admissions with unique ID generation, customizable forms, guardian details, education history, and photo uploads.",
                    capabilities = listOf(
                        "Unique ID generation for all students",
                        "Comprehensive and customizable admission forms",
                        "Add multiple guardians and emergency contacts",
                        "Record previous education history",
                        "Upload student photos",
                        "Fully customizable to meet specific school standards"
                    )
                )
            }
            composable(Routes.StudentDetails) {
                StubModuleScreen(
                    title = "Student Details",
                    icon = Icons.Filled.PersonSearch,
                    description = "View and search student records by batch, with advanced filters for specific data retrieval across current and former students.",
                    capabilities = listOf(
                        "Normal student view based on batches",
                        "Search existing and former students",
                        "Advanced search with a large number of filters",
                        "Specific data retrieval per student"
                    )
                )
            }
            composable(Routes.Examinations) {
                StubModuleScreen(
                    title = "Examinations",
                    icon = Icons.Filled.Quiz,
                    description = "Create and manage exams with multiple grading types, automated reports, statistical/graphical views, and support for GPA, CCE, and CWA evaluation.",
                    capabilities = listOf(
                        "Create exams based on grades, marks, or custom types",
                        "Group exams as needed",
                        "Extensive Report Center with automated reports",
                        "Statistical and graphical views (charts)",
                        "Support for GPA, CCE, and CWA evaluation methods"
                    )
                )
            }
            composable(Routes.ManageUsers) {
                StubModuleScreen(
                    title = "Manage Users",
                    icon = Icons.Filled.People,
                    description = "Search, view, and edit user profiles and privileges with role-based access control across the institute.",
                    capabilities = listOf(
                        "Global search for any user via the dashboard search bar",
                        "View and edit user profiles and passwords",
                        "Set specific privileges based on roles",
                        "Role-based access control for organizational responsibility"
                    )
                )
            }
            composable(Routes.HumanResources) {
                StubModuleScreen(
                    title = "Human Resources",
                    icon = Icons.Filled.Work,
                    description = "End-to-end employee management covering admission, payroll, leave management, and advanced employee search.",
                    capabilities = listOf(
                        "End-to-end employee management from admission to exit",
                        "Customizable employee admission and payroll forms",
                        "One-click payslip approval/rejection",
                        "Efficient leave management system",
                        "Advanced employee search"
                    )
                )
            }
            composable(Routes.Finance) {
                StubModuleScreen(
                    title = "Finance",
                    icon = Icons.Filled.AccountBalance,
                    description = "Comprehensive financial management including fee classification, expense tracking, donation management, and payslip integration.",
                    capabilities = listOf(
                        "Fee classification and separate fee collection date design",
                        "Fee defaulter analysis",
                        "Easy fee submission and instant payment processing",
                        "Track expenses, incomes, assets, liabilities, and donations",
                        "Automatic transaction facilities",
                        "Integration with the payslip system",
                        "Assign specific tutors to batches for financial tracking"
                    )
                )
            }
            composable(Routes.Messages) {
                StubModuleScreen(
                    title = "Messages",
                    icon = Icons.Filled.Email,
                    description = "Inbuilt messaging system for administration, teachers, students, and parents with broadcast capabilities.",
                    capabilities = listOf(
                        "Messaging for administration, teachers, students, and parents",
                        "Record all communications with students",
                        "Broadcast school events, news, and holidays"
                    )
                )
            }
            composable(Routes.ManageNews) {
                StubModuleScreen(
                    title = "Manage News",
                    icon = Icons.Filled.Newspaper,
                    description = "Create, edit, and manage news in rich text format with commenting and moderation tools.",
                    capabilities = listOf(
                        "Create, edit, and search news using rich text format (RTF)",
                        "Users can comment on published news",
                        "Moderation tools to delete comments or news entries"
                    )
                )
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
