package com.example.ims_app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ims_app.data.AttendanceEntry
import com.example.ims_app.data.AttendanceStatus
import com.example.ims_app.data.DemoRepository

@Composable
fun AttendanceScreen(repository: DemoRepository) {
    val currentSheet = repository.activeAttendanceSheet()
    val batchOptions = listOf("B.Tech CSE - Sem 4", "B.Tech CSE - Sem 6", "BBA - Sem 2")
    val dateOptions = listOf("10 Apr 2026", "11 Apr 2026", "12 Apr 2026")
    val selectedStatuses = remember(currentSheet.id) {
        mutableStateMapOf<Int, AttendanceStatus>().apply {
            currentSheet.entries.forEach { put(it.id, it.status) }
        }
    }
    var note by rememberSaveable(currentSheet.id) { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Attendance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Mark the batch, save the sheet, and review the recorded statuses.", style = MaterialTheme.typography.bodyMedium)
        }

        item {
            ElevatedCard {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select batch", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        batchOptions.forEach { batch ->
                            FilterChip(
                                selected = repository.selectedBatch == batch,
                                onClick = { repository.selectedBatch = batch },
                                label = { Text(batch) }
                            )
                        }
                    }
                    Text("Select date", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        dateOptions.forEach { date ->
                            FilterChip(
                                selected = repository.selectedDate == date,
                                onClick = { repository.selectedDate = date },
                                label = { Text(date) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = {}, label = { Text(currentSheet.batch) })
                        AssistChip(onClick = {}, label = { Text(currentSheet.dateLabel) })
                    }
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Session note") }
                    )
                }
            }
        }

        items(currentSheet.entries) { entry ->
            AttendanceRow(
                entry = entry,
                selectedStatus = selectedStatuses[entry.id] ?: entry.status,
                onStatusChange = { selectedStatuses[entry.id] = it }
            )
        }

        item {
            Button(
                onClick = {
                    repository.upsertAttendanceSheet(
                        currentSheet.copy(
                            entries = currentSheet.entries.map { it.copy(status = selectedStatuses[it.id] ?: it.status) }
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save attendance")
            }
        }

        item {
            ElevatedCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(summaryText(currentSheet.entries.map { selectedStatuses[it.id] ?: it.status }))
                }
            }
        }
    }
}

@Composable
private fun AttendanceRow(
    entry: AttendanceEntry,
    selectedStatus: AttendanceStatus,
    onStatusChange: (AttendanceStatus) -> Unit,
) {
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(entry.studentName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(entry.rollNo, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AttendanceStatus.values().forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { onStatusChange(status) },
                        label = { Text(status.label) }
                    )
                }
            }
        }
    }
}

private fun summaryText(statuses: List<AttendanceStatus>): String {
    val present = statuses.count { it == AttendanceStatus.Present }
    val absent = statuses.count { it == AttendanceStatus.Absent }
    val leave = statuses.count { it == AttendanceStatus.Leave }
    return "Present: $present, Absent: $absent, Leave: $leave"
}
