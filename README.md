# PocketDoc

PocketDoc is an Android document viewer prototype built with Kotlin and Jetpack Compose. Choose a file from the Android system picker to read it on your device. The app does not request internet access.

## Supported files

- PDF
- Legacy Excel workbooks (`.xls`) and CSV
- Legacy PowerPoint presentations (`.ppt`), displayed as readable slide text
- Text files: `.txt`, `.md`, `.tsv`, `.json`, `.xml`, and `.log`
- Images: `.jpg`, `.jpeg`, `.png`, `.webp`, `.gif`, and `.bmp`

Newer Office formats such as `.docx`, `.xlsx`, and `.pptx` are not currently supported. This is a prototype; see the product and technical documents for the broader roadmap.

## Requirements

- Android Studio with Android SDK Platform 35 installed
- JDK 17
- An Android device or emulator running Android 8.0 (API 26) or later

## Build and run

Open the project in Android Studio, allow Gradle to sync, then run the `app` configuration on a device or emulator.

Alternatively, from PowerShell in the project directory:

```powershell
.\gradlew.bat assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

To install on a connected device or running emulator:

```powershell
.\gradlew.bat installDebug
```

## Project layout

- `app/` — Android application source and resources
- `gradle/` — Gradle wrapper configuration
- `PRD PocketDoc – Offline Mobile Document Viewer for Android.md` — product requirements and roadmap
- `TID PocketDoc – Technical Implementation Document.md` — technical design and implementation guidance

## License

No project license is specified yet. Check the licenses of the app's dependencies before redistributing it.
