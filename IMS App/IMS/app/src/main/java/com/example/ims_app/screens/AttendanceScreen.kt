package com.example.ims_app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ims_app.data.AttendanceEntry
import com.example.ims_app.data.AttendanceReportType
import com.example.ims_app.data.AttendanceStatus
import com.example.ims_app.data.DemoRepository
import com.example.ims_app.data.UserRole

private enum class AttendanceMode {
    Mark,
    Reports
}

@Composable
fun AttendanceScreen(repository: DemoRepository) {
    val role = repository.activeRole
    val canManageAttendance = repository.canManageAttendance()
    val canManageMasterData = repository.canManageAttendanceMasterData()
    val currentSheet = repository.activeAttendanceSheet()

    var attendanceMode by remember { mutableStateOf(if (canManageAttendance) AttendanceMode.Mark else AttendanceMode.Reports) }
    var reportType by remember { mutableStateOf(AttendanceReportType.Daily) }
    var selectedMonth by remember { mutableStateOf(repository.attendanceMonths().firstOrNull().orEmpty()) }
    var reportBatchFilter by remember(role, repository.currentUser?.batch) {
        mutableStateOf(if (role == UserRole.Student) repository.currentUser?.batch else null)
    }
    var reportDateFilter by remember { mutableStateOf<String?>(null) }
    var reportSubjectFilter by remember { mutableStateOf<String?>(null) }

    var showAddBatchDialog by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showAddStudentDialog by remember { mutableStateOf(false) }

    val batchOptions = repository.attendanceBatches()
    val dateOptions = repository.attendanceDates().ifEmpty { listOf("10 Apr 2026", "11 Apr 2026", "12 Apr 2026") }
    val subjectOptions = repository.subjectsForBatch(repository.selectedBatch)

    val selectedStatuses = remember(currentSheet.id) {
        mutableStateMapOf<Int, AttendanceStatus>().apply {
            currentSheet.entries.forEach { put(it.id, it.status) }
        }
    }
    val selectedRemarks = remember(currentSheet.id) {
        mutableStateMapOf<Int, String>().apply {
            currentSheet.entries.forEach { put(it.id, it.remark) }
        }
    }
    var sessionNote by remember(currentSheet.id) { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Attendance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                when (role) {
                    UserRole.Student -> "View your personal attendance reports by filter."
                    UserRole.Faculty -> "Mark attendance with optional remarks and generate reports."
                    UserRole.Admin -> "Full attendance control with reports and data management."
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (canManageAttendance) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = attendanceMode == AttendanceMode.Mark,
                        onClick = { attendanceMode = AttendanceMode.Mark },
                        label = { Text("Mark") }
                    )
                    FilterChip(
                        selected = attendanceMode == AttendanceMode.Reports,
                        onClick = { attendanceMode = AttendanceMode.Reports },
                        label = { Text("Reports") }
                    )
                }
            }
        }

        if (attendanceMode == AttendanceMode.Mark && canManageAttendance) {
            item {
                ElevatedCard {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Manage attendance data", style = MaterialTheme.typography.labelLarge)
                        if (canManageMasterData) {
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(onClick = { showAddBatchDialog = true }) { Text("Add batch") }
                                Button(onClick = { showAddSubjectDialog = true }) { Text("Add subject") }
                                Button(onClick = { showAddStudentDialog = true }) { Text("Add student") }
                            }
                        } else {
                            Text(
                                "Only admin can add batches, subjects, and students.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text("Select batch", style = MaterialTheme.typography.labelLarge)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            batchOptions.forEach { batch ->
                                FilterChip(
                                    selected = repository.selectedBatch == batch,
                                    onClick = {
                                        repository.selectedBatch = batch
                                        repository.selectedSubject = repository.subjectsForBatch(batch).firstOrNull() ?: repository.selectedSubject
                                    },
                                    label = { Text(batch) }
                                )
                            }
                        }

                        Text("Select date", style = MaterialTheme.typography.labelLarge)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            dateOptions.forEach { date ->
                                FilterChip(
                                    selected = repository.selectedDate == date,
                                    onClick = { repository.selectedDate = date },
                                    label = { Text(date) }
                                )
                            }
                        }

                        Text("Select subject", style = MaterialTheme.typography.labelLarge)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            subjectOptions.forEach { subject ->
                                FilterChip(
                                    selected = repository.selectedSubject == subject,
                                    onClick = { repository.selectedSubject = subject },
                                    label = { Text(subject) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(onClick = {}, label = { Text(currentSheet.batch) })
                            AssistChip(onClick = {}, label = { Text(currentSheet.dateLabel) })
                            AssistChip(onClick = {}, label = { Text(currentSheet.subject) })
                        }

                        OutlinedTextField(
                            value = sessionNote,
                            onValueChange = { sessionNote = it },
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
                    remark = selectedRemarks[entry.id] ?: entry.remark,
                    editable = true,
                    onStatusChange = { selectedStatuses[entry.id] = it },
                    onRemarkChange = { selectedRemarks[entry.id] = it }
                )
            }

            item {
                Button(
                    onClick = {
                        repository.upsertAttendanceSheet(
                            currentSheet.copy(
                                entries = currentSheet.entries.map { existing ->
                                    existing.copy(
                                        status = selectedStatuses[existing.id] ?: existing.status,
                                        remark = selectedRemarks[existing.id] ?: existing.remark
                                    )
                                }
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save attendance")
                }
            }
        } else {
            item {
                ElevatedCard {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Report type", style = MaterialTheme.typography.labelLarge)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AttendanceReportType.values().forEach { type ->
                                FilterChip(
                                    selected = reportType == type,
                                    onClick = { reportType = type },
                                    label = { Text(type.label) }
                                )
                            }
                        }

                        if (role == UserRole.Student) {
                            Text("Batch: ${repository.currentUser?.batch ?: "Not assigned"}", style = MaterialTheme.typography.labelLarge)
                        } else {
                            Text("Filter batch", style = MaterialTheme.typography.labelLarge)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(selected = reportBatchFilter == null, onClick = { reportBatchFilter = null }, label = { Text("All") })
                                batchOptions.forEach { batch ->
                                    FilterChip(selected = reportBatchFilter == batch, onClick = { reportBatchFilter = batch }, label = { Text(batch) })
                                }
                            }
                        }

                        when (reportType) {
                            AttendanceReportType.Daily -> {
                                Text("Filter date", style = MaterialTheme.typography.labelLarge)
                                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(selected = reportDateFilter == null, onClick = { reportDateFilter = null }, label = { Text("All") })
                                    dateOptions.forEach { date ->
                                        FilterChip(selected = reportDateFilter == date, onClick = { reportDateFilter = date }, label = { Text(date) })
                                    }
                                }
                            }
                            AttendanceReportType.Monthly -> {
                                Text("Filter month", style = MaterialTheme.typography.labelLarge)
                                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    repository.attendanceMonths().forEach { month ->
                                        FilterChip(selected = selectedMonth == month, onClick = { selectedMonth = month }, label = { Text(month) })
                                    }
                                }
                            }
                            AttendanceReportType.SubjectWise -> {
                                Text("Filter subject", style = MaterialTheme.typography.labelLarge)
                                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(selected = reportSubjectFilter == null, onClick = { reportSubjectFilter = null }, label = { Text("All") })
                                    repository.attendanceSubjects().forEach { subject ->
                                        FilterChip(selected = reportSubjectFilter == subject, onClick = { reportSubjectFilter = subject }, label = { Text(subject) })
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val reportSheets = repository.attendanceSheetsFor(
                reportType = reportType,
                batch = if (role == UserRole.Student) repository.currentUser?.batch else reportBatchFilter,
                dateLabel = if (reportType == AttendanceReportType.Daily) reportDateFilter else null,
                monthLabel = if (reportType == AttendanceReportType.Monthly) selectedMonth else null,
                subject = if (reportType == AttendanceReportType.SubjectWise) reportSubjectFilter else null,
            )

            items(reportSheets) { sheet ->
                val reportEntries = if (role == UserRole.Student) {
                    val myRoll = repository.currentUser?.rollNo
                    sheet.entries.filter { it.rollNo == myRoll }
                } else {
                    sheet.entries
                }

                if (reportEntries.isNotEmpty()) {
                    ElevatedCard {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(sheet.subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("${sheet.batch} • ${sheet.dateLabel}")
                            reportEntries.forEach { entry ->
                                AttendanceRow(
                                    entry = entry,
                                    selectedStatus = entry.status,
                                    remark = entry.remark,
                                    editable = false,
                                    onStatusChange = {},
                                    onRemarkChange = {}
                                )
                            }
                        }
                    }
                }
            }

            item {
                val summary = repository.attendanceReportSummary(reportSheets)
                val attendancePercent = if (summary.totalStudents == 0) 0.0 else (summary.presentCount * 100.0 / summary.totalStudents)
                ElevatedCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Report summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (summary.totalSheets == 0) Text("No records match selected filters.")
                        Text("Sheets: ${summary.totalSheets}")
                        Text("Students: ${summary.totalStudents}")
                        Text("Present: ${summary.presentCount}, Absent: ${summary.absentCount}, Leave: ${summary.leaveCount}")
                        Text("Attendance rate: ${String.format("%.1f", attendancePercent)}%")
                    }
                }
            }
        }
    }

    if (canManageMasterData && showAddBatchDialog) {
        AddBatchDialog(
            onDismiss = { showAddBatchDialog = false },
            onAdd = { batch ->
                repository.addAttendanceBatch(batch)
                repository.selectedBatch = batch
                repository.selectedSubject = repository.subjectsForBatch(batch).firstOrNull() ?: repository.selectedSubject
                showAddBatchDialog = false
            }
        )
    }

    if (canManageMasterData && showAddSubjectDialog) {
        AddSubjectDialog(
            selectedBatch = repository.selectedBatch,
            batchOptions = repository.attendanceBatches(),
            onDismiss = { showAddSubjectDialog = false },
            onAdd = { batch, subject ->
                repository.addAttendanceSubject(batch, subject)
                repository.selectedBatch = batch
                repository.selectedSubject = subject
                showAddSubjectDialog = false
            }
        )
    }

    if (canManageMasterData && showAddStudentDialog) {
        AddStudentDialog(
            selectedBatch = repository.selectedBatch,
            batchOptions = repository.attendanceBatches(),
            onDismiss = { showAddStudentDialog = false },
            onAdd = { batch, name, rollNo ->
                repository.addAttendanceStudent(batch, name, rollNo)
                repository.selectedBatch = batch
                showAddStudentDialog = false
            }
        )
    }
}

@Composable
private fun AttendanceRow(
    entry: AttendanceEntry,
    selectedStatus: AttendanceStatus,
    remark: String,
    editable: Boolean,
    onStatusChange: (AttendanceStatus) -> Unit,
    onRemarkChange: (String) -> Unit,
) {
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(entry.studentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(entry.rollNo, style = MaterialTheme.typography.bodySmall)
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AttendanceStatus.values().forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { if (editable) onStatusChange(status) },
                        label = { Text(status.label) },
                        enabled = editable
                    )
                }
            }
            OutlinedTextField(
                value = remark,
                onValueChange = onRemarkChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Remark (optional)") },
                enabled = editable,
                singleLine = true
            )
        }
    }
}

@Composable
private fun AddBatchDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
) {
    var batchName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val normalized = batchName.trim()
                if (normalized.isNotBlank()) onAdd(normalized)
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Add batch") },
        text = {
            OutlinedTextField(
                value = batchName,
                onValueChange = { batchName = it },
                label = { Text("Batch name") },
                singleLine = true
            )
        }
    )
}

@Composable
private fun AddSubjectDialog(
    selectedBatch: String,
    batchOptions: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit,
) {
    var batchName by remember { mutableStateOf(selectedBatch) }
    var subjectName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val normalizedBatch = batchName.trim()
                val normalizedSubject = subjectName.trim()
                if (normalizedBatch.isNotBlank() && normalizedSubject.isNotBlank()) {
                    onAdd(normalizedBatch, normalizedSubject)
                }
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Add subject") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BatchDropdownField("Batch", batchOptions, batchName) { batchName = it }
                OutlinedTextField(value = subjectName, onValueChange = { subjectName = it }, label = { Text("Subject") }, singleLine = true)
            }
        }
    )
}

@Composable
private fun AddStudentDialog(
    selectedBatch: String,
    batchOptions: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit,
) {
    var batchName by remember { mutableStateOf(selectedBatch) }
    var studentName by remember { mutableStateOf("") }
    var rollNo by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val normalizedBatch = batchName.trim()
                val normalizedName = studentName.trim()
                val normalizedRoll = rollNo.trim()
                if (normalizedBatch.isNotBlank() && normalizedName.isNotBlank() && normalizedRoll.isNotBlank()) {
                    onAdd(normalizedBatch, normalizedName, normalizedRoll)
                }
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Add student") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BatchDropdownField("Batch", batchOptions, batchName) { batchName = it }
                OutlinedTextField(value = studentName, onValueChange = { studentName = it }, label = { Text("Student name") }, singleLine = true)
                OutlinedTextField(value = rollNo, onValueChange = { rollNo = it }, label = { Text("Roll number") }, singleLine = true)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchDropdownField(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            singleLine = true,
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onSelected(option)
                    expanded = false
                })
            }
        }
    }
}
