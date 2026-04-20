package com.example.ims_app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ims_app.data.DashboardMetric
import com.example.ims_app.data.DemoRepository
import com.example.ims_app.data.UserRole

private data class ModuleItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
)

@Composable
fun DashboardScreen(
    repository: DemoRepository,
    onNavigateToTimetable: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToAdmission: () -> Unit,
    onNavigateToStudentDetails: () -> Unit,
    onNavigateToExaminations: () -> Unit,
    onNavigateToManageUsers: () -> Unit,
    onNavigateToHumanResources: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToManageNews: () -> Unit,
) {
    val metrics = repository.dashboardMetrics()

    val modules = listOf(
        ModuleItem("Time Table", Icons.Filled.CalendarMonth, "timetable"),
        ModuleItem("Attendance", Icons.Filled.CheckCircle, "attendance"),
        ModuleItem("Admission", Icons.Filled.PersonAdd, "admission"),
        ModuleItem("Student Details", Icons.Filled.PersonSearch, "student_details"),
        ModuleItem("Examinations", Icons.Filled.Quiz, "examinations"),
        ModuleItem("Manage Users", Icons.Filled.People, "manage_users"),
        ModuleItem("Human Resources", Icons.Filled.Work, "human_resources"),
        ModuleItem("Finance", Icons.Filled.AccountBalance, "finance"),
        ModuleItem("Messages", Icons.Filled.Email, "messages"),
        ModuleItem("Manage News", Icons.Filled.Newspaper, "manage_news"),
    )

    val navigateMap = mapOf(
        "timetable" to onNavigateToTimetable,
        "attendance" to onNavigateToAttendance,
        "admission" to onNavigateToAdmission,
        "student_details" to onNavigateToStudentDetails,
        "examinations" to onNavigateToExaminations,
        "manage_users" to onNavigateToManageUsers,
        "human_resources" to onNavigateToHumanResources,
        "finance" to onNavigateToFinance,
        "messages" to onNavigateToMessages,
        "manage_news" to onNavigateToManageNews,
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ElevatedCard {
                Column(Modifier.padding(20.dp)) {
                    Text("Welcome back", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "Institute dashboard",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = repository.searchQuery,
                        onValueChange = { repository.searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        placeholder = { Text("Search modules, subjects, batches") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Logged in as: ${repository.currentUser?.displayName ?: "Guest"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Role: ${repository.activeRole.label}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                metrics.forEach { metric ->
                    MetricCard(metric = metric, modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            SectionHeader(title = "Utilities", subtitle = "Launch your module workflows")
        }

        // Filter modules by search query, then grid: 3 columns
        val query = repository.searchQuery.trim()
        val filtered = if (query.isEmpty()) modules else modules.filter {
            it.title.contains(query, ignoreCase = true)
        }
        val rows = filtered.chunked(3)
        items(rows) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { module ->
                    ModuleGridCard(
                        title = module.title,
                        icon = module.icon,
                        onClick = { navigateMap[module.route]?.invoke() },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Fill empty spots in the last row so weight is balanced
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            SectionHeader(title = "Today", subtitle = "Operational snapshot")
        }

        items(listOf(
            "Timetable conflicts are highlighted before save",
            "Attendance reports support daily/monthly/subject-wise filters",
            "Session remains valid for up to 3 days"
        )) { note ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(note, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
private fun ModuleGridCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        MaterialTheme.shapes.large
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MetricCard(metric: DashboardMetric, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(metric.title, style = MaterialTheme.typography.labelLarge)
            Text(metric.value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(metric.subtitle, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium)
    }
}
