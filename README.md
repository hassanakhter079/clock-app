# AMOLED Clock — APK Compilation Guide

**Compatibility:** Any Android Device (Phones, Tablets, Foldables) · Android 8.0+ (API 26–36+)  
**Display:** Fully responsive for all screen sizes & resolutions (optimized for AMOLED / OLED)  
**Architecture:** Universal (arm64-v8a, armeabi-v7a, x86, x86_64)  

---

## 📲 Download APK

Download the latest pre-compiled, signed APK directly from the [Releases](https://github.com/hassanakhter079/clock-app/releases/latest) page:
- 📥 **[Download Clock.apk](https://github.com/hassanakhter079/clock-app/releases/latest/download/Clock.apk)**

---

## Files in this package

| File | Purpose |
|---|---|
| `index.html` | Complete clock app — copy to `assets/` |
| `MainActivity.java` | Android Activity hosting the WebView |
| `AndroidManifest.xml` | App manifest with permissions |
| `build.gradle` | Gradle build config |
| `proguard-rules.pro` | Release build obfuscation rules |

---

## Step-by-step: Compile to APK

### 1. Install Android Studio
Download from https://developer.android.com/studio  
Install with **Android SDK 36** (Android 16) and **Android 8 (API 26)**.

### 2. Create a new project

1. **File → New → New Project**
2. Choose **"Empty Views Activity"** (not Compose)
3. Set:
   - **Package name:** `com.amoledclock.app`
   - **Language:** Java
   - **Minimum SDK:** API 26 (Android 8.0)
   - **Target SDK:** API 36

### 3. Place the files

```
app/
└── src/
    └── main/
        ├── java/com/amoledclock/app/
        │   └── MainActivity.java       ← copy here
        ├── assets/
        │   ├── index.html              ← copy here
        │   └── fonts/
        │       ├── JetBrainsMono-Bold.ttf
        │       ├── JetBrainsMono-Regular.ttf
        │       ├── PlusJakartaSans-SemiBold.woff2
        │       └── PlusJakartaSans-Regular.woff2
        └── AndroidManifest.xml         ← replace existing
```

Replace contents of `build.gradle` (Module: app) with the provided file.  
Replace `proguard-rules.pro` with the provided file.

### 4. Download fonts (free, OFL licensed)

**JetBrains Mono:**  
https://github.com/JetBrains/JetBrainsMono/releases  
→ Download `JetBrainsMono-Bold.ttf` and `JetBrainsMono-Regular.ttf`

**Plus Jakarta Sans:**  
https://fonts.google.com/specimen/Plus+Jakarta+Sans  
→ Click "Download family" → extract `PlusJakartaSans-Regular.woff2` and `PlusJakartaSans-SemiBold.woff2`

> **Note:** The app works without bundled fonts — it falls back to Roboto Mono. Fonts just make it look sharper.

### 5. Build debug APK

```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

The APK will appear at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 6. Install on Android Device

**Via ADB:**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

**Via Direct APK / USB transfer:**  
Copy the APK to your phone or tablet, tap the file in your file manager, and allow "Install unknown apps."

### 7. Release APK (signed)

1. **Build → Generate Signed Bundle / APK → APK**
2. Create or use an existing keystore
3. Select **release** build variant
4. APK will be at `app/build/outputs/apk/release/app-release.apk`

---

## Key features

| Feature | Implementation |
|---|---|
| **Smooth seconds bar** | CSS custom property `--bar-pct` driven by `requestAnimationFrame` at 120fps — GPU composited, zero layout thrash |
| **AMOLED themes** | 12 color palettes (Cyan, Emerald, Amber, Violet, Gold, Pink, Ocean, Lime…) |
| **12H / 24H** | Toggle, persisted in `localStorage` and native `SharedPreferences` |
| **Blinking colon** | Millisecond-accurate (not second-based) |
| **OLED burn-in protection** | 1–4px pixel drift every minute via CSS transform |
| **Calendar** | Full month grid, week/day info, swipe navigation |
| **Stopwatch** | rAF-driven, 10ms precision display |
| **Countdown timer** | 1m / 3m / 5m / 10m / 25m Focus presets |
| **Hourly chime** | Web Audio API sine-wave synth + vibration |
| **Screen wake lock** | Web API + native `FLAG_KEEP_SCREEN_ON` |
| **Night dimmer** | Overlay opacity + native window brightness |
| **Landscape layout** | Clock + calendar side-by-side on wide screens & tablets |
| **Edge-to-edge** | Android 11+ `setDecorFitsSystemWindows(false)` |
| **Idle auto-hide** | Top/bottom UI fades after 4 seconds |

---

## AndroidBridge API (JS ↔ Native)

| JS call | Native action |
|---|---|
| `AndroidBridge.vibrate(ms)` | `VibrationEffect.createOneShot` |
| `AndroidBridge.setKeepScreenOn(bool)` | `FLAG_KEEP_SCREEN_ON` |
| `AndroidBridge.setScreenBrightness(0.0–1.0)` | `LayoutParams.screenBrightness` |
| `AndroidBridge.getTheme()` | `SharedPreferences.getString("theme")` |
| `AndroidBridge.setTheme(name)` | `SharedPreferences.putString("theme")` |
| `AndroidBridge.syncSettings(blink,sec,24h)` | Save to native prefs |
| `AndroidBridge.pinWidget(type)` | `AppWidgetManager.requestPinAppWidget` |

---

## Troubleshooting

**Fonts not loading** → Check the path: `assets/fonts/JetBrainsMono-Bold.ttf` (case-sensitive).  
**WebView blank** → Enable internet permission if loading remote resources; check `adb logcat`.  
**Vibration not working** → Add `<uses-permission android:name="android.permission.VIBRATE" />` to manifest (already included).  
**Screen dims** → The `FLAG_KEEP_SCREEN_ON` flag requires the activity to be in foreground.
