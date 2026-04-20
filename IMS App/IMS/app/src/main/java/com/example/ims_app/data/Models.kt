package com.example.ims_app.data

enum class UserRole(val label: String) {
    Admin("Admin"),
    Faculty("Faculty"),
    Student("Student")
}

enum class AttendanceStatus(val label: String) {
    Present("Present"),
    Absent("Absent"),
    Leave("Leave")
}

enum class AttendanceReportType(val label: String) {
    Daily("Daily"),
    Monthly("Monthly"),
    SubjectWise("Subject-wise")
}

enum class WeekDay(val label: String) {
    Monday("Monday"),
    Tuesday("Tuesday"),
    Wednesday("Wednesday"),
    Thursday("Thursday"),
    Friday("Friday"),
    Saturday("Saturday")
}

data class DashboardMetric(
    val title: String,
    val value: String,
    val subtitle: String,
)

data class UserAccount(
    val username: String,
    val password: String,
    val role: UserRole,
    val displayName: String,
    val batch: String? = null,
    val rollNo: String? = null,
)

data class SessionUser(
    val username: String,
    val displayName: String,
    val role: UserRole,
    val batch: String? = null,
    val rollNo: String? = null,
)

data class TimetableEntry(
    val id: Int,
    val subject: String,
    val batch: String,
    val day: WeekDay,
    val startTime: String,
    val endTime: String,
    val room: String,
    val facultyUsername: String,
    val facultyName: String,
)

data class AttendanceEntry(
    val id: Int,
    val studentName: String,
    val rollNo: String,
    val status: AttendanceStatus,
    val remark: String = "",
)

data class StudentRecord(
    val id: Int,
    val name: String,
    val rollNo: String,
    val batch: String,
)

data class AttendanceSheet(
    val id: Int,
    val batch: String,
    val dateLabel: String,
    val subject: String,
    val entries: List<AttendanceEntry>,
)

data class AttendanceReportSummary(
    val totalSheets: Int,
    val totalStudents: Int,
    val presentCount: Int,
    val absentCount: Int,
    val leaveCount: Int,
)
