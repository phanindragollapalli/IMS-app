package com.example.ims_app.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.ims_app.data.DemoRepository
import com.example.ims_app.data.TimetableEntry
import com.example.ims_app.data.UserRole
import com.example.ims_app.data.WeekDay
import kotlin.math.roundToInt

private enum class TimetableViewMode {
    List,
    WeeklyBoard,
}

private data class SlotCell(
    val day: WeekDay,
    val startTime: String,
)

@Composable
fun TimetableScreen(repository: DemoRepository) {
    var showEditor by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<TimetableEntry?>(null) }
    var editorTitle by remember { mutableStateOf("Add timetable entry") }
    var errorMessage by remember { mutableStateOf("") }
    var viewMode by remember { mutableStateOf(TimetableViewMode.WeeklyBoard) }

    val entries = repository.visibleTimetableEntries()
    val batches = repository.attendanceBatches()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Time Table", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    when (repository.activeRole) {
                        UserRole.Admin -> "Full control over all timetable entries."
                        UserRole.Faculty -> "Update your own entries. Conflict and workload checks are enforced."
                        UserRole.Student -> "View-only timetable for your batch."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = viewMode == TimetableViewMode.WeeklyBoard,
                        onClick = { viewMode = TimetableViewMode.WeeklyBoard },
                        label = { Text("Weekly board") }
                    )
                    FilterChip(
                        selected = viewMode == TimetableViewMode.List,
                        onClick = { viewMode = TimetableViewMode.List },
                        label = { Text("List") }
                    )
                }
            }

            if (repository.activeRole != UserRole.Student) {
                item {
                    ElevatedCard {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Filter batch", style = MaterialTheme.typography.labelLarge)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(batches) { batch ->
                                    FilterChip(
                                        selected = repository.selectedTimetableBatch == batch,
                                        onClick = { repository.selectedTimetableBatch = batch },
                                        label = { Text(batch) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (errorMessage.isNotBlank()) {
                item {
                    ElevatedCard {
                        Text(
                            errorMessage,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (viewMode == TimetableViewMode.WeeklyBoard) {
                item {
                    WeeklyTimetableBoard(
                        entries = entries,
                        userRole = repository.activeRole,
                        canDrag = { repository.canEditTimetableEntry(it) },
                        onMoveEntry = { updated ->
                            val saveError = repository.saveTimetableEntry(updated)
                            errorMessage = saveError ?: ""
                        },
                        onEdit = {
                            editingEntry = it
                            editorTitle = "Edit timetable entry"
                            showEditor = true
                        },
                        onDuplicate = {
                            editingEntry = it.copy(id = repository.nextTimetableId())
                            editorTitle = "Duplicate course"
                            showEditor = true
                        }
                    )
                }
            } else {
                items(entries) { entry ->
                    TimetableCard(
                        entry = entry,
                        canEdit = repository.canEditTimetableEntry(entry),
                        onEdit = {
                            editingEntry = entry
                            editorTitle = "Edit timetable entry"
                            showEditor = true
                        },
                        onDuplicate = {
                            editingEntry = entry.copy(id = repository.nextTimetableId())
                            editorTitle = "Duplicate course"
                            showEditor = true
                        },
                        onDelete = {
                            val deleted = repository.deleteTimetableEntry(entry.id)
                            if (!deleted) {
                                errorMessage = "You are not allowed to delete this entry."
                            }
                        }
                    )
                }
            }
        }

        if (repository.canManageTimetable()) {
            FloatingActionButton(
                onClick = {
                    editingEntry = null
                    editorTitle = "Add timetable entry"
                    showEditor = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add timetable entry")
            }
        }
    }

    if (showEditor) {
        TimetableEditorDialog(
            repository = repository,
            initial = editingEntry,
            title = editorTitle,
            onDismiss = { showEditor = false },
            onSave = { candidate ->
                val error = repository.saveTimetableEntry(candidate)
                if (error == null) {
                    showEditor = false
                    errorMessage = ""
                } else {
                    errorMessage = error
                }
            }
        )
    }
}

@Composable
private fun WeeklyTimetableBoard(
    entries: List<TimetableEntry>,
    userRole: UserRole,
    canDrag: (TimetableEntry) -> Boolean,
    onMoveEntry: (TimetableEntry) -> Unit,
    onEdit: (TimetableEntry) -> Unit,
    onDuplicate: (TimetableEntry) -> Unit,
) {
    val defaultSlots = listOf("09:00", "10:00", "11:00", "12:00", "14:00", "15:00", "16:00")
    val slots = remember(entries) {
        (defaultSlots + entries.map { it.startTime }).distinct().sortedBy { toMinutes(it) }
    }
    val days = WeekDay.values().toList()

    val cellBounds = remember { mutableStateMapOf<SlotCell, Rect>() }
    var draggingEntryId by remember { mutableStateOf<Int?>(null) }
    var hoverCell by remember { mutableStateOf<SlotCell?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    val horizontalState = rememberScrollState()

    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Weekly timetable (drag cards between slots)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Long press and drag a class card to another day/time slot.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.horizontalScroll(horizontalState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(42.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Time", style = MaterialTheme.typography.labelMedium)
                    }
                    slots.forEach { slot ->
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .heightIn(min = 110.dp)
                                .wrapContentHeight(Alignment.Top),
                            contentAlignment = Alignment.TopStart
                        ) {
                            Text(slot, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                days.forEach { day ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(200.dp)
                                .height(42.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(day.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }

                        slots.forEach { slot ->
                            val cell = SlotCell(day = day, startTime = slot)
                            val entry = entries.firstOrNull { it.day == day && it.startTime == slot }
                            val isHover = hoverCell == cell

                            Box(
                                modifier = Modifier
                                    .width(200.dp)
                                    .heightIn(min = 110.dp)
                                    .wrapContentHeight(Alignment.Top)
                                    .border(
                                        width = if (isHover) 2.dp else 1.dp,
                                        color = if (isHover) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .onGloballyPositioned { coords ->
                                        cellBounds[cell] = coords.boundsInRoot()
                                    }
                                    .padding(6.dp)
                            ) {
                                if (entry != null) {
                                    val editable = canDrag(entry)
                                    var originInRoot by remember(entry.id) { mutableStateOf(Offset.Zero) }

                                    ElevatedCard(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .onGloballyPositioned { coords ->
                                                originInRoot = coords.boundsInRoot().topLeft
                                            }
                                            .then(
                                                if (editable) {
                                                    Modifier
                                                        .offset {
                                                            if (draggingEntryId == entry.id) {
                                                                IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt())
                                                            } else {
                                                                IntOffset.Zero
                                                            }
                                                        }
                                                        .pointerInput(entry.id) {
                                                            detectDragGesturesAfterLongPress(
                                                                onDragStart = {
                                                                    draggingEntryId = entry.id
                                                                    hoverCell = cell
                                                                    dragOffset = Offset.Zero
                                                                },
                                                                onDrag = { change, amount ->
                                                                    change.consume()
                                                                    dragOffset += amount
                                                                    val pointerInRoot = originInRoot + change.position + dragOffset
                                                                    hoverCell = cellBounds.entries.firstOrNull { (_, rect) -> rect.contains(pointerInRoot) }?.key
                                                                },
                                                                onDragEnd = {
                                                                    val target = hoverCell
                                                                    val source = SlotCell(entry.day, entry.startTime)
                                                                    if (target != null && target != source) {
                                                                        val duration = (toMinutes(entry.endTime) - toMinutes(entry.startTime)).coerceAtLeast(50)
                                                                        val targetStart = toMinutes(target.startTime)
                                                                        val updated = entry.copy(
                                                                            day = target.day,
                                                                            startTime = target.startTime,
                                                                            endTime = fromMinutes(targetStart + duration)
                                                                        )
                                                                        onMoveEntry(updated)
                                                                    }
                                                                    draggingEntryId = null
                                                                    hoverCell = null
                                                                    dragOffset = Offset.Zero
                                                                },
                                                                onDragCancel = {
                                                                    draggingEntryId = null
                                                                    hoverCell = null
                                                                    dragOffset = Offset.Zero
                                                                }
                                                            )
                                                        }
                                                } else {
                                                    Modifier
                                                }
                                            )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(8.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(entry.subject, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                                if (userRole != UserRole.Faculty) {
                                                    Text(entry.facultyName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Text(entry.room, style = MaterialTheme.typography.bodySmall)
                                                Text("${entry.startTime} - ${entry.endTime}", style = MaterialTheme.typography.bodySmall)
                                            }
                                            if (editable) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    TextButton(onClick = { onEdit(entry) }, contentPadding = PaddingValues(0.dp)) {
                                                        Text("Edit")
                                                    }
                                                    TextButton(onClick = { onDuplicate(entry) }, contentPadding = PaddingValues(0.dp)) {
                                                        Text("Duplicate")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimetableCard(
    entry: TimetableEntry,
    canEdit: Boolean,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(entry.subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${entry.batch} • ${entry.day.label}", style = MaterialTheme.typography.bodyMedium)
            Text("${entry.startTime} - ${entry.endTime} • ${entry.room}", style = MaterialTheme.typography.bodyMedium)
            Text("Faculty: ${entry.facultyName}", style = MaterialTheme.typography.bodyMedium)
            if (canEdit) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onEdit) { Text("Edit") }
                    Button(onClick = onDuplicate) { Text("Duplicate") }
                    Button(onClick = onDelete) { Text("Delete") }
                }
            }
        }
    }
}

@Composable
private fun TimetableEditorDialog(
    repository: DemoRepository,
    initial: TimetableEntry?,
    title: String,
    onDismiss: () -> Unit,
    onSave: (TimetableEntry) -> Unit,
) {
    val currentUser = repository.currentUser
    val isFaculty = repository.activeRole == UserRole.Faculty

    var subject by remember(initial) { mutableStateOf(initial?.subject ?: "") }
    var batch by remember(initial) { mutableStateOf(initial?.batch ?: repository.selectedTimetableBatch) }
    var day by remember(initial) { mutableStateOf(initial?.day ?: WeekDay.Monday) }
    var startTime by remember(initial) { mutableStateOf(initial?.startTime ?: "09:00") }
    var endTime by remember(initial) { mutableStateOf(initial?.endTime ?: "09:50") }
    var room by remember(initial) { mutableStateOf(initial?.room ?: "R-101") }
    var facultyUsername by remember(initial) { mutableStateOf(initial?.facultyUsername ?: "faculty1") }
    var facultyName by remember(initial) { mutableStateOf(initial?.facultyName ?: (currentUser?.displayName ?: "Faculty")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val candidate = TimetableEntry(
                    id = initial?.id ?: repository.nextTimetableId(),
                    subject = subject.ifBlank { "Untitled" },
                    batch = batch,
                    day = day,
                    startTime = startTime,
                    endTime = endTime,
                    room = room,
                    facultyUsername = if (isFaculty) (currentUser?.username ?: "faculty1") else facultyUsername.ifBlank { "faculty1" },
                    facultyName = if (isFaculty) (currentUser?.displayName ?: facultyName) else facultyName,
                )
                onSave(candidate)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") })
                OutlinedTextField(value = batch, onValueChange = { batch = it }, label = { Text("Batch") })
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    WeekDay.values().forEach { weekDay ->
                        FilterChip(selected = day == weekDay, onClick = { day = weekDay }, label = { Text(weekDay.label) })
                    }
                }
                OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("Start (HH:MM)") })
                OutlinedTextField(value = endTime, onValueChange = { endTime = it }, label = { Text("End (HH:MM)") })
                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Room") })
                if (!isFaculty) {
                    OutlinedTextField(value = facultyUsername, onValueChange = { facultyUsername = it }, label = { Text("Faculty username") })
                    OutlinedTextField(value = facultyName, onValueChange = { facultyName = it }, label = { Text("Faculty name") })
                }
            }
        }
    )
}

private fun toMinutes(time: String): Int {
    val parts = time.split(":")
    if (parts.size != 2) return 0
    val h = parts[0].toIntOrNull() ?: 0
    val m = parts[1].toIntOrNull() ?: 0
    return h * 60 + m
}

private fun fromMinutes(total: Int): String {
    val bounded = total.coerceIn(0, 23 * 60 + 59)
    val h = bounded / 60
    val m = bounded % 60
    return "%02d:%02d".format(h, m)
}
