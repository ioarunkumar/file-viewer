<div align="center">

# 📄 PocketDoc

**An offline document viewer for Android, built with Kotlin and Jetpack Compose.**

Pick a file from the system picker and read it on your device. No internet permission. No uploads.

![Platform](https://img.shields.io/badge/platform-Android%208.0%2B-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?logo=kotlin&logoColor=white)
![JDK](https://img.shields.io/badge/JDK-17-orange)
![Status](https://img.shields.io/badge/status-prototype-yellow)

[Features](#-features) · [Supported Files](#-supported-files) · [Getting Started](#-getting-started) · [Project Structure](#-project-structure) · [Roadmap](#-roadmap) · [License](#-license)

</div>

---

## ✨ Features

- **Fully offline:** the app does not request internet access
- **System file picker:** open documents using Android's built-in picker
- **Multiple formats:** PDFs, spreadsheets, slides, text files, and images
- **Modern UI:** built entirely with Jetpack Compose

> **Note:** PocketDoc is a prototype. See the [product](#-project-structure) and technical documents for the broader roadmap.

## 📂 Supported Files

| Type | Formats | Notes |
|------|---------|-------|
| PDF | `.pdf` | |
| Spreadsheets | `.xls`, `.csv` | Legacy Excel workbooks |
| Presentations | `.ppt` | Legacy PowerPoint, shown as readable slide text |
| Text | `.txt`, `.md`, `.tsv`, `.json`, `.xml`, `.log` | |
| Images | `.jpg`, `.jpeg`, `.png`, `.webp`, `.gif`, `.bmp` | |

> ⚠️ Newer Office formats (`.docx`, `.xlsx`, `.pptx`) are **not currently supported**.

## 🚀 Getting Started

### Requirements

| Requirement | Version |
|-------------|---------|
| Android Studio | With Android SDK Platform 35 installed |
| JDK | 17 |
| Device / emulator | Android 8.0 (API 26) or later |

### Run from Android Studio

1. Open the project in Android Studio.
2. Let Gradle finish syncing.
3. Select the `app` run configuration and run it on a device or emulator.

### Build from the command line

From PowerShell in the project directory:

```powershell
.\gradlew.bat assembleDebug
```

The debug APK is written to:

```
app/build/outputs/apk/debug/app-debug.apk
```

### Install on a device

With a device connected or an emulator running:

```powershell
.\gradlew.bat installDebug
```

## 🗂 Project Structure

```
PocketDoc/
├── app/        # Android application source and resources
├── gradle/     # Gradle wrapper configuration
├── PRD PocketDoc – Offline Mobile Document Viewer for Android.md
└── TID PocketDoc – Technical Implementation Document.md
```

| Document | Purpose |
|----------|---------|
| `PRD PocketDoc – Offline Mobile Document Viewer for Android.md` | Product requirements and roadmap |
| `TID PocketDoc – Technical Implementation Document.md` | Technical design and implementation guidance |

## 🛣 Roadmap

- [ ] Support for `.docx`, `.xlsx`, and `.pptx`

More details are in the product requirements document.

## 📜 License

No license is required.
