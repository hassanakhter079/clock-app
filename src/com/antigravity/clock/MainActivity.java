package com.antigravity.clock;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;

    public class WebAppInterface {
        private Vibrator vibrator;
        private Activity activity;

        WebAppInterface(Activity activity) {
            this.activity = activity;
            this.vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        }

        @JavascriptInterface
        public boolean isNativeApp() {
            return true;
        }

        @JavascriptInterface
        public void vibrate(long ms) {
            try {
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(ms);
                    }
                }
            } catch (Exception ignored) {}
        }

        @JavascriptInterface
        public void setKeepScreenOn(final boolean keepOn) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (keepOn) {
                        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    } else {
                        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                }
            });
        }

        @JavascriptInterface
        public void setScreenBrightness(final float brightness) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        WindowManager.LayoutParams layout = activity.getWindow().getAttributes();
                        // brightness between 0.01f and 1.0f, or -1 for system default
                        layout.screenBrightness = Math.max(0.01f, Math.min(1.0f, brightness));
                        activity.getWindow().setAttributes(layout);
                    } catch (Exception ignored) {}
                }
            });
        }

        @JavascriptInterface
        public String getTheme() {
            try {
                return ThemeManager.getTheme(activity).name;
            } catch (Exception e) {
                return "cyan";
            }
        }

        @JavascriptInterface
        public void setTheme(final String themeName) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ThemeManager.setTheme(activity, themeName);
                }
            });
        }

        @JavascriptInterface
        public void syncSettings(final boolean blink, final boolean showSeconds, final boolean is24h) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ThemeManager.syncAllSettings(activity, blink, showSeconds, is24h);
                }
            });
        }

        @JavascriptInterface
        public boolean isPinWidgetSupported() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    AppWidgetManager appWidgetManager = activity.getSystemService(AppWidgetManager.class);
                    return appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported();
                } catch (Exception ignored) {}
            }
            return false;
        }

        @JavascriptInterface
        public void pinWidget(final String widgetType) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            AppWidgetManager appWidgetManager = activity.getSystemService(AppWidgetManager.class);
                            if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported()) {
                                Class<?> providerClass = "calendar".equalsIgnoreCase(widgetType)
                                        ? ClockCalendarWidgetProvider.class
                                        : ClockWidgetProvider.class;
                                ComponentName myProvider = new ComponentName(activity, providerClass);
                                appWidgetManager.requestPinAppWidget(myProvider, null, null);
                            }
                        } catch (Exception ignored) {}
                    }
                });
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen on by default for desk clock monitoring
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        hideSystemUI();

        webView = new WebView(this);
        webView.setBackgroundColor(Color.parseColor("#000000"));

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);

        webView.addJavascriptInterface(new WebAppInterface(this), "AndroidBridge");

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl("file:///android_asset/index.html");

        setContentView(webView);

        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission("android.permission.POST_NOTIFICATIONS") != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 101);
            }
        }

        handleIntentExtras(getIntent());
        updateWidgets();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntentExtras(intent);
        updateWidgets();
    }

    private void handleIntentExtras(Intent intent) {
        if (intent != null) {
            if (intent.hasExtra("theme")) {
                final String t = intent.getStringExtra("theme");
                ThemeManager.setTheme(this, t);
                if (webView != null) {
                    webView.post(new Runnable() {
                        @Override
                        public void run() {
                            webView.evaluateJavascript("if (typeof applyTheme === 'function') applyTheme('" + t + "');", null);
                        }
                    });
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        updateWidgets();
    }

    private void updateWidgets() {
        ThemeManager.notifyWidgetsChanged(this);
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
