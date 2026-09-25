# ============================================================
# ProGuard Rules — AMOLED Clock
# ============================================================

# Keep the activity
-keep class com.amoledclock.app.MainActivity { *; }

# Keep the AndroidBridge inner class so JS can call it
-keep class com.amoledclock.app.MainActivity$AndroidBridge { *; }

# Keep @JavascriptInterface annotated methods (CRITICAL)
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# WebView-related
-keepclassmembers class android.webkit.** { *; }
-keepclassmembers class com.android.internal.http.multipart.** { *; }

# Prevent obfuscating app widget classes if you add them
# -keep class com.amoledclock.app.ClockWidgetProvider { *; }
