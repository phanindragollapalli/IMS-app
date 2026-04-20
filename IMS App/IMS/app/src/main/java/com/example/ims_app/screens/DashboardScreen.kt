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
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ims_app.data.DashboardMetric
import com.example.ims_app.data.DemoRepository
import com.example.ims_app.data.UserRole

@Composable
fun DashboardScreen(
    repository: DemoRepository,
    onNavigateToTimetable: () -> Unit,
    onNavigateToAttendance: () -> Unit,
) {
    val metrics = repository.dashboardMetrics()

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
            SectionHeader(title = "Quick access", subtitle = "Launch your module workflows")
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionCard(
                    title = "Time Table",
                    subtitle = when (repository.activeRole) {
                        UserRole.Student -> "View your timetable by day and slot"
                        UserRole.Faculty -> "Update your timetable and check conflicts"
                        UserRole.Admin -> "Manage all timetable entries"
                    },
                    actionText = "Open timetable",
                    onClick = onNavigateToTimetable
                )
                ActionCard(
                    title = "Attendance",
                    subtitle = when (repository.activeRole) {
                        UserRole.Student -> "View your personal attendance reports"
                        UserRole.Faculty -> "Mark attendance and generate reports"
                        UserRole.Admin -> "Manage attendance data and reports"
                    },
                    actionText = "Open attendance",
                    onClick = onNavigateToAttendance
                )
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
private fun ActionCard(title: String, subtitle: String, actionText: String, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {
                Text(actionText.take(1), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
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
