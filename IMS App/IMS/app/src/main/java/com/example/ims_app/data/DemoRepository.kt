package com.example.ims_app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DemoRepository {
    private const val TIMETABLE_DAY_START = 8 * 60 + 30
    private const val TIMETABLE_LUNCH_START = 13 * 60
    private const val TIMETABLE_LUNCH_END = 14 * 60
    private const val TIMETABLE_DAY_END = 18 * 60 + 30
    private const val TIMETABLE_MIN_SLOT_MINUTES = 30
    private const val TIMETABLE_MAX_SLOT_MINUTES = 90

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
            "B.Tech CSE - Sem 4" to listOf(
                BatchSubject("Database Systems"),
                BatchSubject("Operating Systems")
            ),
            "B.Tech CSE - Sem 6" to listOf(
                BatchSubject("Software Engineering")
            ),
            "BBA - Sem 2" to listOf(
                BatchSubject("Business Communication")
            )
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

    private val coursesState = mutableStateListOf(
        Course(1, "CSE401", "Database Systems"),
        Course(2, "CSE402", "Operating Systems"),
        Course(3, "CSE601", "Software Engineering"),
        Course(4, "BBA201", "Business Communication")
    )

    private val batchTransferLogs = mutableStateListOf<BatchTransferLog>()

    val timetableEntries = mutableStateListOf(
        TimetableEntry(1, "Database Systems", "B.Tech CSE - Sem 4", WeekDay.Monday, "08:30", "10:00", "R-201", "faculty1", "Dr. Rao"),
        TimetableEntry(2, "Operating Systems", "B.Tech CSE - Sem 4", WeekDay.Monday, "10:00", "11:30", "R-202", "faculty1", "Dr. Rao"),
        TimetableEntry(3, "Software Engineering", "B.Tech CSE - Sem 6", WeekDay.Tuesday, "14:00", "15:30", "R-301", "faculty1", "Dr. Rao"),
        TimetableEntry(4, "Business Communication", "BBA - Sem 2", WeekDay.Wednesday, "15:30", "17:00", "R-105", "admin", "System Admin")
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

    fun canManageAcademicCatalog(): Boolean = activeRole == UserRole.Admin || activeRole == UserRole.Faculty

    fun canManageAcademicBatches(): Boolean = activeRole == UserRole.Admin

    fun canManageBatchTransfers(): Boolean = activeRole == UserRole.Admin

    fun courses(): List<Course> = coursesState.toList().sortedWith(compareBy({ it.code }, { it.name }))

    fun managedSubjects(batch: String): List<BatchSubject> = attendanceSubjectsByBatch[batch].orEmpty()

    fun studentsForBatch(batch: String): List<StudentRecord> = studentsByBatch[batch].orEmpty().sortedBy { it.rollNo }

    fun transferHistory(): List<BatchTransferLog> = batchTransferLogs.toList().sortedByDescending { it.id }

    fun addCourse(code: String, name: String): String? {
        if (!canManageAcademicCatalog()) return "You are not allowed to manage courses."
        val normalizedCode = code.trim().uppercase()
        val normalizedName = name.trim()
        if (normalizedCode.isBlank() || normalizedName.isBlank()) return "Course code and name are required."
        if (coursesState.any { it.code.equals(normalizedCode, ignoreCase = true) }) {
            return "Course code already exists."
        }
        val nextId = (coursesState.maxOfOrNull { it.id } ?: 0) + 1
        coursesState.add(Course(nextId, normalizedCode, normalizedName))
        return null
    }

    fun updateCourse(courseId: Int, code: String, name: String): String? {
        if (!canManageAcademicCatalog()) return "You are not allowed to manage courses."
        val normalizedCode = code.trim().uppercase()
        val normalizedName = name.trim()
        if (normalizedCode.isBlank() || normalizedName.isBlank()) return "Course code and name are required."
        val index = coursesState.indexOfFirst { it.id == courseId }
        if (index < 0) return "Course not found."
        if (coursesState.any { it.id != courseId && it.code.equals(normalizedCode, ignoreCase = true) }) {
            return "Course code already exists."
        }
        coursesState[index] = coursesState[index].copy(code = normalizedCode, name = normalizedName)
        return null
    }

    fun deleteCourse(courseId: Int): String? {
        if (!canManageAcademicCatalog()) return "You are not allowed to manage courses."
        val target = coursesState.firstOrNull { it.id == courseId } ?: return "Course not found."
        coursesState.remove(target)
        return null
    }

    fun addAcademicBatch(batch: String): String? {
        if (!canManageAcademicBatches()) return "Only admin can manage batches."
        val normalized = batch.trim()
        if (normalized.isBlank()) return "Batch name is required."
        if (attendanceBatches.any { it.equals(normalized, ignoreCase = true) }) return "Batch already exists."

        attendanceBatches.add(normalized)
        attendanceSubjectsByBatch = attendanceSubjectsByBatch + (normalized to listOf(BatchSubject("General")))
        studentsByBatch = studentsByBatch + (normalized to emptyList())
        return null
    }

    fun removeAcademicBatch(batch: String): String? {
        if (!canManageAcademicBatches()) return "Only admin can manage batches."
        val normalized = batch.trim()
        if (normalized.isBlank()) return "Batch name is required."
        if (!attendanceBatches.contains(normalized)) return "Batch not found."
        if (attendanceBatches.size <= 1) return "At least one batch must remain."

        // Cascade-delete timetable entries and attendance sheets for this batch
        timetableEntries.removeAll { it.batch == normalized }
        val sheetsToRemove = attendanceSheets.filter { it.batch == normalized }
        sheetsToRemove.forEach { attendanceSheets.remove(it) }

        attendanceBatches.remove(normalized)
        attendanceSubjectsByBatch = attendanceSubjectsByBatch - normalized
        studentsByBatch = studentsByBatch - normalized
        if (selectedBatch == normalized) {
            selectedBatch = attendanceBatches.first()
        }
        if (selectedTimetableBatch == normalized) {
            selectedTimetableBatch = attendanceBatches.first()
        }
        return null
    }

    fun renameAcademicBatch(oldName: String, newName: String): String? {
        if (!canManageAcademicBatches()) return "Only admin can manage batches."
        val oldNorm = oldName.trim()
        val newNorm = newName.trim()
        if (oldNorm.isBlank() || newNorm.isBlank()) return "Batch name is required."
        if (!attendanceBatches.contains(oldNorm)) return "Batch not found."
        if (oldNorm == newNorm) return null // no-op
        if (attendanceBatches.any { it.equals(newNorm, ignoreCase = true) }) return "A batch with that name already exists."

        // Rename in the batch list
        val idx = attendanceBatches.indexOf(oldNorm)
        attendanceBatches[idx] = newNorm

        // Migrate subjects
        val subjects = attendanceSubjectsByBatch[oldNorm].orEmpty()
        attendanceSubjectsByBatch = (attendanceSubjectsByBatch - oldNorm) + (newNorm to subjects)

        // Migrate students
        val students = studentsByBatch[oldNorm].orEmpty().map { it.copy(batch = newNorm) }
        studentsByBatch = (studentsByBatch - oldNorm) + (newNorm to students)

        // Migrate timetable entries
        timetableEntries.forEachIndexed { i, entry ->
            if (entry.batch == oldNorm) timetableEntries[i] = entry.copy(batch = newNorm)
        }

        // Migrate attendance sheets
        attendanceSheets.forEachIndexed { i, sheet ->
            if (sheet.batch == oldNorm) attendanceSheets[i] = sheet.copy(batch = newNorm)
        }

        // Update selected state
        if (selectedBatch == oldNorm) selectedBatch = newNorm
        if (selectedTimetableBatch == oldNorm) selectedTimetableBatch = newNorm

        return null
    }

    fun addManagedSubject(batch: String, subject: String, isElective: Boolean): String? {
        if (!canManageAcademicCatalog()) return "You are not allowed to manage subjects."
        val normalizedBatch = batch.trim()
        val normalizedSubject = subject.trim()
        if (normalizedBatch.isBlank() || normalizedSubject.isBlank()) return "Batch and subject are required."
        val existing = attendanceSubjectsByBatch[normalizedBatch]
            ?: return "Batch not found. Create the batch first."
        if (existing.any { it.name.equals(normalizedSubject, ignoreCase = true) }) {
            return "Subject already exists in this batch."
        }
        attendanceSubjectsByBatch = attendanceSubjectsByBatch + (
            normalizedBatch to (existing + BatchSubject(normalizedSubject, isElective))
        )
        return null
    }

    fun updateManagedSubject(batch: String, currentName: String, newName: String, isElective: Boolean): String? {
        if (!canManageAcademicCatalog()) return "You are not allowed to manage subjects."
        val normalizedBatch = batch.trim()
        val normalizedCurrent = currentName.trim()
        val normalizedNew = newName.trim()
        if (normalizedBatch.isBlank() || normalizedCurrent.isBlank() || normalizedNew.isBlank()) {
            return "Batch and subject name are required."
        }
        val existing = attendanceSubjectsByBatch[normalizedBatch] ?: return "Batch not found."
        val index = existing.indexOfFirst { it.name.equals(normalizedCurrent, ignoreCase = true) }
        if (index < 0) return "Subject not found in this batch."
        if (existing.any { it.name.equals(normalizedNew, ignoreCase = true) && !it.name.equals(normalizedCurrent, ignoreCase = true) }) {
            return "Another subject with this name already exists."
        }
        val updatedSubjects = existing.toMutableList()
        updatedSubjects[index] = BatchSubject(normalizedNew, isElective)
        attendanceSubjectsByBatch = attendanceSubjectsByBatch + (normalizedBatch to updatedSubjects)

        if (selectedBatch == normalizedBatch && selectedSubject.equals(normalizedCurrent, ignoreCase = true)) {
            selectedSubject = normalizedNew
        }

        attendanceSheets.indices.forEach { i ->
            val sheet = attendanceSheets[i]
            if (sheet.batch == normalizedBatch && sheet.subject.equals(normalizedCurrent, ignoreCase = true)) {
                attendanceSheets[i] = sheet.copy(subject = normalizedNew)
            }
        }

        timetableEntries.indices.forEach { i ->
            val entry = timetableEntries[i]
            if (entry.batch == normalizedBatch && entry.subject.equals(normalizedCurrent, ignoreCase = true)) {
                timetableEntries[i] = entry.copy(subject = normalizedNew)
            }
        }

        return null
    }

    fun deleteManagedSubject(batch: String, subject: String): String? {
        if (!canManageAcademicCatalog()) return "You are not allowed to manage subjects."
        val normalizedBatch = batch.trim()
        val normalizedSubject = subject.trim()
        if (normalizedBatch.isBlank() || normalizedSubject.isBlank()) return "Batch and subject are required."
        val existing = attendanceSubjectsByBatch[normalizedBatch] ?: return "Batch not found."
        val index = existing.indexOfFirst { it.name.equals(normalizedSubject, ignoreCase = true) }
        if (index < 0) return "Subject not found in this batch."
        if (existing.size <= 1) return "A batch must have at least one subject."
        if (attendanceSheets.any { it.batch == normalizedBatch && it.subject.equals(normalizedSubject, ignoreCase = true) }) {
            return "Cannot delete subject because attendance records exist."
        }
        if (timetableEntries.any { it.batch == normalizedBatch && it.subject.equals(normalizedSubject, ignoreCase = true) }) {
            return "Cannot delete subject because timetable entries exist."
        }

        attendanceSubjectsByBatch = attendanceSubjectsByBatch + (
            normalizedBatch to existing.filterNot { it.name.equals(normalizedSubject, ignoreCase = true) }
        )
        if (selectedBatch == normalizedBatch && selectedSubject.equals(normalizedSubject, ignoreCase = true)) {
            selectedSubject = subjectsForBatch(normalizedBatch).firstOrNull() ?: selectedSubject
        }
        return null
    }

    /** Directly transfers students. Admin-only. */
    fun executeTransfer(studentRollNos: List<String>, fromBatch: String, toBatch: String, reason: String): String? {
        if (!canManageBatchTransfers()) return "Only admin can transfer students."
        val source = fromBatch.trim()
        val target = toBatch.trim()
        val transferReason = reason.trim()
        val normalizedRollNos = studentRollNos.map { it.trim().uppercase() }.filter { it.isNotBlank() }.distinct()
        if (source.isBlank() || target.isBlank()) return "Source and target batches are required."
        if (source == target) return "Source and target batch cannot be the same."
        if (transferReason.isBlank()) return "Transfer reason is required."
        if (normalizedRollNos.isEmpty()) return "Select at least one student for transfer."
        if (!attendanceBatches.contains(source) || !attendanceBatches.contains(target)) return "Invalid batch selection."

        val sourceStudents = studentsByBatch[source].orEmpty().toMutableList()
        val targetStudents = studentsByBatch[target].orEmpty().toMutableList()
        val missing = normalizedRollNos.filter { roll -> sourceStudents.none { it.rollNo.equals(roll, ignoreCase = true) } }
        if (missing.isNotEmpty()) return "Some selected students are not present in source batch."

        val executedBy = currentUser?.username ?: "admin"
        val executedAt = nowLabel()

        normalizedRollNos.forEach { roll ->
            val sourceStudent = sourceStudents.first { it.rollNo.equals(roll, ignoreCase = true) }
            sourceStudents.remove(sourceStudent)
            val nextTargetId = (targetStudents.maxOfOrNull { it.id } ?: 0) + 1
            targetStudents.add(sourceStudent.copy(id = nextTargetId, batch = target))
            batchTransferLogs.add(
                0,
                BatchTransferLog(
                    id = nextTransferLogId(),
                    studentRollNo = sourceStudent.rollNo,
                    fromBatch = source,
                    toBatch = target,
                    reason = transferReason,
                    approvedBy = executedBy,
                    approvedAt = executedAt,
                )
            )
        }

        studentsByBatch = studentsByBatch + mapOf(
            source to sourceStudents,
            target to targetStudents,
        )
        return null
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



    fun canManageTimetable(): Boolean = activeRole == UserRole.Admin || activeRole == UserRole.Faculty

    fun visibleTimetableEntries(): List<TimetableEntry> {
        val query = searchQuery.trim().lowercase()
        val roleFiltered = when (activeRole) {
            UserRole.Admin -> timetableEntries.toList()
            UserRole.Faculty -> timetableEntries.toList()
            UserRole.Student -> timetableEntries.filter { it.batch == (currentUser?.batch ?: selectedTimetableBatch) }
        }
        val batchFiltered = if (activeRole == UserRole.Student) roleFiltered
            else if (selectedTimetableBatch == "All") roleFiltered
            else roleFiltered.filter { it.batch == selectedTimetableBatch }
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

    fun saveTimetableEntry(entry: TimetableEntry): String? {
        if (!canManageTimetable()) return "You do not have permission to modify timetable entries."
        if (activeRole == UserRole.Faculty && currentUser?.username != entry.facultyUsername) {
            return "Faculty can update only their own timetable entries."
        }
        validateTimetableWindow(entry)?.let { return it }
        if (isCourseWeeklyLimitExceeded(entry)) {
            return "A course can have at most 3 classes per week for the same batch."
        }
        if (hasTimetableConflict(entry)) {
            return "Time conflict detected for ${entry.batch} on ${entry.day.label}."
        }
        val workloadError = getFacultyWorkloadError(entry.facultyUsername, entry.day, entry.id)
        if (workloadError != null) return workloadError

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

    private fun getFacultyWorkloadError(facultyUsername: String, day: WeekDay, excludeId: Int): String? {
        val dayLoad = timetableEntries.count {
            it.facultyUsername == facultyUsername && it.day == day && it.id != excludeId
        }
        if (dayLoad >= 3) return "Faculty can have at most 3 classes per day. Limit reached for ${day.label}."
        val weekLoad = timetableEntries.count {
            it.facultyUsername == facultyUsername && it.id != excludeId
        }
        if (weekLoad >= 12) return "Faculty can have at most 12 classes per week. Weekly limit reached."
        return null
    }

    private fun isCourseWeeklyLimitExceeded(candidate: TimetableEntry): Boolean {
        val sameCourseCount = timetableEntries.count {
            it.id != candidate.id && it.subject == candidate.subject && it.batch == candidate.batch
        }
        return sameCourseCount >= 3
    }

    private fun validateTimetableWindow(candidate: TimetableEntry): String? {
        val start = timeToMinutes(candidate.startTime)
        val end = timeToMinutes(candidate.endTime)
        if (start < 0 || end <= start) return "Invalid start/end time format."
        if (start < TIMETABLE_DAY_START) return "Timetable starts at 08:30."
        if (end > TIMETABLE_DAY_END) return "Classes must end by 18:30."
        if (start < TIMETABLE_LUNCH_END && end > TIMETABLE_LUNCH_START) {
            return "13:00-14:00 is reserved as lunch break."
        }
        val duration = end - start
        if (duration < TIMETABLE_MIN_SLOT_MINUTES) {
            return "Each timetable class must be at least 30 minutes."
        }
        if (duration > TIMETABLE_MAX_SLOT_MINUTES) {
            return "A timetable class can be at most 90 minutes."
        }
        return null
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
        attendanceSubjectsByBatch[batch].orEmpty().map { it.name }.ifEmpty { listOf("General") }

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



    private fun nextTransferLogId(): Int = (batchTransferLogs.maxOfOrNull { it.id } ?: 0) + 1

    private fun nowLabel(): String = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date())

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
