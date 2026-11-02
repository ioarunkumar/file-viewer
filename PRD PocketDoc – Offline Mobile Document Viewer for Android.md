# PRD: PocketDoc – Offline Mobile Document Viewer for Android

**Document type:** Product Requirements Document (PRD) **Version:** 1.0 (Draft) **Date:** 2 November 2026 **Working name:** PocketDoc (placeholder; rename freely) **Platform:** Android (APK), phones first, tablets supported

---

## 1. Summary

PocketDoc is a lightweight, fully offline Android app that opens PDFs, Word documents, Excel spreadsheets, PowerPoint presentations, and common text and image formats. It is designed for the phone screen first. Documents are rendered crisply, scroll smoothly, and adapt to a narrow display instead of forcing the user to pinch and pan across a desktop-sized page.

The app has no internet permission, no accounts, no ads, and no cloud dependency. Files never leave the device.

## 2. Problem Statement

People receive documents on WhatsApp, email, and USB or file transfer, and need to read them on a phone. Today's options fall short:

- Many viewers need an internet connection or an account to open Office files.
- Heavy office suites are large (often 100 MB+), slow to start, and designed for desktop layouts.
- Basic built-in viewers show tiny text, misrender tables and slides, or fail on non-PDF formats.
- Free apps often add ads, watermarks, or upsells that interrupt reading.

## 3. Goals and Non-Goals

### 3.1 Goals

1. Open the most common document types reliably, fully offline.
2. Deliver sharp, readable output at any zoom level and screen density.
3. Provide mobile-specific reading experiences (reflow, fit-to-width, slide mode, spreadsheet card view).
4. Start fast and stay light: small install size, low memory use, no lag on mid-range devices.
5. Respect privacy: no network access, no tracking.

### 3.2 Non-Goals (v1)

- Editing or creating documents (view-only in v1).
- Cloud sync, accounts, or collaboration.
- Full desktop-fidelity rendering of every Office feature (macros, complex SmartArt, embedded video, etc.).
- iOS or desktop versions.
- Password cracking or DRM bypass (password-protected files open only with the correct password).

## 4. Target Users

| Persona | Needs |
| --- | --- |
| **Student** | Read lecture PDFs, slides, and spreadsheets on a budget phone, often with poor connectivity |
| **Working professional** | Quickly open attachments (reports, invoices, decks) while commuting |
| **Small business owner / field worker** | View price lists, forms, and sheets offline in remote areas |
| **Older or low-vision user** | Large, readable text with high contrast and easy zoom |

## 5. Supported File Types

| Category | Formats | v1 Support Level |
| --- | --- | --- |
| PDF | .pdf (including encrypted with password) | Full render, search, text selection |
| Word | .docx | Mobile reflow reader + original layout view |
| Excel | .xlsx, .csv, .tsv | Scrollable grid, sheet tabs, card view |
| PowerPoint | .pptx | Slide view + outline view |
| Text | .txt, .md, .log, .json, .xml | Reader with monospace option |
| Images | .jpg, .png, .webp, .gif, .svg | Zoomable viewer |
| Legacy Office | .doc, .xls, .ppt | v1.5 (see Roadmap) |
| OpenDocument | .odt, .ods, .odp | v1.5 |
| eBooks | .epub | v2 |

## 6. User Stories

1. As a user, I tap a PDF in WhatsApp or my file manager and PocketDoc opens it instantly, even in airplane mode.
2. As a user, I open a Word document and the text re-wraps to my screen width so I can read without horizontal scrolling.
3. As a user, I open a wide spreadsheet and can freeze headers, switch sheets, and view a single row as a card.
4. As a user, I open a presentation and swipe through slides, or switch to an outline to read the text quickly.
5. As a user, I search for a word inside a document and jump between matches.
6. As a user, I switch to dark or sepia mode to read comfortably at night.
7. As a user, I find recently opened files and favorites on the home screen.
8. As a user, I share or print the original file from within the viewer.

## 7. Functional Requirements

Priority key: **P0** = must ship in v1, **P1** = should ship in v1, **P2** = later.

### 7.1 File Access and Management

| ID | Requirement | Priority |
| --- | --- | --- |
| F-1 | Open files via the Android system file picker (Storage Access Framework) | P0 |
| F-2 | Register as a handler for supported MIME types so files open from other apps ("Open with") | P0 |
| F-3 | Handle "Share to PocketDoc" intents | P0 |
| F-4 | Recent files list with thumbnail, name, type, and last-opened time | P0 |
| F-5 | Favorites / pinned files | P1 |
| F-6 | Browse device storage by type (PDF, Office, Text, Images) using MediaStore | P1 |
| F-7 | Remember last-read position per file | P0 |
| F-8 | Clear history and cached data from settings | P0 |

### 7.2 Common Viewer Features

| ID | Requirement | Priority |
| --- | --- | --- |
| V-1 | Smooth pinch-to-zoom, double-tap zoom, fit-to-width default | P0 |
| V-2 | Re-render at the current zoom level so text stays sharp (no blurry upscaling) | P0 |
| V-3 | Full-text search with highlighted matches and next/previous navigation | P0 |
| V-4 | Page or slide jump, thumbnail strip, and a draggable fast scroller | P1 |
| V-5 | Themes: light, dark, sepia, and true black; follows system setting by default | P0 |
| V-6 | Keep screen awake while reading (toggle) | P1 |
| V-7 | Text selection and copy | P1 |
| V-8 | Share original file; print via Android print framework | P1 |
| V-9 | Document info (title, size, pages, modified date) | P2 |
| V-10 | Clear, friendly error screens for corrupted or unsupported files | P0 |

### 7.3 PDF

| ID | Requirement | Priority |
| --- | --- | --- |
| P-1 | Vertical continuous scroll and single-page horizontal swipe modes | P0 |
| P-2 | Password-protected PDF prompt | P0 |
| P-3 | Table of contents and bookmarks panel | P1 |
| P-4 | Clickable internal and external links (external links show a confirmation, since the app has no network access of its own) | P1 |
| P-5 | **Mobile Reading Mode:** extract text into a reflowable view with adjustable font size (best effort, for text-based PDFs) | P1 |
| P-6 | Auto-crop white margins to increase text size on small screens | P1 |
| P-7 | Inverted or tinted color rendering for night reading that preserves images where possible | P1 |

### 7.4 Word (.docx)

| ID | Requirement | Priority |
| --- | --- | --- |
| W-1 | **Mobile view (default):** reflowed paragraphs, headings, lists, tables (scrollable or stacked), and inline images | P0 |
| W-2 | **Page view:** approximate original page layout, zoomable | P1 |
| W-3 | Adjustable font size, line spacing, and font family | P0 |
| W-4 | Headers/footers, footnotes, hyperlinks, and basic text formatting | P0 |
| W-5 | Display tracked changes and comments (read-only) | P2 |

### 7.5 Excel (.xlsx / .csv)

| ID | Requirement | Priority |
| --- | --- | --- |
| X-1 | Sheet tabs, scrollable grid with column/row headers and smooth fling | P0 |
| X-2 | Display cached formula results correctly (no recalculation needed in v1) | P0 |
| X-3 | Respect number, date, currency, and percentage formats, cell fills, fonts, borders, and merged cells | P0 |
| X-4 | Freeze panes and "freeze first row/column" toggle | P0 |
| X-5 | **Card view:** show one row at a time as label/value pairs for wide tables | P1 |
| X-6 | Tap a cell to see its full content in a bottom sheet | P0 |
| X-7 | Search across sheets; filter/sort for viewing only | P1 |
| X-8 | Basic charts rendered as images or simple native charts | P2 |

### 7.6 PowerPoint (.pptx)

| ID | Requirement | Priority |
| --- | --- | --- |
| S-1 | Slide view with swipe navigation and correct aspect ratio | P0 |
| S-2 | Render text, images, shapes, tables, and slide backgrounds/themes | P0 |
| S-3 | Outline view: all slide text as a readable list | P0 |
| S-4 | Speaker notes panel | P1 |
| S-5 | Landscape auto-fit and fullscreen presentation mode | P1 |
| S-6 | Slide thumbnail grid | P1 |
| S-7 | Animations and transitions (not rendered; static final state shown) | Out of scope |

## 8. Mobile-Specific Experience Requirements

- **One-handed use:** primary controls sit in the bottom half of the screen; top bars auto-hide while reading.
- **Gestures:** tap center to toggle controls, swipe edges for page turn (configurable), volume keys for page turn (optional).
- **Touch targets:** minimum 48 dp.
- **Orientation:** supports portrait, landscape, and tablet split-screen; keeps position on rotation.
- **Accessibility:** TalkBack support for the UI and for extracted text, font scaling, high-contrast theme.
- **Material 3** design with dynamic color on Android 12+.
- **Languages:** UI localization-ready; correct rendering of RTL and complex scripts (Devanagari, Arabic, CJK) in all formats.

## 9. Non-Functional Requirements

### 9.1 Performance

| Metric | Target (mid-range device, 4 GB RAM) |
| --- | --- |
| Cold start to home screen | < 1.2 s |
| Open 10-page PDF to first visible page | < 600 ms |
| Open 200-page PDF to first visible page | < 1.5 s |
| Open 5 MB .docx to first visible content | < 2 s |
| Open 10 MB .xlsx (100k cells) to first visible rows | < 3 s |
| Scroll and zoom frame rate | 60 fps (no sustained jank) |
| Peak memory while viewing | < 250 MB for typical files |
| Install size (APK, arm64) | < 25 MB for v1 |

### 9.2 Quality

- Text must be rendered as vector at every zoom level, not as a stretched bitmap.
- Output must honor device density and support wide-gamut and HDR displays where available.
- Crash-free sessions of 99.5% or better.

### 9.3 Privacy and Security

- **No INTERNET permission** declared in the manifest; this is both a feature and a verifiable claim.
- No analytics or third-party SDKs that collect data. Local-only, opt-in crash logs that the user can export manually.
- Documents are parsed in an isolated, defensive manner: size limits, zip-bomb protection, and time limits on parsing.
- Internal cache is private to the app and can be cleared.

### 9.4 Compatibility

- Android 8.0 (API 26) and above.
- ARM64 primary; ARMv7 and x86\_64 provided as separate APKs or a universal APK.

### 9.5 Open-Source and Licensing

- The app is built on open-source components only; no proprietary SDKs, ad networks, or analytics libraries.
- Allowed licenses for core dependencies: Apache 2.0, MIT, BSD-2/BSD-3, MPL 2.0 (used unmodified and kept in a separate module), and SIL OFL or Apache 2.0 for fonts.
- Disallowed: AGPL and GPL libraries (for example MuPDF and iText) unless the whole app is deliberately released under a compatible license.
- A generated "Open-source licenses" screen ships in the app, and CI fails the build if a disallowed license is added.

## 10. Information Architecture and Key Screens

1. **Home:** search bar, Recent, Favorites, "Open file" button, filter chips by file type.
2. **Browse:** storage locations and type-based browsing.
3. **PDF Viewer:** page canvas, search, thumbnails, TOC, reading mode toggle.
4. **Document Reader (Word and text):** reflowed content, text appearance sheet.
5. **Spreadsheet Viewer:** grid, sheet tabs, card view toggle, cell detail sheet.
6. **Presentation Viewer:** slide pager, outline, notes, thumbnails.
7. **Settings:** theme, default view modes, page-turn behavior, cache, about and licenses.

## 11. Success Metrics

Because the app collects no analytics, success is measured through Play Store/APK distribution data, public ratings, and optional in-app feedback.

| Metric | Target |
| --- | --- |
| Successful-open rate in internal test corpus (500 real-world files) | ≥ 98% for PDF, ≥ 95% for DOCX/XLSX/PPTX |
| Average store rating | ≥ 4.4 |
| Cold start and open-time targets met on 3 reference devices | 100% |
| Crash-free sessions | ≥ 99.5% |
| Uninstall within 7 days | < 30% |

## 12. Release Plan

| Phase | Scope | Indicative Duration |
| --- | --- | --- |
| **Alpha** | PDF viewer, file picker, intent handling, recents | 4 weeks |
| **Beta 1** | Text, images, CSV/XLSX grid, themes, search | 4 weeks |
| **Beta 2** | DOCX mobile reader, PPTX slide and outline view | 5 weeks |
| **v1.0 (APK release)** | Polish, accessibility, performance tuning, corpus testing | 3 weeks |
| **v1.5** | Legacy .doc/.xls/.ppt and ODF through the optional LibreOfficeKit high-fidelity engine; PDF annotations | Later |
| **v2** | EPUB, simple editing, optional text-to-speech | Later |

## 13. Risks and Mitigations

| Risk | Impact | Mitigation |
| --- | --- | --- |
| Office formats are complex; layout differs from desktop Word/PowerPoint | Users see slightly different output | Be explicit about the mobile-first goal; offer Page view; maintain a corpus of real files for regression tests |
| Large files cause out-of-memory errors | Crashes | Streaming parsers, tile rendering, bitmap pooling, hard limits with friendly messages |
| APK size grows if a full office engine is bundled | Slow download, low adoption | Keep v1 engine light; deliver the heavy engine as an optional separate module in v1.5 |
| Open-source license conflicts (for example AGPL libraries) | Legal exposure | Use permissively licensed libraries (Apache 2.0, BSD, MIT) for the core; complete a license audit before release |
| Android storage permission changes | Files not discoverable | Use Storage Access Framework and MediaStore; avoid broad storage permissions |
| Fonts missing offline, causing wrong glyphs | Poor display quality | Bundle a compact set of fonts with wide script coverage (such as Noto subsets) and map common Office fonts to metric-compatible substitutes |

## 14. Open Questions

1. Distribution: Play Store, F-Droid, direct APK, or all three? This affects signing, size, and policy requirements.
2. Monetization: free and open source, one-time purchase, or donation-based?
3. Is a high-fidelity Office engine (LibreOfficeKit, MPL 2.0; larger and heavier) acceptable as an optional download for v1.5?
4. Which UI languages are needed at launch?
5. Should basic annotation (highlight, draw, sign) be pulled into v1?

## 15. Appendix: Glossary

- **SAF:** Storage Access Framework, Android's system file picker.
- **Reflow:** re-wrapping text to fit the screen width instead of using fixed page dimensions.
- **Tile rendering:** drawing a page in small bitmap tiles at the current zoom, so only the visible region is rendered.
- **APK:** Android Package, the installable app file.

## 16. Open-Source Technology Approach

Every supported format is handled by an open-source engine, so the app stays small, auditable, and free of licensing fees. The full module map, versions to verify, and integration rules are in the companion TID (section 16).

### 16.1 Engine per File Type

| File type | Open-source engine | License | Notes for product scope |
| --- | --- | --- | --- |
| PDF | PDFium through an Android wrapper; PdfBox-Android for metadata | BSD-3 / Apache 2.0 | Sharp vector rendering, search, passwords; `androidx.pdf` and the platform `PdfRenderer` as fallbacks |
| Word (.docx) | Android `XmlPullParser` and `java.util.zip` plus our own reflow renderer | Platform | Mobile reflow is a product choice, not a limitation of the parser |
| Excel (.xlsx) | fastexcel-reader with Aalto XML, or a custom `XmlPullParser` reader | MIT / Apache 2.0 | Cached formula values only in v1 |
| CSV / TSV | Apache Commons CSV | Apache 2.0 | Delimiter and encoding auto-detected |
| PowerPoint (.pptx) | Android `XmlPullParser` and `java.util.zip` plus our own slide renderer | Platform | Static final state; no animations |
| Text and Markdown | Android ICU `CharsetDetector`, commonmark-java | Platform / BSD-2 | Encoding auto-detection |
| Images and SVG | Coil, Telephoto, AndroidSVG | Apache 2.0 | Large images use tiled decoding |
| Legacy Office and OpenDocument (v1.5) | LibreOfficeKit as an optional module | MPL 2.0 | Separate download to protect the v1 size target |
| EPUB (v2) | Readium Kotlin Toolkit | BSD-3 | Planned |

### 16.2 Product Implications

- **Size:** the v1 target of under 25 MB (arm64) depends on not bundling a full office suite; LibreOfficeKit stays optional.
- **Fidelity:** custom Office renderers favor readability on a phone over pixel-exact desktop layout, which is why Word and Excel offer mobile-first views and Page view is secondary.
- **Maintenance:** several Android PDF wrappers are lightly maintained, so the engine is wrapped behind one interface and can be swapped without changing the UI.
- **Verification before build:** library versions, maintenance status, and APK-size impact are confirmed in the TID's open items (section 16.7) before the Alpha milestone starts.
