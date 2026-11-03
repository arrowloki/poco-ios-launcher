# POCO iOS-Inspired Minimal Launcher 📱✨

A high-performance, lightweight, and offline-first Android launcher specifically optimized for **POCO & Xiaomi smartphones** (Android 10+ / API 29 up to Android 14+). It transforms the Android home screen into an elegant, fluid, iOS-inspired interface while strictly respecting Android system APIs, privacy boundaries, and security restrictions.

---

## ✨ Features

### 1. 🔲 Continuous Superellipse Continuous Squircles
- Continuous curvature mathematically modelled ($x^4 + y^4 = r^4$) instead of standard rounded rectangles.
- Memory-bounded 24MB asynchronous `LruCache` with zero main-thread disk or rasterization stalls.

### 2. 📂 3x3 Mini-Grid Folders & Modal Expansion
- Miniaturized 9-app preview icons rendered live inside squircle folder containers.
- Animated centered modal folder dialog with live editable title, 3x3 full-size icon grid, and tap-outside dismiss.

### 3. 🔍 Sub-Millisecond Spotlight Search
- Downward swipe on home screen instantly reveals iOS-style Spotlight search with Siri suggestions and instant live app filtering.

### 4. 🎛️ Functional iOS-Inspired Control Center
- Swipe down from top-right corner to open.
- Fluid interactive vertical sliders for Screen Brightness and Music Volume (supports both direct tap-to-set and continuous dragging).
- Torch flashlight toggle (`CameraManager.TorchCallback` sync).
- Real-time battery telemetry with charging indicators.
- Live Auto-rotation and Do Not Disturb (Focus) controls.
- Official Android `Settings.Panel` deep links for Wi-Fi, Mobile Data, Bluetooth, and Airplane Mode with zero fake toggles.
- Tactile haptic feedback and upward swipe-to-dismiss gesture.

### 5. 📳 Home Screen Jiggle / Edit Mode
- Alternating subtle icon wobble animation (`-1.8°` to `+1.8°`) with item-hash phase shifts for natural motion.
- Circular minus `(-)` badges on apps, folders, and widgets for quick uninstallation or removal.
- Translucent frosted "Done" pill button to exit edit mode gracefully.
- Long-press context menu: "Edit Home Screen", "Add/Remove Dock", "App Info", and "Uninstall App".

### 6. 🧩 Native Android Home Screen Widgets
- Deep integration with Android's official `AppWidgetHost` and `AppWidgetManager`.
- Add, configure, and remove widgets via the top-left `(+)` button in Edit Mode.
- Formatted inside iOS squircle cards with full-width row span (`GridItemSpan`).

### 7. ⚙️ Grouped Inset Customization Settings
- Theme selector: System, Light, or Dark mode.
- Grid layout selector: 4×5, 4×6, or 5×6.
- Icon size slider: 50dp to 72dp with live feedback.
- System wallpaper chooser shortcut.
- POCO performance toggles: "Reduce Motion" and "Blur & Translucency" toggle to guarantee smooth 120Hz operation on budget 3GB RAM devices.
- **Backup & Restore**: Export full launcher configuration to JSON and restore seamlessly.

---

## 🛠️ Technology Stack

- **Language**: Kotlin 1.9.23 (100% Kotlin DSL)
- **UI Framework**: Jetpack Compose (BOM 2024.04.01), Material 3
- **Architecture**: Clean MVVM + Repository pattern with manual dependency management
- **Persistence**: DataStore Preferences
- **Minimum SDK**: Android 10 (API 29)
- **Target SDK**: Android 14 (API 34)
- **Zero Heavy Third-Party Bloat**: No Flutter, No React Native, No ads, No trackers, No permanent foreground services, No network dependencies for core launcher operations.

---

## 🚀 Download & Installation

You can download the pre-built APK directly from the **[Releases](../../releases)** tab:
- **`app-release.apk`**: Production release build (40 MB, optimized, signed).
- **`app-debug.apk`**: Debug build for development and log profiling.

### Setting as Default Launcher
1. Install `app-release.apk` on your POCO / Android device.
2. Press the **Home** button or open **Settings → Apps → Default Apps → Home app**.
3. Select **POCO Launcher**.

---

## 🧪 Building from Source

```bash
# Clone the repository
git clone https://github.com/arrowloki/poco-ios-launcher.git
cd poco-ios-launcher

# Run unit tests
./gradlew test

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease
```

APKs will be located at:
- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`

---

## 📄 License
Apache 2.0 License. Built for POCO & Android enthusiasts.
