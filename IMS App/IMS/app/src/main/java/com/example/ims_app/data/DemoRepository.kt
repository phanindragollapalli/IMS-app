package com.example.ims_app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object DemoRepository {
    var activeRole by mutableStateOf(UserRole.Admin)
    var searchQuery by mutableStateOf("")
    var selectedBatch by mutableStateOf("B.Tech CSE - Sem 4")
    var selectedDate by mutableStateOf("10 Apr 2026")

    val dashboardMetrics = listOf(
        DashboardMetric("Students", "842", "+24 this week"),
        DashboardMetric("Pending exams", "6", "3 scheduled today"),
        DashboardMetric("Attendance", "94%", "Batch average")
    )

    val examSessions = mutableStateListOf(
        ExamSession(1, "Database Systems", "CSE A", "12 Apr 2026", 100, ExamStatus.Scheduled),
        ExamSession(2, "Operating Systems", "CSE B", "13 Apr 2026", 75, ExamStatus.Draft),
        ExamSession(3, "Software Engineering", "CSE A", "Completed", 50, ExamStatus.Published)
    )

    val attendanceSheets = mutableStateListOf(
        AttendanceSheet(
            1,
            "B.Tech CSE - Sem 4",
            "10 Apr 2026",
            listOf(
                AttendanceEntry(1, "Aarav Singh", "IMS2401", AttendanceStatus.Present),
                AttendanceEntry(2, "Meera Nair", "IMS2402", AttendanceStatus.Present),
                AttendanceEntry(3, "Kabir Rao", "IMS2403", AttendanceStatus.Absent),
                AttendanceEntry(4, "Sara Khan", "IMS2404", AttendanceStatus.Leave)
            )
        )
    )

    fun filteredExams(): List<ExamSession> {
        val query = searchQuery.trim().lowercase()
        if (query.isBlank()) return examSessions.toList()
        return examSessions.filter {
            it.subject.lowercase().contains(query) || it.batch.lowercase().contains(query)
        }
    }

    fun activeAttendanceSheet(): AttendanceSheet =
        attendanceSheets.firstOrNull { it.batch == selectedBatch && it.dateLabel == selectedDate }
            ?: AttendanceSheet(
                id = selectedBatch.hashCode() + selectedDate.hashCode(),
                batch = selectedBatch,
                dateLabel = selectedDate,
                entries = defaultAttendanceEntries()
            )

    fun upsertAttendanceSheet(sheet: AttendanceSheet) {
        val index = attendanceSheets.indexOfFirst { it.id == sheet.id }
        if (index >= 0) {
            attendanceSheets[index] = sheet
        } else {
            attendanceSheets.add(0, sheet)
        }
    }

    fun nextExamId(): Int = (examSessions.maxOfOrNull { it.id } ?: 0) + 1

    fun updateExamStatus(id: Int) {
        val index = examSessions.indexOfFirst { it.id == id }
        if (index == -1) return
        val current = examSessions[index]
        val nextStatus = when (current.status) {
            ExamStatus.Draft -> ExamStatus.Scheduled
            ExamStatus.Scheduled -> ExamStatus.Published
            ExamStatus.Published -> ExamStatus.Completed
            ExamStatus.Completed -> ExamStatus.Completed
        }
        examSessions[index] = current.copy(status = nextStatus)
    }

    fun addExam(subject: String, batch: String, dateLabel: String) {
        examSessions.add(0, ExamSession(nextExamId(), subject, batch, dateLabel, 100, ExamStatus.Draft))
    }

    private fun defaultAttendanceEntries(): List<AttendanceEntry> = listOf(
        AttendanceEntry(1, "Aarav Singh", "IMS2401", AttendanceStatus.Present),
        AttendanceEntry(2, "Meera Nair", "IMS2402", AttendanceStatus.Present),
        AttendanceEntry(3, "Kabir Rao", "IMS2403", AttendanceStatus.Absent),
        AttendanceEntry(4, "Sara Khan", "IMS2404", AttendanceStatus.Leave)
    )
}
