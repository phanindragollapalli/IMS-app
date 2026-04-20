# IMS Android Prototype

Single-activity Jetpack Compose prototype for the IMS assignment.

## Implemented modules

- Dashboard
- Timetable
- Attendance

## Notes

- UI is fully native Compose.
- Data/workflows are simulated in-memory through `DemoRepository`.
- Login is local and role-based (`admin`, `faculty1`, `student1`) with 3-day session persistence.
- Timetable supports create/edit/delete, overlap checks, and faculty workload checks.
- Attendance supports mark + reports (daily/monthly/subject-wise) with optional remarks.
- Detailed code documentation is available in `CODE_DOCUMENTATION.md`.

## Run

- Open the project in Android Studio.
- Sync Gradle.
- Run the `app` module on an emulator or device.