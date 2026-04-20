package com.example.ims_app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object DemoRepository {
    var currentUser by mutableStateOf<SessionUser?>(null)
    var activeRole by mutableStateOf(UserRole.Admin)
    var localizationSettings by mutableStateOf(UserLocalizationSettings())
    var generalSettings by mutableStateOf(GeneralSettings())
    var searchQuery by mutableStateOf("")
    var selectedBatch by mutableStateOf("B.Tech CSE - Sem 4")
    var selectedDate by mutableStateOf("10 Apr 2026")
    var selectedSubject by mutableStateOf("Database Systems")
    var selectedTimetableBatch by mutableStateOf("B.Tech CSE - Sem 4")

    private val attendanceBatches = mutableStateListOf(
        "B.Tech CSE - Sem 4",
        "B.Tech CSE - Sem 6",
        "BBA - Sem 2"
    )

    private var attendanceSubjectsByBatch by mutableStateOf(
        mapOf(
            "B.Tech CSE - Sem 4" to listOf("Database Systems", "Operating Systems"),
            "B.Tech CSE - Sem 6" to listOf("Software Engineering"),
            "BBA - Sem 2" to listOf("Business Communication")
        )
    )

    private var studentsByBatch by mutableStateOf(
        mapOf(
            "B.Tech CSE - Sem 4" to listOf(
                StudentRecord(1, "Aarav Singh", "IMS2401", "B.Tech CSE - Sem 4"),
                StudentRecord(2, "Meera Nair", "IMS2402", "B.Tech CSE - Sem 4"),
                StudentRecord(3, "Kabir Rao", "IMS2403", "B.Tech CSE - Sem 4"),
                StudentRecord(4, "Sara Khan", "IMS2404", "B.Tech CSE - Sem 4")
            ),
            "B.Tech CSE - Sem 6" to listOf(
                StudentRecord(1, "Anika Verma", "IMS2601", "B.Tech CSE - Sem 6"),
                StudentRecord(2, "Rohan Das", "IMS2602", "B.Tech CSE - Sem 6"),
                StudentRecord(3, "Ira Joshi", "IMS2603", "B.Tech CSE - Sem 6"),
                StudentRecord(4, "Vihaan Patel", "IMS2604", "B.Tech CSE - Sem 6")
            ),
            "BBA - Sem 2" to listOf(
                StudentRecord(1, "Nisha Arora", "IMB2201", "BBA - Sem 2"),
                StudentRecord(2, "Rahul Menon", "IMB2202", "BBA - Sem 2")
            )
        )
    )

    val timetableEntries = mutableStateListOf(
        TimetableEntry(1, "Database Systems", "B.Tech CSE - Sem 4", WeekDay.Monday, "09:00", "09:50", "R-201", "faculty1", "Dr. Rao"),
        TimetableEntry(2, "Operating Systems", "B.Tech CSE - Sem 4", WeekDay.Monday, "10:00", "10:50", "R-202", "faculty1", "Dr. Rao"),
        TimetableEntry(3, "Software Engineering", "B.Tech CSE - Sem 6", WeekDay.Tuesday, "11:00", "11:50", "R-301", "faculty1", "Dr. Rao"),
        TimetableEntry(4, "Business Communication", "BBA - Sem 2", WeekDay.Wednesday, "09:00", "09:50", "R-105", "admin", "System Admin")
    )

    val attendanceSheets = mutableStateListOf(
        AttendanceSheet(
            1,
            "B.Tech CSE - Sem 4",
            "10 Apr 2026",
            "Database Systems",
            listOf(
                AttendanceEntry(1, "Aarav Singh", "IMS2401", AttendanceStatus.Present, "Answered quiz"),
                AttendanceEntry(2, "Meera Nair", "IMS2402", AttendanceStatus.Present, ""),
                AttendanceEntry(3, "Kabir Rao", "IMS2403", AttendanceStatus.Absent, "Medical leave"),
                AttendanceEntry(4, "Sara Khan", "IMS2404", AttendanceStatus.Leave, "Family event")
            )
        ),
        AttendanceSheet(
            2,
            "B.Tech CSE - Sem 4",
            "11 Apr 2026",
            "Operating Systems",
            listOf(
                AttendanceEntry(1, "Aarav Singh", "IMS2401", AttendanceStatus.Present, ""),
                AttendanceEntry(2, "Meera Nair", "IMS2402", AttendanceStatus.Absent, "Late bus"),
                AttendanceEntry(3, "Kabir Rao", "IMS2403", AttendanceStatus.Present, ""),
                AttendanceEntry(4, "Sara Khan", "IMS2404", AttendanceStatus.Present, "")
            )
        ),
        AttendanceSheet(
            3,
            "B.Tech CSE - Sem 6",
            "12 Apr 2026",
            "Software Engineering",
            listOf(
                AttendanceEntry(1, "Anika Verma", "IMS2601", AttendanceStatus.Present, ""),
                AttendanceEntry(2, "Rohan Das", "IMS2602", AttendanceStatus.Leave, "Placement interview"),
                AttendanceEntry(3, "Ira Joshi", "IMS2603", AttendanceStatus.Present, ""),
                AttendanceEntry(4, "Vihaan Patel", "IMS2604", AttendanceStatus.Absent, "")
            )
        )
    )

    fun updateCurrentUser(user: SessionUser?) {
        currentUser = user
        activeRole = user?.role ?: UserRole.Admin
        if (activeRole == UserRole.Student) {
            user?.batch?.let {
                selectedBatch = it
                selectedTimetableBatch = it
                selectedSubject = subjectsForBatch(it).firstOrNull() ?: selectedSubject
            }
        }
    }

    fun updateLocalizationSettings(settings: UserLocalizationSettings) {
        localizationSettings = settings
    }

    fun canEditGradingSettings(): Boolean = activeRole == UserRole.Admin || activeRole == UserRole.Faculty

    fun canEditAdminOnlyGeneralSettings(): Boolean = activeRole == UserRole.Admin

    fun applyGeneralSettings(settings: GeneralSettings) {
        generalSettings = sanitizeGeneralSettings(settings)
    }

    fun updateGeneralSettings(settings: GeneralSettings) {
        val sanitized = sanitizeGeneralSettings(settings)
        generalSettings = when {
            activeRole == UserRole.Admin -> sanitized
            activeRole == UserRole.Faculty -> sanitized.copy(
                autoUniqueStudentIds = generalSettings.autoUniqueStudentIds,
                termType = generalSettings.termType,
                defaultAttendanceThreshold = generalSettings.defaultAttendanceThreshold,
            )
            else -> generalSettings
        }
    }

    fun dashboardMetrics(): List<DashboardMetric> {
        val timetableCount = visibleTimetableEntries().size
        val entries = attendanceSheets.flatMap { it.entries }
        val attendancePercent = if (entries.isEmpty()) 0 else (entries.count { it.status == AttendanceStatus.Present } * 100 / entries.size)
        return listOf(
            DashboardMetric("Students", studentsByBatch.values.sumOf { it.size }.toString(), "Across all batches"),
            DashboardMetric("Timetable slots", timetableCount.toString(), "Visible for your role"),
            DashboardMetric("Attendance", "$attendancePercent%", "Overall recorded")
        )
    }

    fun canManageAttendance(): Boolean = activeRole == UserRole.Admin || activeRole == UserRole.Faculty

    fun canManageAttendanceMasterData(): Boolean = activeRole == UserRole.Admin

    fun canManageTimetable(): Boolean = activeRole == UserRole.Admin || activeRole == UserRole.Faculty

    fun visibleTimetableEntries(): List<TimetableEntry> {
        val query = searchQuery.trim().lowercase()
        val roleFiltered = when (activeRole) {
            UserRole.Admin -> timetableEntries.toList()
            UserRole.Faculty -> timetableEntries.toList()
            UserRole.Student -> timetableEntries.filter { it.batch == (currentUser?.batch ?: selectedTimetableBatch) }
        }
        val batchFiltered = if (activeRole == UserRole.Student) roleFiltered else roleFiltered.filter { it.batch == selectedTimetableBatch }
        val searched = if (query.isBlank()) batchFiltered else batchFiltered.filter {
            it.subject.lowercase().contains(query) || it.batch.lowercase().contains(query) || it.facultyName.lowercase().contains(query)
        }
        return searched.sortedWith(compareBy({ it.day.ordinal }, { timeToMinutes(it.startTime) }))
    }

    fun canEditTimetableEntry(entry: TimetableEntry): Boolean {
        return when (activeRole) {
            UserRole.Admin -> true
            UserRole.Faculty -> currentUser?.username == entry.facultyUsername
            UserRole.Student -> false
        }
    }

    fun canDuplicateTimetableEntry(entry: TimetableEntry): Boolean = canEditTimetableEntry(entry)

    fun saveTimetableEntry(entry: TimetableEntry): String? {
        if (!canManageTimetable()) return "You do not have permission to modify timetable entries."
        if (activeRole == UserRole.Faculty && currentUser?.username != entry.facultyUsername) {
            return "Faculty can update only their own timetable entries."
        }
        if (isCourseWeeklyLimitExceeded(entry)) {
            return "A course can have at most 3 classes per week for the same batch."
        }
        if (hasTimetableConflict(entry)) {
            return "Time conflict detected for ${entry.batch} on ${entry.day.label}."
        }
        if (isFacultyWorkloadExceeded(entry.facultyUsername, entry.day, entry.id)) {
            return "Faculty workload limit exceeded for ${entry.day.label}."
        }

        val index = timetableEntries.indexOfFirst { it.id == entry.id }
        if (index >= 0) {
            timetableEntries[index] = entry
        } else {
            timetableEntries.add(0, entry.copy(id = nextTimetableId()))
        }
        return null
    }

    fun deleteTimetableEntry(id: Int): Boolean {
        val entry = timetableEntries.firstOrNull { it.id == id } ?: return false
        if (!canEditTimetableEntry(entry)) return false
        return timetableEntries.remove(entry)
    }

    fun nextTimetableId(): Int = (timetableEntries.maxOfOrNull { it.id } ?: 0) + 1

    private fun hasTimetableConflict(candidate: TimetableEntry): Boolean {
        val start = timeToMinutes(candidate.startTime)
        val end = timeToMinutes(candidate.endTime)
        if (start < 0 || end <= start) return true

        return timetableEntries.any { existing ->
            if (existing.id == candidate.id) return@any false
            if (existing.batch != candidate.batch || existing.day != candidate.day) return@any false
            val existingStart = timeToMinutes(existing.startTime)
            val existingEnd = timeToMinutes(existing.endTime)
            start < existingEnd && end > existingStart
        }
    }

    private fun isFacultyWorkloadExceeded(facultyUsername: String, day: WeekDay, excludeId: Int): Boolean {
        val dayLoad = timetableEntries.count {
            it.facultyUsername == facultyUsername && it.day == day && it.id != excludeId
        }
        return dayLoad >= 5
    }

    private fun isCourseWeeklyLimitExceeded(candidate: TimetableEntry): Boolean {
        val sameCourseCount = timetableEntries.count {
            it.id != candidate.id && it.subject == candidate.subject && it.batch == candidate.batch
        }
        return sameCourseCount >= 3
    }

    private fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        if (parts.size != 2) return -1
        val h = parts[0].toIntOrNull() ?: return -1
        val m = parts[1].toIntOrNull() ?: return -1
        if (h !in 0..23 || m !in 0..59) return -1
        return h * 60 + m
    }

    fun activeAttendanceSheet(): AttendanceSheet =
        attendanceSheets.firstOrNull { it.batch == selectedBatch && it.dateLabel == selectedDate && it.subject == selectedSubject }
            ?: AttendanceSheet(
                id = selectedBatch.hashCode() + selectedDate.hashCode() + selectedSubject.hashCode(),
                batch = selectedBatch,
                dateLabel = selectedDate,
                subject = selectedSubject,
                entries = defaultAttendanceEntries(selectedBatch)
            )

    fun upsertAttendanceSheet(sheet: AttendanceSheet) {
        if (!canManageAttendance()) return
        val index = attendanceSheets.indexOfFirst { it.id == sheet.id }
        if (index >= 0) {
            attendanceSheets[index] = sheet
        } else {
            attendanceSheets.add(0, sheet)
        }
    }

    fun attendanceSheetsFor(
        reportType: AttendanceReportType,
        batch: String? = null,
        dateLabel: String? = null,
        monthLabel: String? = null,
        subject: String? = null,
    ): List<AttendanceSheet> {
        return attendanceSheets.filter { sheet ->
            val matchesBatch = batch == null || sheet.batch == batch
            val matchesDate = dateLabel == null || sheet.dateLabel == dateLabel
            val matchesMonth = monthLabel == null || sheet.dateLabel.substringAfter(" ") == monthLabel
            val matchesSubject = subject == null || sheet.subject == subject
            val matchesRole = when (activeRole) {
                UserRole.Student -> sheet.batch == currentUser?.batch
                else -> true
            }
            when (reportType) {
                AttendanceReportType.Daily -> matchesBatch && matchesDate && matchesRole
                AttendanceReportType.Monthly -> matchesBatch && matchesMonth && matchesRole
                AttendanceReportType.SubjectWise -> matchesBatch && matchesSubject && matchesRole
            }
        }
    }

    fun attendanceDates(): List<String> = attendanceSheets.map { it.dateLabel }.distinct().sorted()

    fun attendanceMonths(): List<String> = attendanceSheets.map { it.dateLabel.substringAfter(" ") }.distinct().sorted()

    fun attendanceSubjects(): List<String> = attendanceSheets.map { it.subject }.distinct().sorted()

    fun attendanceBatches(): List<String> = attendanceBatches.toList()

    fun subjectsForBatch(batch: String): List<String> =
        attendanceSubjectsByBatch[batch].orEmpty().ifEmpty { listOf("General") }

    fun addAttendanceBatch(batch: String) {
        if (!canManageAttendanceMasterData()) return
        val normalized = batch.trim()
        if (normalized.isBlank() || attendanceBatches.contains(normalized)) return
        attendanceBatches.add(normalized)
        attendanceSubjectsByBatch = attendanceSubjectsByBatch + (normalized to listOf("General"))
        studentsByBatch = studentsByBatch + (normalized to emptyList())
    }

    fun addAttendanceSubject(batch: String, subject: String) {
        if (!canManageAttendanceMasterData()) return
        val normalizedBatch = batch.trim()
        val normalizedSubject = subject.trim()
        if (normalizedBatch.isBlank() || normalizedSubject.isBlank()) return
        if (!attendanceBatches.contains(normalizedBatch)) {
            addAttendanceBatch(normalizedBatch)
        }
        val existing = attendanceSubjectsByBatch[normalizedBatch].orEmpty()
        if (existing.contains(normalizedSubject)) return
        attendanceSubjectsByBatch = attendanceSubjectsByBatch + (normalizedBatch to (existing + normalizedSubject))
    }

    fun addAttendanceStudent(batch: String, studentName: String, rollNo: String?) {
        if (!canManageAttendanceMasterData()) return
        val normalizedBatch = batch.trim()
        val normalizedName = studentName.trim()
        val manualRollNo = rollNo?.trim().orEmpty()
        val normalizedRollNo = if (generalSettings.autoUniqueStudentIds) {
            nextAutoStudentRollNo()
        } else {
            manualRollNo.uppercase()
        }
        if (normalizedBatch.isBlank() || normalizedName.isBlank() || normalizedRollNo.isBlank()) return
        if (!attendanceBatches.contains(normalizedBatch)) {
            addAttendanceBatch(normalizedBatch)
        }
        val existingRecords = allStudentRecords()
        if (existingRecords.any { it.rollNo.equals(normalizedRollNo, ignoreCase = true) }) return
        val existing = studentsByBatch[normalizedBatch].orEmpty()
        val nextId = (existing.maxOfOrNull { it.id } ?: 0) + 1
        val updated = existing + StudentRecord(nextId, normalizedName, normalizedRollNo, normalizedBatch)
        studentsByBatch = studentsByBatch + (normalizedBatch to updated)
    }

    fun nextAutoStudentRollNo(): String {
        val nextNumeric = (allStudentRecords().mapNotNull { it.rollNo.toIntOrNull() }.maxOrNull() ?: 0) + 1
        return nextNumeric.toString().padStart(4, '0')
    }

    fun attendanceReportSummary(sheets: List<AttendanceSheet>): AttendanceReportSummary {
        val entries = sheets.flatMap { sheet ->
            if (activeRole == UserRole.Student) {
                val studentRoll = currentUser?.rollNo
                sheet.entries.filter { it.rollNo == studentRoll }
            } else {
                sheet.entries
            }
        }
        return AttendanceReportSummary(
            totalSheets = sheets.size,
            totalStudents = entries.size,
            presentCount = entries.count { it.status == AttendanceStatus.Present },
            absentCount = entries.count { it.status == AttendanceStatus.Absent },
            leaveCount = entries.count { it.status == AttendanceStatus.Leave },
        )
    }

    private fun defaultAttendanceEntries(batch: String): List<AttendanceEntry> {
        val roster = studentsByBatch[batch].orEmpty()
        if (roster.isEmpty()) return listOf(
            AttendanceEntry(1, "Aarav Singh", "IMS2401", AttendanceStatus.Present, ""),
            AttendanceEntry(2, "Meera Nair", "IMS2402", AttendanceStatus.Present, ""),
            AttendanceEntry(3, "Kabir Rao", "IMS2403", AttendanceStatus.Absent, ""),
            AttendanceEntry(4, "Sara Khan", "IMS2404", AttendanceStatus.Leave, "")
        )
        return roster.map { student ->
            AttendanceEntry(
                id = student.id,
                studentName = student.name,
                rollNo = student.rollNo,
                status = AttendanceStatus.Present,
                remark = ""
            )
        }
    }

    private fun allStudentRecords(): List<StudentRecord> = studentsByBatch.values.flatten()

    private fun sanitizeGeneralSettings(settings: GeneralSettings): GeneralSettings {
        val minA = settings.gradeScale.minA.coerceIn(0, 100)
        val minB = settings.gradeScale.minB.coerceIn(0, minA)
        val minC = settings.gradeScale.minC.coerceIn(0, minB)
        val minD = settings.gradeScale.minD.coerceIn(0, minC)
        val passMark = settings.passMarkThreshold.coerceIn(0, minD)
        return settings.copy(
            passMarkThreshold = passMark,
            gradeScale = GradeScaleBand(
                minA = minA,
                minB = minB,
                minC = minC,
                minD = minD,
            ),
            defaultAttendanceThreshold = settings.defaultAttendanceThreshold.coerceIn(0, 100),
        )
    }
}
