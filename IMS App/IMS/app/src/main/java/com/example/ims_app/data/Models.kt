package com.example.ims_app.data

enum class UserRole(val label: String) {
    Admin("Admin"),
    Faculty("Faculty"),
    Registrar("Registrar")
}

enum class ExamStatus(val label: String) {
    Draft("Draft"),
    Scheduled("Scheduled"),
    Published("Published"),
    Completed("Completed")
}

enum class AttendanceStatus(val label: String) {
    Present("Present"),
    Absent("Absent"),
    Leave("Leave")
}

data class DashboardMetric(
    val title: String,
    val value: String,
    val subtitle: String,
)

data class ExamSession(
    val id: Int,
    val subject: String,
    val batch: String,
    val dateLabel: String,
    val marks: Int,
    val status: ExamStatus,
)

data class AttendanceEntry(
    val id: Int,
    val studentName: String,
    val rollNo: String,
    val status: AttendanceStatus,
)

data class AttendanceSheet(
    val id: Int,
    val batch: String,
    val dateLabel: String,
    val entries: List<AttendanceEntry>,
)
