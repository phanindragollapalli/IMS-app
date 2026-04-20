package com.example.ims_app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ims_app.data.AppCurrency
import com.example.ims_app.data.AppLanguage
import com.example.ims_app.data.AppTimeZone
import com.example.ims_app.data.TransferStatus
import com.example.ims_app.data.DashboardMetric
import com.example.ims_app.data.DemoRepository
import com.example.ims_app.data.GeneralSettings
import com.example.ims_app.data.GradingSystem
import com.example.ims_app.data.SessionManager
import com.example.ims_app.data.TermType
import com.example.ims_app.data.UserLocalizationSettings
import com.example.ims_app.data.UserRole

@Composable
fun DashboardScreen(
    repository: DemoRepository,
    onNavigateToTimetable: () -> Unit,
    onNavigateToAttendance: () -> Unit,
) {
    val metrics = repository.dashboardMetrics()
    val context = LocalContext.current
    val sessionManager = remember(context) { SessionManager(context.applicationContext) }

    val saveLocalizationSettings: (UserLocalizationSettings) -> Unit = { updated ->
        repository.updateLocalizationSettings(updated)
        repository.currentUser?.username?.let { username ->
            sessionManager.saveUserLocalizationSettings(username, updated)
        }
    }

    val saveGeneralSettings: (GeneralSettings) -> Unit = { updated ->
        repository.updateGeneralSettings(updated)
        sessionManager.saveGeneralSettings(repository.generalSettings)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ElevatedCard {
                Column(Modifier.padding(20.dp)) {
                    Text("Welcome back", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "Institute dashboard",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = repository.searchQuery,
                        onValueChange = { repository.searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        placeholder = { Text("Search modules, subjects, batches") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Logged in as: ${repository.currentUser?.displayName ?: "Guest"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Role: ${repository.activeRole.label}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                metrics.forEach { metric ->
                    MetricCard(metric = metric, modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            SectionHeader(title = "Quick access", subtitle = "Launch your module workflows")
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionCard(
                    title = "Time Table",
                    subtitle = when (repository.activeRole) {
                        UserRole.Student -> "View your timetable by day and slot"
                        UserRole.Faculty -> "Update your timetable and check conflicts"
                        UserRole.Admin -> "Manage all timetable entries"
                    },
                    actionText = "Open timetable",
                    onClick = onNavigateToTimetable
                )
                ActionCard(
                    title = "Attendance",
                    subtitle = when (repository.activeRole) {
                        UserRole.Student -> "View your personal attendance reports"
                        UserRole.Faculty -> "Mark attendance and generate reports"
                        UserRole.Admin -> "Manage attendance data and reports"
                    },
                    actionText = "Open attendance",
                    onClick = onNavigateToAttendance
                )
            }
        }

        item {
            SectionHeader(title = "Academic management", subtitle = "Courses, batches, subjects, electives and transfers")
        }

        item {
            AcademicManagementCard(repository = repository)
        }

        item {
            SectionHeader(title = "Configuration", subtitle = "Language settings and basic configuration")
        }

        item {
            LocalizationSettingsCard(
                settings = repository.localizationSettings,
                onSettingsChange = saveLocalizationSettings
            )
        }

        item {
            GeneralSettingsCard(
                settings = repository.generalSettings,
                canEditGrading = repository.canEditGradingSettings(),
                canEditAdminOnly = repository.canEditAdminOnlyGeneralSettings(),
                onSettingsChange = saveGeneralSettings
            )
        }

        item {
            SectionHeader(title = "Today", subtitle = "Operational snapshot")
        }

        items(listOf(
            "Timetable conflicts are highlighted before save",
            "Attendance reports support daily/monthly/subject-wise filters",
            "Session remains valid for up to 3 days"
        )) { note ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(note, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
private fun LocalizationSettingsCard(
    settings: UserLocalizationSettings,
    onSettingsChange: (UserLocalizationSettings) -> Unit,
) {
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Language and region", style = MaterialTheme.typography.labelLarge)

            EnumDropdownField(
                label = "Language",
                selectedOption = settings.language,
                options = AppLanguage.values().toList(),
                optionLabel = { it.label },
                onOptionSelected = { selected -> onSettingsChange(settings.copy(language = selected)) }
            )

            OutlinedTextField(
                value = settings.country,
                onValueChange = { country -> onSettingsChange(settings.copy(country = country)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Country") },
                singleLine = true
            )

            EnumDropdownField(
                label = "Currency",
                selectedOption = settings.currency,
                options = AppCurrency.values().toList(),
                optionLabel = { it.label },
                onOptionSelected = { selected -> onSettingsChange(settings.copy(currency = selected)) }
            )

            EnumDropdownField(
                label = "Time zone",
                selectedOption = settings.timeZone,
                options = AppTimeZone.values().toList(),
                optionLabel = { it.label },
                onOptionSelected = { selected -> onSettingsChange(settings.copy(timeZone = selected)) }
            )
        }
    }
}

@Composable
private fun AcademicManagementCard(repository: DemoRepository) {
    val batches = repository.attendanceBatches()
    val courses = repository.courses()
    val canManageCatalog = repository.canManageAcademicCatalog()
    val canManageBatches = repository.canManageAcademicBatches()
    val canManageTransfers = repository.canManageBatchTransfers()

    var message by remember { mutableStateOf("") }
    var editingCourseId by remember { mutableStateOf<Int?>(null) }
    var courseCode by remember { mutableStateOf("") }
    var courseName by remember { mutableStateOf("") }

    var newBatchName by remember { mutableStateOf("") }

    var selectedSubjectBatch by remember { mutableStateOf(batches.firstOrNull().orEmpty()) }
    var subjectName by remember { mutableStateOf("") }
    var subjectElective by remember { mutableStateOf(false) }

    var sourceBatch by remember { mutableStateOf(batches.firstOrNull().orEmpty()) }
    var targetBatch by remember { mutableStateOf(batches.drop(1).firstOrNull() ?: batches.firstOrNull().orEmpty()) }
    var transferReason by remember { mutableStateOf("") }
    val selectedRollNos = remember { mutableStateListOf<String>() }

    LaunchedEffect(batches) {
        if (batches.isEmpty()) return@LaunchedEffect
        if (selectedSubjectBatch !in batches) {
            selectedSubjectBatch = batches.first()
        }
        if (sourceBatch !in batches) {
            sourceBatch = batches.first()
        }
        val targetCandidates = batches.filterNot { it == sourceBatch }
        if (targetBatch !in targetCandidates) {
            targetBatch = targetCandidates.firstOrNull().orEmpty()
        }
    }

    val sourceStudents = repository.studentsForBatch(sourceBatch)
    LaunchedEffect(sourceBatch, sourceStudents.size) {
        selectedRollNos.removeAll { selected -> sourceStudents.none { it.rollNo == selected } }
    }

    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Course management", style = MaterialTheme.typography.labelLarge)

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
                TextButton(
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
                TextButton(
                    onClick = {
                        editingCourseId = null
                        courseCode = ""
                        courseName = ""
                    },
                    enabled = canManageCatalog,
                ) {
                    Text("Clear")
                }
            }

            courses.forEach { course ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${course.code} - ${course.name}", style = MaterialTheme.typography.bodyMedium)
                    if (canManageCatalog) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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

            Text("Batch management", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = newBatchName,
                onValueChange = { newBatchName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("New batch name") },
                singleLine = true,
                enabled = canManageBatches,
            )
            TextButton(
                onClick = {
                    message = repository.addAcademicBatch(newBatchName) ?: "Batch added."
                    if (message == "Batch added.") newBatchName = ""
                },
                enabled = canManageBatches,
            ) {
                Text("Add batch")
            }
            batches.forEach { batch ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(batch, style = MaterialTheme.typography.bodyMedium)
                    if (canManageBatches) {
                        TextButton(onClick = {
                            message = repository.removeAcademicBatch(batch) ?: "Batch removed."
                        }) {
                            Text("Remove")
                        }
                    }
                }
            }

            Text("Subjects and electives", style = MaterialTheme.typography.labelLarge)
            if (batches.isNotEmpty()) {
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
                TextButton(
                    onClick = {
                        message = repository.addManagedSubject(selectedSubjectBatch, subjectName, subjectElective) ?: "Subject added."
                        if (message == "Subject added.") {
                            subjectName = ""
                            subjectElective = false
                        }
                    },
                    enabled = canManageCatalog,
                ) {
                    Text("Add subject")
                }

                repository.managedSubjects(selectedSubjectBatch).forEach { managed ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (managed.isElective) "${managed.name} (Elective)" else managed.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (canManageCatalog) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                }) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }

            Text("Batch transfers", style = MaterialTheme.typography.labelLarge)
            if (canManageTransfers && batches.size > 1) {
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

                Text("Select students", style = MaterialTheme.typography.bodyMedium)
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

                TextButton(onClick = {
                    val error = if (selectedRollNos.size <= 1) {
                        repository.initiateSingleTransfer(
                            studentRollNo = selectedRollNos.firstOrNull().orEmpty(),
                            fromBatch = sourceBatch,
                            toBatch = targetBatch,
                            reason = transferReason,
                        )
                    } else {
                        repository.initiateBulkTransfer(
                            studentRollNos = selectedRollNos,
                            fromBatch = sourceBatch,
                            toBatch = targetBatch,
                            reason = transferReason,
                        )
                    }
                    message = error ?: "Transfer request submitted."
                    if (error == null) {
                        selectedRollNos.clear()
                        transferReason = ""
                    }
                }) {
                    Text(if (selectedRollNos.size > 1) "Create bulk transfer request" else "Create single transfer request")
                }

                repository.transferRequests().filter { it.status == TransferStatus.Pending }.forEach { request ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Request #${request.id}: ${request.studentRollNos.size} student(s), ${request.fromBatch} -> ${request.toBatch}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("Reason: ${request.reason}", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                message = repository.approveTransferRequest(request.id) ?: "Transfer approved."
                            }) { Text("Approve") }
                            TextButton(onClick = {
                                message = repository.rejectTransferRequest(request.id) ?: "Transfer rejected."
                            }) { Text("Reject") }
                        }
                    }
                }

                repository.transferHistory().take(5).forEach { log ->
                    Text(
                        "${log.studentRollNo}: ${log.fromBatch} -> ${log.toBatch} (${log.approvedAt})",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                Text(
                    "Only admin can create and approve transfer requests.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (message.isNotBlank()) {
                Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun GeneralSettingsCard(
    settings: GeneralSettings,
    canEditGrading: Boolean,
    canEditAdminOnly: Boolean,
    onSettingsChange: (GeneralSettings) -> Unit,
) {
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("General settings", style = MaterialTheme.typography.labelLarge)

            EnumDropdownField(
                label = "Grading system",
                selectedOption = settings.gradingSystem,
                options = GradingSystem.values().toList(),
                optionLabel = { it.label },
                onOptionSelected = { selected -> onSettingsChange(settings.copy(gradingSystem = selected)) },
                enabled = canEditGrading,
            )

            IntegerSettingField(
                label = "Pass mark threshold (%)",
                value = settings.passMarkThreshold,
                enabled = canEditGrading,
                onValueChanged = { updated -> onSettingsChange(settings.copy(passMarkThreshold = updated)) }
            )

            if (settings.gradingSystem == GradingSystem.LetterGrade) {
                Text("Grade scale boundaries (minimum marks)", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    IntegerSettingField(
                        label = "A >=",
                        value = settings.gradeScale.minA,
                        enabled = canEditGrading,
                        modifier = Modifier.weight(1f),
                        onValueChanged = { updated ->
                            onSettingsChange(settings.copy(gradeScale = settings.gradeScale.copy(minA = updated)))
                        }
                    )
                    IntegerSettingField(
                        label = "B >=",
                        value = settings.gradeScale.minB,
                        enabled = canEditGrading,
                        modifier = Modifier.weight(1f),
                        onValueChanged = { updated ->
                            onSettingsChange(settings.copy(gradeScale = settings.gradeScale.copy(minB = updated)))
                        }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    IntegerSettingField(
                        label = "C >=",
                        value = settings.gradeScale.minC,
                        enabled = canEditGrading,
                        modifier = Modifier.weight(1f),
                        onValueChanged = { updated ->
                            onSettingsChange(settings.copy(gradeScale = settings.gradeScale.copy(minC = updated)))
                        }
                    )
                    IntegerSettingField(
                        label = "D >=",
                        value = settings.gradeScale.minD,
                        enabled = canEditGrading,
                        modifier = Modifier.weight(1f),
                        onValueChanged = { updated ->
                            onSettingsChange(settings.copy(gradeScale = settings.gradeScale.copy(minD = updated)))
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Automatic unique student IDs", style = MaterialTheme.typography.bodyMedium)
                    Text("Generate numeric IDs like 0001, 0002", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = settings.autoUniqueStudentIds,
                    onCheckedChange = if (canEditAdminOnly) {
                        { enabled -> onSettingsChange(settings.copy(autoUniqueStudentIds = enabled)) }
                    } else {
                        null
                    },
                    enabled = canEditAdminOnly
                )
            }

            EnumDropdownField(
                label = "Term type",
                selectedOption = settings.termType,
                options = TermType.values().toList(),
                optionLabel = { it.label },
                onOptionSelected = { selected -> onSettingsChange(settings.copy(termType = selected)) },
                enabled = canEditAdminOnly,
            )

            IntegerSettingField(
                label = "Default attendance threshold (%)",
                value = settings.defaultAttendanceThreshold,
                enabled = canEditAdminOnly,
                onValueChanged = { updated -> onSettingsChange(settings.copy(defaultAttendanceThreshold = updated)) }
            )

            if (!canEditAdminOnly) {
                Text(
                    "Only admin can edit auto IDs, term type, and attendance threshold.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun IntegerSettingField(
    label: String,
    value: Int,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onValueChanged: (Int) -> Unit,
) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { typed ->
            typed.toIntOrNull()?.let(onValueChanged)
        },
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        enabled = enabled
    )
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

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (enabled) {
                expanded = it
            }
        }
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
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
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

@Composable
private fun MetricCard(metric: DashboardMetric, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(metric.title, style = MaterialTheme.typography.labelLarge)
            Text(metric.value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(metric.subtitle, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ActionCard(title: String, subtitle: String, actionText: String, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {
                Text(actionText.take(1), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium)
    }
}
