package com.example.ims_app.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ims_app.data.AttendanceEntry
import com.example.ims_app.data.AttendanceReportType
import com.example.ims_app.data.AttendanceSheet
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

    val currentSheet = repository.activeAttendanceSheet()

    var attendanceMode by remember { mutableStateOf(if (canManageAttendance) AttendanceMode.Mark else AttendanceMode.Reports) }
    var reportType by remember { mutableStateOf(AttendanceReportType.Daily) }
    var selectedMonth by remember { mutableStateOf(repository.attendanceMonths().firstOrNull().orEmpty()) }
    var reportBatchFilter by remember(role, repository.currentUser?.batch) {
        mutableStateOf(if (role == UserRole.Student) repository.currentUser?.batch else null)
    }
    var reportDateFilter by remember { mutableStateOf<String?>(null) }
    var reportSubjectFilter by remember { mutableStateOf<String?>(null) }


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
    var saveMessage by remember { mutableStateOf("") }

    LaunchedEffect(saveMessage) {
        if (saveMessage.isNotBlank()) {
            delay(5000L)
            saveMessage = ""
        }
    }

    // Student view: subject drill-down state
    var selectedCourseForDetail by remember { mutableStateOf<String?>(null) }

    if (role == UserRole.Student) {
        StudentAttendanceView(
            repository = repository,
            selectedCourseForDetail = selectedCourseForDetail,
            onSelectCourse = { selectedCourseForDetail = it },
            onBack = { selectedCourseForDetail = null }
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Attendance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                when (role) {
                    UserRole.Faculty -> "Mark attendance with optional remarks and generate reports."
                    UserRole.Admin -> "Full attendance control with reports and data management."
                    else -> ""
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
                AttendanceRowInline(
                    entry = entry,
                    selectedStatus = selectedStatuses[entry.id] ?: entry.status,
                    remark = selectedRemarks[entry.id] ?: entry.remark,
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
                        saveMessage = "Attendance updated"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save attendance")
                }
            }

            if (saveMessage.isNotBlank()) {
                item {
                    ElevatedCard {
                        Text(
                            saveMessage,
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                    }
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
                batch = reportBatchFilter,
                dateLabel = if (reportType == AttendanceReportType.Daily) reportDateFilter else null,
                monthLabel = if (reportType == AttendanceReportType.Monthly) selectedMonth else null,
                subject = if (reportType == AttendanceReportType.SubjectWise) reportSubjectFilter else null,
            )

            items(reportSheets) { sheet ->
                ElevatedCard {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(sheet.subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("${sheet.batch} • ${sheet.dateLabel}")
                        sheet.entries.forEach { entry ->
                            AttendanceRowInline(
                                entry = entry,
                                selectedStatus = entry.status,
                                remark = entry.remark,
                                onStatusChange = {},
                                onRemarkChange = {},
                                readOnly = true,
                            )
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

}

// ─── Student View ───────────────────────────────────────────────────────────────

@Composable
private fun StudentAttendanceView(
    repository: DemoRepository,
    selectedCourseForDetail: String?,
    onSelectCourse: (String) -> Unit,
    onBack: () -> Unit,
) {
    val studentBatch = repository.currentUser?.batch ?: return
    val studentRoll = repository.currentUser?.rollNo ?: return
    val allSheets = repository.attendanceSheets.filter { it.batch == studentBatch }
    val subjects = allSheets.map { it.subject }.distinct().sorted()

    if (selectedCourseForDetail != null) {
        // Drill-down: date-wise detail for a single course
        StudentCourseDetailView(
            courseName = selectedCourseForDetail,
            sheets = allSheets.filter { it.subject == selectedCourseForDetail },
            studentRoll = studentRoll,
            onBack = onBack,
        )
    } else {
        // Summary: subject-wise table
        StudentCourseSummaryView(
            subjects = subjects,
            allSheets = allSheets,
            studentRoll = studentRoll,
            onSelectCourse = onSelectCourse,
        )
    }
}

@Composable
private fun StudentCourseSummaryView(
    subjects: List<String>,
    allSheets: List<AttendanceSheet>,
    studentRoll: String,
    onSelectCourse: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("My Attendance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
        }

        item {
            ElevatedCard {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    // Table header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Course Name",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Total",
                            modifier = Modifier.width(52.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            "Present",
                            modifier = Modifier.width(56.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            "Absent",
                            modifier = Modifier.width(52.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    subjects.forEach { subject ->
                        val sheetsForSubject = allSheets.filter { it.subject == subject }
                        val myEntries = sheetsForSubject.flatMap { sheet ->
                            sheet.entries.filter { it.rollNo == studentRoll }
                        }
                        val total = myEntries.size
                        val present = myEntries.count { it.status == AttendanceStatus.Present }
                        val absent = myEntries.count { it.status == AttendanceStatus.Absent || it.status == AttendanceStatus.Leave }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectCourse(subject) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                subject,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                total.toString(),
                                modifier = Modifier.width(52.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                present.toString(),
                                modifier = Modifier.width(56.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF4CAF50),
                            )
                            Text(
                                absent.toString(),
                                modifier = Modifier.width(52.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = if (absent > 0) Color(0xFFF44336) else Color(0xFF4CAF50),
                            )
                        }
                        HorizontalDivider()
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tap a course for detailed attendance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentCourseDetailView(
    courseName: String,
    sheets: List<AttendanceSheet>,
    studentRoll: String,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    Spacer(Modifier.width(4.dp))
                    Text("Back")
                }
            }
        }

        item {
            ElevatedCard {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))

                    // Legend
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.height(14.dp))
                            Text("Present", style = MaterialTheme.typography.labelSmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.height(14.dp))
                            Text("Absent", style = MaterialTheme.typography.labelSmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.height(14.dp))
                            Text("Leave", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        item {
            ElevatedCard {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    // Column headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Date",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Status",
                            modifier = Modifier.width(72.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    val sortedSheets = sheets.sortedByDescending { it.dateLabel }
                    if (sortedSheets.isEmpty()) {
                        Text(
                            "No attendance records found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    sortedSheets.forEach { sheet ->
                        val myEntry = sheet.entries.firstOrNull { it.rollNo == studentRoll }
                        if (myEntry != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    sheet.dateLabel,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Row(
                                    modifier = Modifier.width(72.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    when (myEntry.status) {
                                        AttendanceStatus.Present -> {
                                            Icon(Icons.Filled.CheckCircle, contentDescription = "Present", tint = Color(0xFF4CAF50), modifier = Modifier.height(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("P", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                        }
                                        AttendanceStatus.Absent -> {
                                            Icon(Icons.Filled.Close, contentDescription = "Absent", tint = Color(0xFFF44336), modifier = Modifier.height(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("A", color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
                                        }
                                        AttendanceStatus.Leave -> {
                                            Icon(Icons.Filled.CheckCircle, contentDescription = "Leave", tint = Color(0xFFFF9800), modifier = Modifier.height(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("L", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

// ─── Inline attendance row for Faculty/Admin ────────────────────────────────────

@Composable
private fun AttendanceRowInline(
    entry: AttendanceEntry,
    selectedStatus: AttendanceStatus,
    remark: String,
    onStatusChange: (AttendanceStatus) -> Unit,
    onRemarkChange: (String) -> Unit,
    readOnly: Boolean = false,
) {
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Name and roll on the left
                Column(modifier = Modifier.weight(1f)) {
                    Text(entry.studentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(entry.rollNo, style = MaterialTheme.typography.bodySmall)
                }
                // Status buttons on the right, beside the name
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AttendanceStatus.values().forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { if (!readOnly) onStatusChange(status) },
                            label = { Text(status.label.take(1)) },
                            enabled = !readOnly,
                        )
                    }
                }
            }
            if (!readOnly) {
                OutlinedTextField(
                    value = remark,
                    onValueChange = onRemarkChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Remark (optional)") },
                    singleLine = true,
                )
            } else if (remark.isNotBlank()) {
                Text("Remark: $remark", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
