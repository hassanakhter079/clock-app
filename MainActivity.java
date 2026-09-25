package com.amoledclock.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

/**
 * AMOLED Clock — MainActivity
 *
 * Hosts the clock HTML in a full-screen, edge-to-edge WebView.
 * Provides AndroidBridge for JS ↔ native communication.
 *
 * Target: Samsung Galaxy Tab S8+ · Android 16 (API 36)
 * minSdk: 26 (Android 8.0)
 */
public class MainActivity extends Activity {

    private WebView webView;
    private Vibrator vibrator;
    private SharedPreferences prefs;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ── Full-screen, edge-to-edge ──────────────────────────────
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_FULLSCREEN |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );
        // Android 11+ edge-to-edge
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
        } else {
            window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
        // Pure black status/nav bar background
        window.setStatusBarColor(0xFF000000);
        window.setNavigationBarColor(0xFF000000);

        // ── Vibrator ───────────────────────────────────────────────
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vm = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vm != null ? vm.getDefaultVibrator() : null;
        } else {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }

        prefs = getSharedPreferences("amoled_clock", MODE_PRIVATE);

        // ── WebView setup ──────────────────────────────────────────
        webView = new WebView(this);
        webView.setBackgroundColor(0xFF000000);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);          // localStorage
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setAllowFileAccess(true);            // for font files
        settings.setMediaPlaybackRequiresUserGesture(false); // Web Audio API
        settings.setJavaScriptCanOpenWindowsAutomatically(false);

        // Hardware acceleration (on by default for API 14+, explicit here)
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Inject native bridge
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");

        // Prevent WebView from showing error pages
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true; // block external navigation
            }
        });

        // Load from assets
        webView.loadUrl("file:///android_asset/index.html");

        // Full-screen layout
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFF000000);
        root.addView(webView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));
        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
        // Re-apply keep-screen-on in case it was released while paused
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // Block back button — clock is meant to stay on screen
        // Optionally: webView.evaluateJavascript("history.back()", null);
    }

    // ══════════════════════════════════════════════════════════════════
    // ANDROID BRIDGE — JavaScript interface
    // All methods callable from JS as: window.AndroidBridge.method(args)
    // ══════════════════════════════════════════════════════════════════
    private class AndroidBridge {

        /** Trigger device vibration. ms = duration in milliseconds. */
        @JavascriptInterface
        public void vibrate(int ms) {
            if (vibrator == null || !vibrator.hasVibrator()) return;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(ms,
                        VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(ms);
                }
            } catch (Exception e) { /* ignore */ }
        }

        /** Keep the screen on or let it dim normally. */
        @JavascriptInterface
        public void setKeepScreenOn(boolean on) {
            runOnUiThread(() -> {
                if (on) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            });
        }

        /**
         * Set screen brightness (0.0 – 1.0).
         * Note: Requires WRITE_SETTINGS permission for system brightness.
         * Without it, only the window brightness (in-app) changes.
         */
        @JavascriptInterface
        public void setScreenBrightness(float level) {
            runOnUiThread(() -> {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.screenBrightness = Math.max(0.01f, Math.min(1.0f, level));
                getWindow().setAttributes(lp);
            });
        }

        /** Get saved theme from native prefs (so theme persists app restart). */
        @JavascriptInterface
        public String getTheme() {
            return prefs.getString("theme", null);
        }

        /** Save theme to native prefs. */
        @JavascriptInterface
        public void setTheme(String theme) {
            prefs.edit().putString("theme", theme).apply();
        }

        /**
         * Sync clock display settings to native side.
         * Useful if you add AppWidgets that mirror these settings.
         */
        @JavascriptInterface
        public void syncSettings(boolean blink, boolean showSeconds, boolean is24h) {
            prefs.edit()
                .putBoolean("blink", blink)
                .putBoolean("showSeconds", showSeconds)
                .putBoolean("is24h", is24h)
                .apply();
            // TODO: notify AppWidgetProvider if you implement home-screen widgets
        }

        /**
         * Guide the user to pin a home-screen widget.
         * In a full implementation, use AppWidgetManager.requestPinAppWidget().
         */
        @JavascriptInterface
        public void pinWidget(String type) {
            // TODO: Implement AppWidgetManager.requestPinAppWidget() here
            // Example:
            // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //     AppWidgetManager mgr = AppWidgetManager.getInstance(MainActivity.this);
            //     ComponentName provider = new ComponentName(MainActivity.this, ClockWidgetProvider.class);
            //     if (mgr.isRequestPinAppWidgetSupported()) {
            //         mgr.requestPinAppWidget(provider, null, null);
            //     }
            // }
        }
    }
}
