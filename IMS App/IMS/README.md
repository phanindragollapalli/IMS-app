# IMS — Institute Management System

Single-activity Android app built with Kotlin and Jetpack Compose. All data is simulated in-memory via `DemoRepository` with session persistence through `SharedPreferences`.

## Run

1. Open in Android Studio.
2. Sync Gradle.
3. Run the `app` module on an emulator or device (minSdk 24).

## Demo Credentials

| Username   | Password      | Role    |
|------------|---------------|---------|
| `admin`    | `admin123`    | Admin   |
| `faculty1` | `faculty123`  | Faculty |
| `student1` | `student123`  | Student |

Sessions persist for **3 days** via `SharedPreferences`.

---

## Project Structure

```
app/src/main/java/com/example/ims_app/
├── MainActivity.kt              # Single activity, session bootstrap
├── data/
│   ├── Models.kt                # All data classes and enums
│   ├── DemoRepository.kt        # In-memory data store + business logic
│   └── SessionManager.kt        # SharedPreferences auth + settings persistence
├── navigation/
│   └── AppNav.kt                # NavHost, routes, top bar, Scaffold
├── screens/
│   ├── LoginScreen.kt           # Credential form with password toggle
│   ├── DashboardScreen.kt       # Metrics, search, module grid
│   ├── TimetableScreen.kt       # Weekly board + list view, editor dialog
│   ├── AttendanceScreen.kt      # Mark/report modes, student drill-down
│   ├── AdminControlsScreen.kt   # Course/batch/subject/transfer management
│   ├── SettingsScreen.kt        # Localization + academic settings
│   └── StubModuleScreen.kt      # Placeholder for unimplemented modules
└── ui/theme/
    ├── Color.kt                 # Color palette
    ├── Theme.kt                 # Material3 theme
    └── Type.kt                  # Typography
```

---

## Architecture

- **Single Activity** — `MainActivity` is the only activity. It bootstraps `SessionManager`, restores session state, and switches between `LoginScreen` and `ImsAppNav`.
- **No ViewModel** — State lives directly in `DemoRepository` (a Kotlin `object` with Compose `mutableStateOf` / `mutableStateListOf` fields). Changes trigger recomposition automatically.
- **Navigation** — Jetpack Navigation Compose with `NavHost` inside a `Scaffold`. All routes are string constants in the `Routes` object. Navigation uses `navigateSingleTop` to avoid duplicate back-stack entries.

---

## File-by-File Documentation

### `MainActivity.kt`

Entry point. Creates `SessionManager`, checks for an existing valid session, hydrates `DemoRepository` with the logged-in user's settings (localization + general), and renders either `LoginScreen` or `ImsAppNav`. Logout clears the session and resets `currentUser` to `null`.

### `data/Models.kt`

Pure data definitions — no logic.

| Type | Purpose |
|------|---------|
| `UserRole` | Enum: `Admin`, `Faculty`, `Student` |
| `AttendanceStatus` | Enum: `Present`, `Absent`, `Leave` |
| `AttendanceReportType` | Enum: `Daily`, `Monthly`, `SubjectWise` |
| `WeekDay` | Enum: Monday–Saturday |
| `AppLanguage`, `AppCurrency`, `AppTimeZone` | Localization enums (single-value each for now) |
| `UserLocalizationSettings` | Language, country, currency, timezone bundle |
| `GradingSystem`, `TermType`, `GradeScaleBand` | Academic config enums/data |
| `GeneralSettings` | Grading system, pass mark, grade scale, term type, attendance threshold |
| `Course` | `id`, `code`, `name` |
| `BatchSubject` | Subject name + elective flag |
| `BatchTransferLog` | Transfer audit trail record |
| `DashboardMetric` | Title/value/subtitle for dashboard cards |
| `UserAccount` | Full credential record (username, password, role, display name, batch, rollNo) |
| `SessionUser` | Subset of `UserAccount` without password (runtime identity) |
| `TimetableEntry` | Slot definition: subject, batch, day, start/end time, room, faculty |
| `AttendanceEntry` | Per-student attendance row: status + optional remark |
| `StudentRecord` | Student identity within a batch |
| `AttendanceSheet` | A batch+date+subject attendance session containing a list of `AttendanceEntry` |
| `AttendanceReportSummary` | Aggregated present/absent/leave counts |

### `data/DemoRepository.kt`

Singleton (`object`) holding all app state and business logic. Key areas:

**State fields** — `currentUser`, `activeRole`, `selectedBatch`, `selectedDate`, `selectedSubject`, `searchQuery`, `localizationSettings`, `generalSettings`. All are Compose-observable.

**Timetable logic:**
- Validation: slots must be 08:30–18:30, 30–90 min duration, no overlap with lunch (13:00–14:00).
- Conflict detection: same batch + day + overlapping time range.
- Faculty workload caps: max 3 classes/day, max 12 classes/week.
- Course limit: max 3 classes/week per subject per batch.
- CRUD: `saveTimetableEntry()` (create/update), `deleteTimetableEntry()`.

**Attendance logic:**
- `activeAttendanceSheet()` — returns existing sheet or creates a default one from the student roster.
- `upsertAttendanceSheet()` — inserts or updates a sheet.
- `attendanceSheetsFor()` — filters sheets by report type (daily/monthly/subject-wise), with optional batch/date/month/subject filters. Students auto-filter to their own batch.
- `attendanceReportSummary()` — aggregates present/absent/leave counts. Students see only their own entries.

**Academic catalog (Admin Controls):**
- Courses: add/update/delete with duplicate code checks.
- Batches: add/remove/rename with cascade to timetable, attendance, and students.
- Subjects: add/update/delete per batch; deletion blocked if attendance or timetable records exist.
- Batch transfers: admin-only one-step transfer of students between batches, with audit logging.

**Settings:**
- `updateGeneralSettings()` — Admin can edit all fields; Faculty limited (can't change term type, attendance threshold, auto IDs); Students can't edit.
- `sanitizeGeneralSettings()` — enforces grade scale ordering (A ≥ B ≥ C ≥ D ≥ pass mark) and threshold bounds.

### `data/SessionManager.kt`

Handles persistence via `SharedPreferences`:
- **Auth**: `login()` validates credentials against serialized user list, writes login timestamp. `logout()` clears session keys. `isSessionValid()` checks 3-day expiry.
- **User storage**: Seed users are written on first launch. Users are serialized as `username|password|role|displayName|batch|rollNo` joined by `;;`.
- **Settings persistence**: Localization settings are stored per-user with prefixed keys. General settings are stored globally.

### `navigation/AppNav.kt`

- Defines all route constants in the `Routes` object (13 routes total).
- `ImsAppNav` composable: sets up `Scaffold` with `TopAppBar` (back button on non-dashboard routes, Admin Controls icon for admins, Settings icon, Logout button) and `NavHost`.
- Each route maps to its screen composable. Dashboard receives navigation callbacks for all 10 module tiles.
- `navigateSingleTop()` extension prevents duplicate entries and preserves/saves state.

### `screens/LoginScreen.kt`

Simple credential form. Username + password fields, password visibility toggle, error message on failed login. Calls `onLogin(username, password)` callback; parent (`MainActivity`) drives `SessionManager.login()`.

### `screens/DashboardScreen.kt`

Three sections in a `LazyColumn`:

1. **Welcome card** — greeting, search bar (filters module tiles and timetable entries), current user/role display.
2. **Metric cards** — row of 3: total students, visible timetable slots, overall attendance %.
3. **Module grid** — 3-column grid of 10 module tiles (Timetable, Attendance, Admission, Student Details, Examinations, Manage Users, HR, Finance, Messages, Manage News). Tiles are searchable. Each navigates to its route.

Private composables: `ModuleGridCard` (icon + label tile), `MetricCard`, `SectionHeader`.

### `screens/TimetableScreen.kt`

Two view modes toggled by `FilterChip`:

- **Weekly Board** (`WeeklyTimetableBoard`) — day × time-slot grid. Each cell shows a class card with subject, faculty, room, time. Admin/Faculty can long-press-drag cards between cells to reschedule. Drop validates via `saveTimetableEntry()`. Edit/Duplicate buttons on editable cards.
- **List** — simple card-per-entry with Edit/Duplicate/Delete buttons.

Batch filter chips (Admin/Faculty only; students see only their batch). FAB to add new entry (hidden for students).

**Editor dialog** (`TimetableEditorDialog`) — fields: subject, batch, day (chip row), start time, duration (30–90 min), room. Faculty see locked `facultyUsername`/`facultyName`; Admin can set any.

Helper functions: `toMinutes()`, `fromMinutes()` for HH:MM ↔ int conversion.

### `screens/AttendanceScreen.kt`

Branching by role:

**Faculty/Admin** — two modes via chip toggle:
- **Mark mode** — pick batch/date/subject via filter chips. Shows the active sheet's student rows. Each `AttendanceRowInline` has P/A/L status chips + optional remark field. Save button writes back via `upsertAttendanceSheet()`. Success banner auto-dismisses after 5 seconds.
- **Reports mode** — pick report type (daily/monthly/subject-wise), filter by batch, then type-specific filter (date/month/subject). Shows matching sheets with read-only attendance rows and an aggregate summary card (count + %).

**Student** — separate two-level view:
- `StudentCourseSummaryView` — table of all subjects with total/present/absent columns. Tap a course to drill down.
- `StudentCourseDetailView` — date-wise list for a single course, showing status icons (✓ green, ✗ red, ✓ orange for leave) per date. Back button returns to summary.

### `screens/AdminControlsScreen.kt`

Admin-only screen (accessible via top-bar icon on dashboard). Four `SectionCard` blocks in a `LazyColumn`:

1. **Course management** — add/edit/delete courses. Duplicate code validation.
2. **Batch management** — add/rename/remove batches. Removal cascades to timetable + attendance.
3. **Subjects and electives** — per-batch subject list. Add subjects, toggle core/elective, delete (blocked if referenced).
4. **Batch transfers** — select source/target batch, pick students via chips, enter reason, execute. Shows last 5 transfer log entries.

Helper composables: `SectionCard` (titled card wrapper), `EnumDropdownField` (reusable `ExposedDropdownMenuBox`).

### `screens/SettingsScreen.kt`

Two sections:

1. **Language and region** — dropdowns for language, currency, timezone; text field for country. Saved per-user via `SessionManager`.
2. **Academic** — term type dropdown, attendance threshold field. Admin-only fields; non-admin sees a notice.

Uses `SettingsSectionCard` and `SettingsDropdownField` private composables (same pattern as `AdminControlsScreen`).

### `screens/StubModuleScreen.kt`

Generic placeholder for unimplemented modules (Admission, Student Details, Examinations, Manage Users, HR, Finance, Messages, Manage News). Takes `title`, `icon`, `description`, and `capabilities` list. Renders a hero card + bullet list + "stub" notice.

### `ui/theme/`

Standard Material3 theme files. `Color.kt` defines the palette, `Theme.kt` applies light/dark schemes, `Type.kt` sets typography.

---

## Key Business Rules

- **Timetable window**: 08:30–18:30, lunch break 13:00–14:00. Slots: 30–90 min.
- **Faculty workload**: ≤ 3 classes/day, ≤ 12 classes/week.
- **Course weekly limit**: ≤ 3 classes per subject per batch per week.
- **Subject deletion**: blocked if attendance records or timetable entries reference it.
- **Batch deletion**: cascades — removes timetable entries, attendance sheets, students.
- **Grade scale ordering**: enforced as minA ≥ minB ≥ minC ≥ minD ≥ passMarkThreshold, all in [0, 100].
- **Session expiry**: 3 days from login timestamp.