package com.example.ims_app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ims_app.data.DemoRepository
import com.example.ims_app.data.ExamSession
import com.example.ims_app.data.ExamStatus

@Composable
fun ExaminationsScreen(repository: DemoRepository) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedExamId by remember { mutableStateOf<Int?>(null) }
    val exams = repository.filteredExams()

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Examinations", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Create, publish, and track exam sessions offline.", style = MaterialTheme.typography.bodyMedium)
            }

            item {
                ElevatedCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = repository.searchQuery,
                            onValueChange = { repository.searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Search exams") },
                            singleLine = true
                        )
                        Text("Role: ${repository.activeRole.label}", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            items(exams) { exam ->
                ExamCard(
                    exam = exam,
                    onSelect = { selectedExamId = exam.id },
                    onAdvanceStatus = { repository.updateExamStatus(exam.id) }
                )
            }
        }

        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier.padding(20.dp).fillMaxWidth(0.22f)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add exam")
        }
    }

    if (showCreateDialog) {
        CreateExamDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { subject, batch, dateLabel ->
                repository.addExam(subject, batch, dateLabel)
                showCreateDialog = false
            }
        )
    }

    selectedExamId?.let { id ->
        val exam = exams.firstOrNull { it.id == id } ?: return
        AlertDialog(
            onDismissRequest = { selectedExamId = null },
            confirmButton = {
                TextButton(onClick = { selectedExamId = null }) { Text("Close") }
            },
            title = { Text(exam.subject) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Batch: ${exam.batch}")
                    Text("Date: ${exam.dateLabel}")
                    Text("Marks: ${exam.marks}")
                    Text("Status: ${exam.status.label}")
                }
            }
        )
    }
}

@Composable
private fun ExamCard(exam: ExamSession, onSelect: () -> Unit, onAdvanceStatus: () -> Unit) {
    ElevatedCard(onClick = onSelect) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(exam.subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${exam.batch} • ${exam.dateLabel}", style = MaterialTheme.typography.bodyMedium)
            AssistChip(onClick = onAdvanceStatus, label = { Text(exam.status.label) })
            Button(onClick = onAdvanceStatus) {
                Text(nextStatusLabel(exam.status))
            }
        }
    }
}

@Composable
private fun CreateExamDialog(onDismiss: () -> Unit, onCreate: (String, String, String) -> Unit) {
    var subject by remember { mutableStateOf("") }
    var batch by remember { mutableStateOf("CSE A") }
    var dateLabel by remember { mutableStateOf("15 Apr 2026") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onCreate(subject.ifBlank { "New Exam" }, batch, dateLabel) }) { Text("Create") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Create exam") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") })
                OutlinedTextField(value = batch, onValueChange = { batch = it }, label = { Text("Batch") })
                OutlinedTextField(value = dateLabel, onValueChange = { dateLabel = it }, label = { Text("Date") })
            }
        }
    )
}

private fun nextStatusLabel(status: ExamStatus): String = when (status) {
    ExamStatus.Draft -> "Schedule"
    ExamStatus.Scheduled -> "Publish"
    ExamStatus.Published -> "Complete"
    ExamStatus.Completed -> "Completed"
}
