package com.antigravity.clock;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;
import android.widget.RemoteViews;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ThemeManager {
    public static final String PREFS_NAME = "clock_prefs";
    public static final String KEY_THEME = "clk_theme";
    public static final String KEY_BLINK = "clk_blink";
    public static final String KEY_SECONDS = "clk_sec";
    public static final String KEY_24H = "clk_24h";

    public static class Theme {
        public final String name;
        public final int accent;
        public final int glow;
        public final int dimColon;
        public final int cardBg;
        public final int cardBorder;
        public final int textMuted;
        public final int bgResId;
        public final int cardBgResId;
        public final int pillBgResId;

        public Theme(String name, int accent, int glow, int cardBg, int cardBorder, int textMuted,
                     int bgResId, int cardBgResId, int pillBgResId) {
            this.name = name;
            this.accent = accent;
            this.glow = glow;
            this.dimColon = Color.argb(40, Color.red(accent), Color.green(accent), Color.blue(accent));
            this.cardBg = cardBg;
            this.cardBorder = cardBorder;
            this.textMuted = textMuted;
            this.bgResId = bgResId;
            this.cardBgResId = cardBgResId;
            this.pillBgResId = pillBgResId;
        }
    }

    private static final Map<String, Theme> THEMES = new HashMap<>();

    static {
        // 10 AMOLED themes matching index.html 1:1 with pure dark glass card backgrounds
        THEMES.put("cyan", new Theme("cyan", Color.parseColor("#00f0ff"), 0x5900f0ff,
                Color.parseColor("#0c0c0c"), Color.parseColor("#1a2630"), Color.parseColor("#888888"),
                R.drawable.widget_bg_cyan, R.drawable.widget_card_cyan, R.drawable.widget_meta_pill_cyan));

        THEMES.put("white", new Theme("white", Color.parseColor("#ffffff"), 0x40ffffff,
                Color.parseColor("#0c0c0c"), Color.parseColor("#282828"), Color.parseColor("#888888"),
                R.drawable.widget_bg_white, R.drawable.widget_card_white, R.drawable.widget_meta_pill_white));

        THEMES.put("emerald", new Theme("emerald", Color.parseColor("#00ff66"), 0x5900ff66,
                Color.parseColor("#0c0c0c"), Color.parseColor("#132a1a"), Color.parseColor("#888888"),
                R.drawable.widget_bg_emerald, R.drawable.widget_card_emerald, R.drawable.widget_meta_pill_emerald));

        THEMES.put("amber", new Theme("amber", Color.parseColor("#ffaa00"), 0x59ffaa00,
                Color.parseColor("#0c0c0c"), Color.parseColor("#2b2010"), Color.parseColor("#888888"),
                R.drawable.widget_bg_amber, R.drawable.widget_card_amber, R.drawable.widget_meta_pill_amber));

        THEMES.put("crimson", new Theme("crimson", Color.parseColor("#ff3b30"), 0x59ff3b30,
                Color.parseColor("#0c0c0c"), Color.parseColor("#2d1414"), Color.parseColor("#888888"),
                R.drawable.widget_bg_crimson, R.drawable.widget_card_crimson, R.drawable.widget_meta_pill_crimson));

        THEMES.put("violet", new Theme("violet", Color.parseColor("#bf5af2"), 0x59bf5af2,
                Color.parseColor("#0c0c0c"), Color.parseColor("#26152d"), Color.parseColor("#888888"),
                R.drawable.widget_bg_violet, R.drawable.widget_card_violet, R.drawable.widget_meta_pill_violet));

        THEMES.put("stealth", new Theme("stealth", Color.parseColor("#8e8e93"), 0x338e8e93,
                Color.parseColor("#0c0c0c"), Color.parseColor("#202022"), Color.parseColor("#888888"),
                R.drawable.widget_bg_stealth, R.drawable.widget_card_stealth, R.drawable.widget_meta_pill_stealth));

        THEMES.put("nightred", new Theme("nightred", Color.parseColor("#ff2d20"), 0x73ff2d20,
                Color.parseColor("#120000"), Color.parseColor("#40ff2d20"), Color.parseColor("#993333"),
                R.drawable.widget_bg_nightred, R.drawable.widget_card_bg_nightred, R.drawable.widget_meta_pill_nightred));

        THEMES.put("gold", new Theme("gold", Color.parseColor("#ffd700"), 0x59ffd700,
                Color.parseColor("#0c0c0c"), Color.parseColor("#2b2408"), Color.parseColor("#888888"),
                R.drawable.widget_bg_gold, R.drawable.widget_card_gold, R.drawable.widget_meta_pill_gold));

        THEMES.put("pink", new Theme("pink", Color.parseColor("#ff2d7a"), 0x59ff2d7a,
                Color.parseColor("#0c0c0c"), Color.parseColor("#2e111d"), Color.parseColor("#888888"),
                R.drawable.widget_bg_pink, R.drawable.widget_card_pink, R.drawable.widget_meta_pill_pink));
    }

    public static Theme getTheme(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String name = prefs.getString(KEY_THEME, "cyan");
            Theme t = THEMES.get(name);
            if (t != null) return t;
        } catch (Exception ignored) {}
        return THEMES.get("cyan");
    }

    public static void setTheme(Context context, String themeName) {
        if (themeName == null || !THEMES.containsKey(themeName)) {
            themeName = "cyan";
        }
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_THEME, themeName).commit();
        } catch (Exception ignored) {}
        notifyWidgetsChanged(context);
    }

    public static boolean isBlinkColon(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return prefs.getBoolean(KEY_BLINK, true);
        } catch (Exception ignored) {
            return true;
        }
    }

    public static void setBlinkColon(Context context, boolean blink) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_BLINK, blink).commit();
        } catch (Exception ignored) {}
        notifyWidgetsChanged(context);
    }

    public static boolean isShowSeconds(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return prefs.getBoolean(KEY_SECONDS, true);
        } catch (Exception ignored) {
            return true;
        }
    }

    public static void setShowSeconds(Context context, boolean show) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_SECONDS, show).commit();
        } catch (Exception ignored) {}
        notifyWidgetsChanged(context);
    }

    public static boolean is24HourFormat(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            if (prefs.contains(KEY_24H)) {
                return prefs.getBoolean(KEY_24H, true);
            }
        } catch (Exception ignored) {}
        return android.text.format.DateFormat.is24HourFormat(context);
    }

    public static void set24HourFormat(Context context, boolean is24h) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_24H, is24h).commit();
        } catch (Exception ignored) {}
        notifyWidgetsChanged(context);
    }

    public static void syncAllSettings(Context context, boolean blink, boolean showSeconds, boolean is24h) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit()
                .putBoolean(KEY_BLINK, blink)
                .putBoolean(KEY_SECONDS, showSeconds)
                .putBoolean(KEY_24H, is24h)
                .commit();
        } catch (Exception ignored) {}
        notifyWidgetsChanged(context);
    }

    // High-resolution lightweight double-buffered progress bar rendering (600x8)
    private static final int BAR_W = 600;
    private static final int BAR_H = 8;
    private static Bitmap sBarBmpA = null;
    private static Bitmap sBarBmpB = null;
    private static Canvas sBarCanvasA = null;
    private static Canvas sBarCanvasB = null;
    private static boolean sToggleBuffer = false;
    private static final Paint sTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint sFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint sTipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final RectF sTrackRect = new RectF(0, 0, BAR_W, BAR_H);
    private static final RectF sFillRect = new RectF();
    private static final android.graphics.Path sTrackClipPath = new android.graphics.Path();
    private static boolean sClipPathInitialized = false;

    // AM/PM badge caching per theme
    private static String sCachedAmpmTheme = null;
    private static Bitmap sCachedAmpmAM = null;
    private static Bitmap sCachedAmpmPM = null;

    /**
     * Renders a silky-smooth anti-aliased high-resolution progress bar bitmap.
     * Alternates between two pre-allocated Bitmaps (double buffering) so that
     * Android's RemoteViews and Launcher HWUI RenderThread always detect an updated
     * bitmap reference and immediately redraw without dropping frames or leaking memory.
     * Sleek glowing neon design matching the app UI 1:1.
     */
    public static synchronized Bitmap renderProgressBar(float fraction, Theme theme) {
        if (sBarBmpA == null) {
            sBarBmpA = Bitmap.createBitmap(BAR_W, BAR_H, Bitmap.Config.ARGB_8888);
            sBarCanvasA = new Canvas(sBarBmpA);
            sBarBmpB = Bitmap.createBitmap(BAR_W, BAR_H, Bitmap.Config.ARGB_8888);
            sBarCanvasB = new Canvas(sBarBmpB);
        }

        float radius = BAR_H / 2f;
        if (!sClipPathInitialized) {
            sTrackClipPath.reset();
            sTrackClipPath.addRoundRect(sTrackRect, radius, radius, android.graphics.Path.Direction.CW);
            sClipPathInitialized = true;
        }

        // Toggle between buffer A and buffer B on each frame
        sToggleBuffer = !sToggleBuffer;
        Bitmap targetBmp = sToggleBuffer ? sBarBmpA : sBarBmpB;
        Canvas canvas = sToggleBuffer ? sBarCanvasA : sBarCanvasB;

        targetBmp.eraseColor(Color.TRANSPARENT);

        // Background track matching index.html rgba(255, 255, 255, 0.06)
        sTrackPaint.setStyle(Paint.Style.FILL);
        int trackColor = "nightred".equals(theme.name) ? Color.argb(45, 255, 45, 32) : Color.argb(20, 255, 255, 255);
        sTrackPaint.setColor(trackColor);
        canvas.drawRoundRect(sTrackRect, radius, radius, sTrackPaint);

        // Progress fill with theme accent
        float clampedFraction = Math.max(0f, Math.min(1.0f, fraction));
        if (clampedFraction > 0.0005f) {
            float fillW = BAR_W * clampedFraction;
            sFillPaint.setStyle(Paint.Style.FILL);
            sFillPaint.setColor(theme.accent);

            canvas.save();
            canvas.clipPath(sTrackClipPath);
            sFillRect.set(0, 0, Math.max(radius * 2, fillW), BAR_H);
            canvas.drawRoundRect(sFillRect, radius, radius, sFillPaint);

            // Radiant neon leading tip glow matching app box-shadow: 0 0 8px var(--accent)
            if (fillW > radius * 2) {
                float glowW = Math.min(fillW, 36f);
                android.graphics.LinearGradient tipGrad = new android.graphics.LinearGradient(
                        fillW - glowW, 0, fillW, 0,
                        Color.TRANSPARENT,
                        Color.argb(170, 255, 255, 255),
                        android.graphics.Shader.TileMode.CLAMP);
                sTipPaint.setShader(tipGrad);
                canvas.drawRect(fillW - glowW, 0, fillW, BAR_H, sTipPaint);
            }

            canvas.restore();
        }

        return targetBmp;
    }

    /**
     * Renders a pixel-perfect capsule AM/PM badge bitmap matching the app.
     * Caches per theme to eliminate per-frame allocations.
     */
    public static synchronized Bitmap renderAmpmBadge(String text, Theme theme) {
        boolean isPM = "PM".equalsIgnoreCase(text);
        if (theme.name.equals(sCachedAmpmTheme)) {
            Bitmap cached = isPM ? sCachedAmpmPM : sCachedAmpmAM;
            if (cached != null) return cached;
        }

        int w = 76;
        int h = 36;
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        float cornerRadius = 8f;
        RectF rect = new RectF(1.0f, 1.0f, w - 1.0f, h - 1.0f);

        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);

        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1.5f);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(20f);
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);

        if (isPM) {
            // PM: Highlighted in theme accent color
            int bgColor = Color.argb(25, Color.red(theme.accent), Color.green(theme.accent), Color.blue(theme.accent));
            bgPaint.setColor(bgColor);
            borderPaint.setColor(theme.accent);
            textPaint.setColor(theme.accent);
        } else {
            // AM: Subtle dark translucent badge with muted text
            bgPaint.setColor(Color.argb(20, 255, 255, 255));
            borderPaint.setColor(Color.argb(18, 255, 255, 255));
            textPaint.setColor(theme.textMuted);
        }

        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint);

        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        float y = (h / 2f) + (bounds.height() / 2f) - 1f;
        canvas.drawText(text, w / 2f, y, textPaint);

        if (!theme.name.equals(sCachedAmpmTheme)) {
            sCachedAmpmTheme = theme.name;
            sCachedAmpmAM = null;
            sCachedAmpmPM = null;
        }
        if (isPM) {
            sCachedAmpmPM = bmp;
        } else {
            sCachedAmpmAM = bmp;
        }

        return bmp;
    }

    /**
     * Builds a complete, self-contained RemoteViews for the Digital Clock widget.
     * Guarantees fixed action count (~16 actions) preventing the RemoteViews IPC
     * action accumulation bug in system_server.
     */
    public static RemoteViews createDigitalWidgetViews(Context context, Theme theme,
                                                         boolean is24h, boolean showSeconds, boolean isColonDim,
                                                         int ampm, float progressFraction, Calendar now) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_digital);

        // Launch app on click
        Intent launchIntent = new Intent(context, MainActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, flags);
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        // Explicit time values
        int hourVal = now.get(Calendar.HOUR_OF_DAY);
        if (!is24h) {
            hourVal = now.get(Calendar.HOUR);
            if (hourVal == 0) hourVal = 12;
        }
        int minuteVal = now.get(Calendar.MINUTE);
        int secondVal = now.get(Calendar.SECOND);
        String hourStr = String.format(java.util.Locale.US, "%02d", hourVal);
        String minStr = String.format(java.util.Locale.US, "%02d", minuteVal);
        String secStr = String.format(java.util.Locale.US, "%02d", secondVal);

        // Set time digits directly (guarantees instantaneous accuracy across all launchers)
        views.setTextViewText(R.id.widget_hours, hourStr);
        views.setTextViewText(R.id.widget_minutes, minStr);
        views.setTextViewText(R.id.widget_seconds, secStr);

        // Themed colon color with breathing dim state
        views.setTextColor(R.id.widget_colon, isColonDim ? theme.dimColon : theme.accent);

        // Themed seconds digit color
        views.setTextColor(R.id.widget_seconds, theme.accent);

        // Date string
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("EEEE, MMM d, yyyy", java.util.Locale.US);
        String dateStr = dateFormat.format(now.getTime()).toUpperCase(java.util.Locale.US);
        views.setTextViewText(R.id.widget_date, dateStr);

        // Meta tags
        int weekNum = now.get(Calendar.WEEK_OF_YEAR);
        int dayOfYear = now.get(Calendar.DAY_OF_YEAR);
        views.setTextViewText(R.id.widget_meta_week, "WEEK " + weekNum);
        views.setTextViewText(R.id.widget_meta_day, "DAY " + dayOfYear);

        String tzId = java.util.TimeZone.getDefault().getID();
        String tzName = "LOCAL";
        if (tzId != null && tzId.contains("/")) {
            tzName = tzId.substring(tzId.lastIndexOf('/') + 1).replace('_', ' ').toUpperCase();
        } else if (tzId != null) {
            tzName = tzId.toUpperCase();
        }
        views.setTextViewText(R.id.widget_meta_tz, tzName);

        views.setTextColor(R.id.widget_meta_week, theme.textMuted);
        views.setTextColor(R.id.widget_meta_day, theme.textMuted);
        views.setTextColor(R.id.widget_meta_tz, theme.textMuted);

        views.setInt(R.id.widget_meta_week, "setBackgroundResource", theme.pillBgResId);
        views.setInt(R.id.widget_meta_day, "setBackgroundResource", theme.pillBgResId);
        views.setInt(R.id.widget_meta_tz, "setBackgroundResource", theme.pillBgResId);

        // Container background
        views.setInt(R.id.widget_container, "setBackgroundResource", theme.bgResId);

        // AM/PM badge
        if (is24h) {
            views.setViewVisibility(R.id.widget_ampm, View.GONE);
        } else {
            views.setViewVisibility(R.id.widget_ampm, View.VISIBLE);
            Bitmap ampmBmp = renderAmpmBadge(ampm == Calendar.PM ? "PM" : "AM", theme);
            views.setImageViewBitmap(R.id.widget_ampm, ampmBmp);
        }

        // Show/hide seconds and progress bar based on setting
        views.setViewVisibility(R.id.widget_seconds, showSeconds ? View.VISIBLE : View.GONE);
        views.setViewVisibility(R.id.widget_live_bar, showSeconds ? View.VISIBLE : View.GONE);

        // Render live smooth progress bar
        if (showSeconds) {
            Bitmap barBmp = renderProgressBar(progressFraction, theme);
            views.setImageViewBitmap(R.id.widget_live_bar, barBmp);
        }

        return views;
    }

    /**
     * Builds a complete, self-contained RemoteViews for the Clock & Calendar widget.
     */
    public static RemoteViews createCalendarWidgetViews(Context context, Theme theme,
                                                          boolean is24h, boolean showSeconds, boolean isColonDim,
                                                          int ampm, float progressFraction, Calendar now) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_calendar);

        Intent launchIntent = new Intent(context, MainActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, flags);
        views.setOnClickPendingIntent(R.id.widget_calendar_root, pendingIntent);
        views.setOnClickPendingIntent(R.id.widget_cal_card, pendingIntent);

        // Explicit time values
        int hourVal = now.get(Calendar.HOUR_OF_DAY);
        if (!is24h) {
            hourVal = now.get(Calendar.HOUR);
            if (hourVal == 0) hourVal = 12;
        }
        int minuteVal = now.get(Calendar.MINUTE);
        int secondVal = now.get(Calendar.SECOND);
        String hourStr = String.format(java.util.Locale.US, "%02d", hourVal);
        String minStr = String.format(java.util.Locale.US, "%02d", minuteVal);
        String secStr = String.format(java.util.Locale.US, "%02d", secondVal);

        views.setTextViewText(R.id.widget_cal_hours, hourStr);
        views.setTextViewText(R.id.widget_cal_minutes, minStr);
        views.setTextViewText(R.id.widget_cal_seconds, secStr);

        views.setTextColor(R.id.widget_cal_colon, isColonDim ? theme.dimColon : theme.accent);
        views.setTextColor(R.id.widget_cal_seconds, theme.accent);

        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("EEEE, MMM d, yyyy", java.util.Locale.US);
        views.setTextViewText(R.id.widget_cal_date, dateFormat.format(now.getTime()).toUpperCase(java.util.Locale.US));

        // Calendar card metadata
        java.text.SimpleDateFormat monthFormat = new java.text.SimpleDateFormat("MMM", java.util.Locale.US);
        java.text.SimpleDateFormat weekdayFormat = new java.text.SimpleDateFormat("EEEE", java.util.Locale.US);

        String monthStr = monthFormat.format(now.getTime()).toUpperCase(java.util.Locale.US);
        String dayNumStr = String.valueOf(now.get(Calendar.DAY_OF_MONTH));
        String weekdayStr = weekdayFormat.format(now.getTime()).toUpperCase(java.util.Locale.US);
        int weekNum = now.get(Calendar.WEEK_OF_YEAR);
        int dayOfYear = now.get(Calendar.DAY_OF_YEAR);

        views.setTextViewText(R.id.widget_cal_month, monthStr);
        views.setTextViewText(R.id.widget_cal_day_num, dayNumStr);
        views.setTextViewText(R.id.widget_cal_weekday, weekdayStr);
        views.setTextViewText(R.id.widget_cal_week_num, "WEEK " + weekNum + " • DAY " + dayOfYear);

        views.setInt(R.id.widget_calendar_root, "setBackgroundResource", theme.bgResId);
        views.setInt(R.id.widget_cal_card, "setBackgroundResource", theme.cardBgResId);

        views.setTextColor(R.id.widget_cal_month, theme.accent);
        views.setTextColor(R.id.widget_cal_weekday, theme.textMuted);
        views.setTextColor(R.id.widget_cal_week_num, theme.accent);

        if (is24h) {
            views.setViewVisibility(R.id.widget_cal_ampm, View.GONE);
        } else {
            views.setViewVisibility(R.id.widget_cal_ampm, View.VISIBLE);
            Bitmap ampmBmp = renderAmpmBadge(ampm == Calendar.PM ? "PM" : "AM", theme);
            views.setImageViewBitmap(R.id.widget_cal_ampm, ampmBmp);
        }

        views.setViewVisibility(R.id.widget_cal_seconds, showSeconds ? View.VISIBLE : View.GONE);
        views.setViewVisibility(R.id.widget_live_bar, showSeconds ? View.VISIBLE : View.GONE);

        if (showSeconds) {
            Bitmap barBmp = renderProgressBar(progressFraction, theme);
            views.setImageViewBitmap(R.id.widget_live_bar, barBmp);
        }

        return views;
    }

    /**
     * Applies full theme and style consistency to the Digital Clock Widget (legacy bridge).
     */
    public static void applyThemeToDigitalWidget(Context context, RemoteViews views, Theme theme,
                                                 boolean is24h, boolean showSeconds, boolean blinkColon,
                                                 int ampm, float progressFraction) {
        views.setInt(R.id.widget_container, "setBackgroundResource", theme.bgResId);
        views.setTextColor(R.id.widget_colon, theme.accent);
        views.setTextColor(R.id.widget_seconds, theme.accent);
        views.setTextColor(R.id.widget_meta_week, theme.accent);
        views.setTextColor(R.id.widget_meta_day, theme.accent);
        views.setTextColor(R.id.widget_meta_tz, theme.accent);
        views.setInt(R.id.widget_meta_week, "setBackgroundResource", theme.pillBgResId);
        views.setInt(R.id.widget_meta_day, "setBackgroundResource", theme.pillBgResId);
        views.setInt(R.id.widget_meta_tz, "setBackgroundResource", theme.pillBgResId);

        if (is24h) {
            views.setViewVisibility(R.id.widget_ampm, View.GONE);
        } else {
            views.setViewVisibility(R.id.widget_ampm, View.VISIBLE);
            Bitmap ampmBmp = renderAmpmBadge(ampm == Calendar.PM ? "PM" : "AM", theme);
            views.setImageViewBitmap(R.id.widget_ampm, ampmBmp);
        }

        views.setViewVisibility(R.id.widget_seconds, showSeconds ? View.VISIBLE : View.GONE);
        views.setViewVisibility(R.id.widget_live_bar, showSeconds ? View.VISIBLE : View.GONE);

        if (showSeconds) {
            Bitmap barBmp = renderProgressBar(progressFraction, theme);
            views.setImageViewBitmap(R.id.widget_live_bar, barBmp);
        }
    }

    /**
     * Applies full theme and style consistency to the Clock & Calendar Widget (legacy bridge).
     */
    public static void applyThemeToCalendarWidget(Context context, RemoteViews views, Theme theme,
                                                  boolean is24h, boolean showSeconds, boolean blinkColon,
                                                  int ampm, float progressFraction) {
        views.setInt(R.id.widget_calendar_root, "setBackgroundResource", theme.bgResId);
        views.setInt(R.id.widget_cal_card, "setBackgroundResource", theme.cardBgResId);
        views.setTextColor(R.id.widget_cal_colon, theme.accent);
        views.setTextColor(R.id.widget_cal_seconds, theme.accent);
        views.setTextColor(R.id.widget_cal_month, theme.accent);
        views.setTextColor(R.id.widget_cal_weekday, theme.textMuted);
        views.setTextColor(R.id.widget_cal_week_num, theme.accent);

        if (is24h) {
            views.setViewVisibility(R.id.widget_cal_ampm, View.GONE);
        } else {
            views.setViewVisibility(R.id.widget_cal_ampm, View.VISIBLE);
            Bitmap ampmBmp = renderAmpmBadge(ampm == Calendar.PM ? "PM" : "AM", theme);
            views.setImageViewBitmap(R.id.widget_cal_ampm, ampmBmp);
        }

        views.setViewVisibility(R.id.widget_cal_seconds, showSeconds ? View.VISIBLE : View.GONE);
        views.setViewVisibility(R.id.widget_live_bar, showSeconds ? View.VISIBLE : View.GONE);

        if (showSeconds) {
            Bitmap barBmp = renderProgressBar(progressFraction, theme);
            views.setImageViewBitmap(R.id.widget_live_bar, barBmp);
        }
    }

    /**
     * Direct synchronous update to all active widgets and restarts the live ticker service.
     */
    public static void notifyWidgetsChanged(Context context) {
        try {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);

            ComponentName digitalWidget = new ComponentName(context, ClockWidgetProvider.class);
            int[] digitalIds = manager.getAppWidgetIds(digitalWidget);
            if (digitalIds != null && digitalIds.length > 0) {
                for (int id : digitalIds) {
                    ClockWidgetProvider.updateAppWidget(context, manager, id);
                }
            }

            ComponentName calWidget = new ComponentName(context, ClockCalendarWidgetProvider.class);
            int[] calIds = manager.getAppWidgetIds(calWidget);
            if (calIds != null && calIds.length > 0) {
                for (int id : calIds) {
                    ClockCalendarWidgetProvider.updateAppWidget(context, manager, id);
                }
            }

            WidgetLiveUpdateService.start(context);
            WidgetLiveUpdateService.requestImmediateUpdate(context);
        } catch (Exception ignored) {}
    }
}
