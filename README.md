## Team

This project was completed as part of the Design and Analysis of Software Systems(DASS) course by:

- [@phanindragollapalli](https://github.com/phanindragollapalli)
- [@YVKartikeya9](https://github.com/YVKartikeya9)

---

# IMS — Institute Management System

Single-activity Android app built with Kotlin and Jetpack Compose. All data is simulated in-memory via `DemoRepository` with session persistence through `SharedPreferences`.

## Project Overview

IMS is an Institute Management System designed to handle various academic and administrative tasks. The app is built using modern Android development practices, specifically Kotlin and Jetpack Compose for the UI. It features a single-activity architecture where state is managed in-memory and sessions persist via `SharedPreferences`.

### Key Features
- **Role-based Access Control**: Different views and capabilities for Admin, Faculty, and Student roles.
- **Timetable Management**: Weekly board and list views with drag-and-drop rescheduling (for admins/faculty) and conflict detection.
- **Attendance Tracking**: Mark attendance, view daily/monthly/subject-wise reports, and student-specific drill-down views.
- **Admin Controls**: Manage courses, batches, subjects, and batch transfers.
- **Settings**: Custom localization (language, timezone, currency) and academic settings (grading system, term type).

## Detailed Setup Guide

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (latest version recommended).
- Java Development Kit (JDK) 17 or higher.
- An Android device or emulator running API level 24 (Android 7.0) or higher.

### Installation Steps

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd IMS-app
   ```

2. **Open the project in Android Studio:**
   - Launch Android Studio.
   - Select **Open** and navigate to the `IMS-app/IMS App/IMS` directory.
   - Click **OK**.

3. **Sync Gradle:**
   - Android Studio should automatically start syncing the project. If not, click on **File > Sync Project with Gradle Files**.
   - Wait for the build process to complete and ensure there are no errors.

4. **Run the Application:**
   - Select your target device or emulator from the device dropdown in the toolbar.
   - Click the **Run 'app'** button (green play icon) or press `Shift + F10`.
   - The app will compile, install, and launch on the selected device.

### Demo Credentials

You can log in using the following built-in demo accounts:

| Username   | Password      | Role    |
|------------|---------------|---------|
| `admin`    | `admin123`    | Admin   |
| `faculty1` | `faculty123`  | Faculty |
| `student1` | `student123`  | Student |

*Note: Sessions persist for 3 days via local storage.*