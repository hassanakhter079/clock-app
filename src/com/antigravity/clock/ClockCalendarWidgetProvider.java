package com.antigravity.clock;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ClockCalendarWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        WidgetLiveUpdateService.start(context);
        WidgetLiveUpdateService.requestImmediateUpdate(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        WidgetLiveUpdateService.start(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        checkAndStopService(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent != null ? intent.getAction() : null;
        if (Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_DATE_CHANGED.equals(action)
                || Intent.ACTION_TIME_TICK.equals(action)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, ClockCalendarWidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            if (appWidgetIds != null && appWidgetIds.length > 0) {
                onUpdate(context, appWidgetManager, appWidgetIds);
            }
        }
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Calendar now = Calendar.getInstance();
        int second = now.get(Calendar.SECOND);
        int millis = now.get(Calendar.MILLISECOND);
        int ampm = now.get(Calendar.AM_PM);

        ThemeManager.Theme theme = ThemeManager.getTheme(context);
        boolean is24h = ThemeManager.is24HourFormat(context);
        boolean showSeconds = ThemeManager.isShowSeconds(context);
        boolean blinkColon = ThemeManager.isBlinkColon(context);
        float progressFraction = (second + (millis / 1000f)) / 60f;
        boolean isColonDim = blinkColon && (millis >= 500);

        RemoteViews views = ThemeManager.createCalendarWidgetViews(
                context, theme, is24h, showSeconds, isColonDim, ampm, progressFraction, now);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private void checkAndStopService(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName digitalWidget = new ComponentName(context, ClockWidgetProvider.class);
        int[] digitalIds = manager.getAppWidgetIds(digitalWidget);
        ComponentName calWidget = new ComponentName(context, ClockCalendarWidgetProvider.class);
        int[] calIds = manager.getAppWidgetIds(calWidget);
        if ((digitalIds == null || digitalIds.length == 0) && (calIds == null || calIds.length == 0)) {
            WidgetLiveUpdateService.stop(context);
        }
    }
}
