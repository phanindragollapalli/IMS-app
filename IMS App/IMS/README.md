# IMS Android Prototype

Single-activity Jetpack Compose prototype for the IMS assignment.

## Implemented modules

- Dashboard
- Examinations
- Attendance

## Notes

- UI is fully native Compose.
- Data is stored in-memory through `DemoRepository` to simulate offline end-to-end flows.
- `APPIDENTIFIER` is defined in `app/build.gradle.kts` as a `BuildConfigField`.

## Run

- Open the project in Android Studio.
- Sync Gradle.
- Run the `app` module on an emulator or device.