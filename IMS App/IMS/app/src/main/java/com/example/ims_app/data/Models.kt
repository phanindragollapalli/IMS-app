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

enum class AppLanguage(val label: String, val code: String) {
    English("English", "en"),
    Hindi("Hindi", "hi"),
    Spanish("Spanish", "es"),
    French("French", "fr")
}

enum class AppCurrency(val label: String, val code: String) {
    INR("INR - Indian Rupee", "INR"),
    USD("USD - US Dollar", "USD"),
    EUR("EUR - Euro", "EUR"),
    GBP("GBP - British Pound", "GBP")
}

enum class AppTimeZone(val label: String, val id: String) {
    IST("IST (UTC +05:30)", "Asia/Kolkata"),
    UTC("UTC (UTC +00:00)", "UTC"),
    CET("CET (UTC +01:00)", "Europe/Paris"),
    EST("EST (UTC -05:00)", "America/New_York")
}

data class UserLocalizationSettings(
    val language: AppLanguage = AppLanguage.English,
    val country: String = "India",
    val currency: AppCurrency = AppCurrency.INR,
    val timeZone: AppTimeZone = AppTimeZone.IST,
)

enum class GradingSystem(val label: String) {
    LetterGrade("Letter Grade"),
    Percentage("Percentage"),
    GPA("GPA (10-point)")
}

enum class TermType(val label: String) {
    Semester("Semester"),
    Trimester("Trimester"),
    Annual("Annual")
}

data class GradeScaleBand(
    val minA: Int = 85,
    val minB: Int = 70,
    val minC: Int = 55,
    val minD: Int = 40,
)

data class GeneralSettings(
    val gradingSystem: GradingSystem = GradingSystem.LetterGrade,
    val passMarkThreshold: Int = 40,
    val gradeScale: GradeScaleBand = GradeScaleBand(),
    val autoUniqueStudentIds: Boolean = false,
    val termType: TermType = TermType.Semester,
    val defaultAttendanceThreshold: Int = 75,
)

data class Course(
    val id: Int,
    val code: String,
    val name: String,
)

data class BatchSubject(
    val name: String,
    val isElective: Boolean = false,
)

enum class TransferStatus(val label: String) {
    Pending("Pending"),
    Approved("Approved"),
    Rejected("Rejected")
}

data class BatchTransferRequest(
    val id: Int,
    val studentRollNos: List<String>,
    val fromBatch: String,
    val toBatch: String,
    val reason: String,
    val status: TransferStatus,
    val requestedBy: String,
    val requestedAt: String,
    val approvedBy: String? = null,
    val decidedAt: String? = null,
)

data class BatchTransferLog(
    val id: Int,
    val studentRollNo: String,
    val fromBatch: String,
    val toBatch: String,
    val reason: String,
    val approvedBy: String,
    val approvedAt: String,
)

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
