package com.example.ims_app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ims_app.data.DemoRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminControlsScreen(repository: DemoRepository) {
    val batches = repository.attendanceBatches()
    val courses = repository.courses()
    val canManageCatalog = repository.canManageAcademicCatalog()
    val canManageBatches = repository.canManageAcademicBatches()
    val canManageTransfers = repository.canManageBatchTransfers()

    var message by remember { mutableStateOf("") }

    // Course state
    var editingCourseId by remember { mutableStateOf<Int?>(null) }
    var courseCode by remember { mutableStateOf("") }
    var courseName by remember { mutableStateOf("") }

    // Batch state
    var newBatchName by remember { mutableStateOf("") }
    var editingBatchName by remember { mutableStateOf<String?>(null) }

    // Subject state
    var selectedSubjectBatch by remember { mutableStateOf(batches.firstOrNull().orEmpty()) }
    var subjectName by remember { mutableStateOf("") }
    var subjectElective by remember { mutableStateOf(false) }

    // Transfer state
    var sourceBatch by remember { mutableStateOf(batches.firstOrNull().orEmpty()) }
    var targetBatch by remember { mutableStateOf(batches.drop(1).firstOrNull() ?: batches.firstOrNull().orEmpty()) }
    var transferReason by remember { mutableStateOf("") }
    val selectedRollNos = remember { mutableStateListOf<String>() }

    LaunchedEffect(batches) {
        if (batches.isEmpty()) return@LaunchedEffect
        if (selectedSubjectBatch !in batches) selectedSubjectBatch = batches.first()
        if (sourceBatch !in batches) sourceBatch = batches.first()
        val targetCandidates = batches.filterNot { it == sourceBatch }
        if (targetBatch !in targetCandidates) targetBatch = targetCandidates.firstOrNull().orEmpty()
    }

    val sourceStudents = repository.studentsForBatch(sourceBatch)
    LaunchedEffect(sourceBatch, sourceStudents.size) {
        selectedRollNos.removeAll { selected -> sourceStudents.none { it.rollNo == selected } }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Status message ──────────────────────────────────────────────
        if (message.isNotBlank()) {
            item {
                ElevatedCard {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // ── Course management ───────────────────────────────────────────
        item {
            SectionCard(title = "Course management") {
                OutlinedTextField(
                    value = courseCode,
                    onValueChange = { courseCode = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Course code") },
                    singleLine = true,
                    enabled = canManageCatalog,
                )
                OutlinedTextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Course name") },
                    singleLine = true,
                    enabled = canManageCatalog,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val error = if (editingCourseId == null) {
                                repository.addCourse(courseCode, courseName)
                            } else {
                                repository.updateCourse(editingCourseId ?: -1, courseCode, courseName)
                            }
                            message = error ?: "Course saved successfully."
                            if (error == null) {
                                editingCourseId = null
                                courseCode = ""
                                courseName = ""
                            }
                        },
                        enabled = canManageCatalog,
                    ) {
                        Text(if (editingCourseId == null) "Add course" else "Update course")
                    }
                    if (editingCourseId != null) {
                        OutlinedButton(
                            onClick = {
                                editingCourseId = null
                                courseCode = ""
                                courseName = ""
                            }
                        ) { Text("Cancel") }
                    }
                }

                if (courses.isNotEmpty()) {
                    HorizontalDivider()
                    courses.forEach { course ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Name wraps; buttons appear below to avoid clipping
                            Text(
                                text = "${course.code} — ${course.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (canManageCatalog) {
                                Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                                    TextButton(onClick = {
                                        editingCourseId = course.id
                                        courseCode = course.code
                                        courseName = course.name
                                    }) { Text("Edit") }
                                    TextButton(onClick = {
                                        message = repository.deleteCourse(course.id) ?: "Course deleted."
                                    }) { Text("Delete") }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Batch management ────────────────────────────────────────────
        item {
            SectionCard(title = "Batch management") {
                OutlinedTextField(
                    value = newBatchName,
                    onValueChange = { newBatchName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (editingBatchName == null) "New batch name" else "Rename batch") },
                    singleLine = true,
                    enabled = canManageBatches,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (editingBatchName != null) {
                                val error = repository.renameAcademicBatch(editingBatchName!!, newBatchName)
                                message = error ?: "Batch renamed."
                                if (error == null) {
                                    editingBatchName = null
                                    newBatchName = ""
                                }
                            } else {
                                message = repository.addAcademicBatch(newBatchName) ?: "Batch added."
                                if (message == "Batch added.") newBatchName = ""
                            }
                        },
                        enabled = canManageBatches,
                    ) { Text(if (editingBatchName == null) "Add batch" else "Update batch") }
                    if (editingBatchName != null) {
                        OutlinedButton(
                            onClick = {
                                editingBatchName = null
                                newBatchName = ""
                            }
                        ) { Text("Cancel") }
                    }
                }

                if (batches.isNotEmpty()) {
                    HorizontalDivider()
                    batches.forEach { batch ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = batch,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (canManageBatches) {
                                Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                                    TextButton(onClick = {
                                        editingBatchName = batch
                                        newBatchName = batch
                                    }) { Text("Edit") }
                                    TextButton(onClick = {
                                        message = repository.removeAcademicBatch(batch) ?: "Batch removed."
                                    }) { Text("Remove") }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Subjects and electives ──────────────────────────────────────
        item {
            SectionCard(title = "Subjects and electives") {
                if (batches.isEmpty()) {
                    Text("No batches available.", style = MaterialTheme.typography.bodySmall)
                } else {
                    EnumDropdownField(
                        label = "Batch",
                        selectedOption = selectedSubjectBatch,
                        options = batches,
                        optionLabel = { it },
                        onOptionSelected = { selectedSubjectBatch = it },
                        enabled = canManageCatalog,
                    )
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Subject name") },
                        singleLine = true,
                        enabled = canManageCatalog,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mark as elective", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = subjectElective,
                            onCheckedChange = { subjectElective = it },
                            enabled = canManageCatalog,
                        )
                    }
                    Button(
                        onClick = {
                            message = repository.addManagedSubject(selectedSubjectBatch, subjectName, subjectElective) ?: "Subject added."
                            if (message == "Subject added.") {
                                subjectName = ""
                                subjectElective = false
                            }
                        },
                        enabled = canManageCatalog,
                    ) { Text("Add subject") }

                    val managedSubjects = repository.managedSubjects(selectedSubjectBatch)
                    if (managedSubjects.isNotEmpty()) {
                        HorizontalDivider()
                        managedSubjects.forEach { managed ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = if (managed.isElective) "${managed.name} (Elective)" else managed.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (canManageCatalog) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                                        TextButton(onClick = {
                                            message = repository.updateManagedSubject(
                                                selectedSubjectBatch,
                                                managed.name,
                                                managed.name,
                                                !managed.isElective,
                                            ) ?: "Subject updated."
                                        }) {
                                            Text(if (managed.isElective) "Make core" else "Make elective")
                                        }
                                        TextButton(onClick = {
                                            message = repository.deleteManagedSubject(selectedSubjectBatch, managed.name) ?: "Subject deleted."
                                        }) { Text("Delete") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Batch transfers ─────────────────────────────────────────────
        item {
            SectionCard(title = "Batch transfers") {
                if (!canManageTransfers || batches.size <= 1) {
                    Text(
                        "Need at least two batches to perform a transfer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    EnumDropdownField(
                        label = "From batch",
                        selectedOption = sourceBatch,
                        options = batches,
                        optionLabel = { it },
                        onOptionSelected = {
                            sourceBatch = it
                            val targetCandidates = batches.filterNot { batch -> batch == sourceBatch }
                            if (targetBatch !in targetCandidates) {
                                targetBatch = targetCandidates.firstOrNull().orEmpty()
                            }
                        }
                    )
                    EnumDropdownField(
                        label = "To batch",
                        selectedOption = targetBatch,
                        options = batches.filterNot { it == sourceBatch },
                        optionLabel = { it },
                        onOptionSelected = { targetBatch = it }
                    )
                    OutlinedTextField(
                        value = transferReason,
                        onValueChange = { transferReason = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Transfer reason") },
                        singleLine = true,
                    )

                    if (sourceStudents.isEmpty()) {
                        Text(
                            "No students in selected source batch.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text("Select students to transfer", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(4.dp))
                        sourceStudents.forEach { student ->
                            FilterChip(
                                selected = selectedRollNos.contains(student.rollNo),
                                onClick = {
                                    if (selectedRollNos.contains(student.rollNo)) {
                                        selectedRollNos.remove(student.rollNo)
                                    } else {
                                        selectedRollNos.add(student.rollNo)
                                    }
                                },
                                label = { Text("${student.name} (${student.rollNo})") }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val error = repository.executeTransfer(
                                studentRollNos = selectedRollNos.toList(),
                                fromBatch = sourceBatch,
                                toBatch = targetBatch,
                                reason = transferReason,
                            )
                            message = error ?: "Transfer completed successfully."
                            if (error == null) {
                                selectedRollNos.clear()
                                transferReason = ""
                            }
                        },
                        enabled = selectedRollNos.isNotEmpty(),
                    ) {
                        Text(
                            if (selectedRollNos.size > 1)
                                "Transfer ${selectedRollNos.size} students"
                            else
                                "Transfer student"
                        )
                    }

                    // Recent transfer history
                    val history = repository.transferHistory().take(5)
                    if (history.isNotEmpty()) {
                        HorizontalDivider()
                        Text("Recent transfers", style = MaterialTheme.typography.labelMedium)
                        history.forEach { log ->
                            Text(
                                text = "${log.studentRollNo}: ${log.fromBatch} → ${log.toBatch}  (${log.approvedAt})",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            HorizontalDivider()
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumDropdownField(
    label: String,
    selectedOption: T,
    options: List<T>,
    optionLabel: (T) -> String,
    onOptionSelected: (T) -> Unit,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }

    androidx.compose.material3.ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = optionLabel(selectedOption),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            label = { Text(label) },
            trailingIcon = {
                androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            singleLine = true
        )
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
