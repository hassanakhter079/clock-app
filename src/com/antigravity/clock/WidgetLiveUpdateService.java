package com.antigravity.clock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.RemoteViews;
import java.util.Calendar;
import java.util.Locale;

public class WidgetLiveUpdateService extends Service {
    public static final String ACTION_START_LIVE = "com.antigravity.clock.START_LIVE";
    public static final String ACTION_STOP_LIVE = "com.antigravity.clock.STOP_LIVE";
    public static final String ACTION_FORCE_UPDATE = "com.antigravity.clock.FORCE_UPDATE";
    private static final String CHANNEL_ID = "clock_widget_live_channel";
    private static final int NOTIF_ID = 1001;

    // Wall-clock synchronized 100ms (10 FPS) tick for silky, jitter-free progress bar animation
    private static final int TICK_INTERVAL_MS = 100;

    private Handler handler;
    private boolean isScreenOn = true;
    private BroadcastReceiver screenReceiver;
    private boolean isRunning = false;
    private int consecutiveEmptyChecks = 0;

    private int mLastMinute = -1;
    private int mLastSecond = -1;
    private boolean mLastColonDim = false;

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning && isScreenOn) {
                updateLiveWidgets();
                long now = System.currentTimeMillis();
                long delay = TICK_INTERVAL_MS - (now % TICK_INTERVAL_MS);
                if (delay < 15) delay += TICK_INTERVAL_MS;
                handler.postDelayed(this, delay);
            }
        }
    };

    public static void start(Context context) {
        try {
            Intent intent = new Intent(context, WidgetLiveUpdateService.class);
            intent.setAction(ACTION_START_LIVE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } catch (Exception e) {
            try {
                Intent intent = new Intent(context, WidgetLiveUpdateService.class);
                context.startService(intent);
            } catch (Exception ignored) {}
        }
    }

    public static void requestImmediateUpdate(Context context) {
        try {
            Intent intent = new Intent(context, WidgetLiveUpdateService.class);
            intent.setAction(ACTION_FORCE_UPDATE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } catch (Exception e) {
            try {
                Intent intent = new Intent(context, WidgetLiveUpdateService.class);
                context.startService(intent);
            } catch (Exception ignored) {}
        }
    }

    public static void stop(Context context) {
        try {
            Intent intent = new Intent(context, WidgetLiveUpdateService.class);
            intent.setAction(ACTION_STOP_LIVE);
            context.stopService(intent);
        } catch (Exception ignored) {}
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        startAsForeground();
        registerScreenReceiver();
    }

    private void startAsForeground() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "AMOLED Clock Live Widget",
                        NotificationManager.IMPORTANCE_MIN
                );
                channel.setDescription("Keeps live clock animations and progress bar running smoothly");
                channel.setShowBadge(false);
                channel.enableLights(false);
                channel.enableVibration(false);
                channel.setSound(null, null);
                NotificationManager manager = getSystemService(NotificationManager.class);
                if (manager != null) {
                    manager.createNotificationChannel(channel);
                }

                Intent launchIntent = new Intent(this, MainActivity.class);
                int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    flags |= PendingIntent.FLAG_IMMUTABLE;
                }
                PendingIntent pi = PendingIntent.getActivity(this, 0, launchIntent, flags);

                Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
                        .setContentTitle("AMOLED Clock")
                        .setContentText("Clock animation and live progress active")
                        .setSmallIcon(R.drawable.ic_stat_clock)
                        .setContentIntent(pi)
                        .setOngoing(true);

                startForeground(NOTIF_ID, builder.build());
            }
        } catch (Exception ignored) {}
    }

    private void registerScreenReceiver() {
        screenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent != null ? intent.getAction() : null;
                if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    isScreenOn = false;
                    handler.removeCallbacks(tickRunnable);
                } else if (Intent.ACTION_SCREEN_ON.equals(action) || Intent.ACTION_USER_PRESENT.equals(action)) {
                    isScreenOn = true;
                    mLastMinute = -1; // Force immediate fresh update upon screen on
                    mLastSecond = -1;
                    if (isRunning) {
                        handler.removeCallbacks(tickRunnable);
                        handler.post(tickRunnable);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(screenReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startAsForeground();

        if (intent != null && ACTION_STOP_LIVE.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        boolean forceUpdate = (intent != null && ACTION_FORCE_UPDATE.equals(intent.getAction()));
        if (forceUpdate) {
            mLastMinute = -1;
            mLastSecond = -1;
        }

        if (!isRunning || forceUpdate) {
            isRunning = true;
            handler.removeCallbacks(tickRunnable);
            handler.post(tickRunnable);
        }
        return START_STICKY;
    }

    private void updateLiveWidgets() {
        AppWidgetManager manager = AppWidgetManager.getInstance(this);

        ComponentName digitalWidget = new ComponentName(this, ClockWidgetProvider.class);
        int[] digitalIds = manager.getAppWidgetIds(digitalWidget);

        ComponentName calWidget = new ComponentName(this, ClockCalendarWidgetProvider.class);
        int[] calIds = manager.getAppWidgetIds(calWidget);

        boolean hasDigital = (digitalIds != null && digitalIds.length > 0);
        boolean hasCal = (calIds != null && calIds.length > 0);

        if (!hasDigital && !hasCal) {
            consecutiveEmptyChecks++;
            // 20 ticks (2 seconds) grace period to allow launcher widget placement to bind IDs
            if (consecutiveEmptyChecks > 20) {
                stopSelf();
            }
            return;
        }
        consecutiveEmptyChecks = 0;

        Calendar now = Calendar.getInstance();
        int second = now.get(Calendar.SECOND);
        int millis = now.get(Calendar.MILLISECOND);
        int minute = now.get(Calendar.MINUTE);
        int ampm = now.get(Calendar.AM_PM);

        // Check user preferences
        boolean showSeconds = ThemeManager.isShowSeconds(this);
        boolean blinkColon = ThemeManager.isBlinkColon(this);
        boolean is24h = ThemeManager.is24HourFormat(this);
        ThemeManager.Theme theme = ThemeManager.getTheme(this);

        float progressFraction = (second + (millis / 1000f)) / 60f;
        boolean isColonDim = blinkColon && (millis >= 500);

        boolean minuteChanged = (minute != mLastMinute);
        boolean secondChanged = (second != mLastSecond);
        boolean colonChanged = (isColonDim != mLastColonDim);

        if (minuteChanged) {
            // Full RemoteViews update ensures hour/minute/date digits and calendar cards are 100% in sync
            if (hasDigital) {
                RemoteViews views = ThemeManager.createDigitalWidgetViews(
                        this, theme, is24h, showSeconds, isColonDim, ampm, progressFraction, now);
                manager.updateAppWidget(digitalIds, views);
            }
            if (hasCal) {
                RemoteViews calViews = ThemeManager.createCalendarWidgetViews(
                        this, theme, is24h, showSeconds, isColonDim, ampm, progressFraction, now);
                manager.updateAppWidget(calIds, calViews);
            }
            mLastMinute = minute;
            mLastSecond = second;
            mLastColonDim = isColonDim;
        } else {
            // Blazing-fast sub-second partial updates: only updates changed views in place.
            // Eliminates IPC overhead, avoids re-layout thrashing, and guarantees silky 10 FPS animations.
            if (hasDigital) {
                RemoteViews partial = new RemoteViews(getPackageName(), R.layout.widget_clock_digital);
                if (showSeconds) {
                    Bitmap barBmp = ThemeManager.renderProgressBar(progressFraction, theme);
                    partial.setImageViewBitmap(R.id.widget_live_bar, barBmp);
                    if (secondChanged) {
                        partial.setTextViewText(R.id.widget_seconds, String.format(Locale.US, "%02d", second));
                    }
                }
                if (colonChanged) {
                    partial.setTextColor(R.id.widget_colon, isColonDim ? theme.dimColon : theme.accent);
                }
                manager.partiallyUpdateAppWidget(digitalIds, partial);
            }

            if (hasCal) {
                RemoteViews calPartial = new RemoteViews(getPackageName(), R.layout.widget_clock_calendar);
                if (showSeconds) {
                    Bitmap barBmp = ThemeManager.renderProgressBar(progressFraction, theme);
                    calPartial.setImageViewBitmap(R.id.widget_live_bar, barBmp);
                    if (secondChanged) {
                        calPartial.setTextViewText(R.id.widget_cal_seconds, String.format(Locale.US, "%02d", second));
                    }
                }
                if (colonChanged) {
                    calPartial.setTextColor(R.id.widget_cal_colon, isColonDim ? theme.dimColon : theme.accent);
                }
                manager.partiallyUpdateAppWidget(calIds, calPartial);
            }

            mLastSecond = second;
            mLastColonDim = isColonDim;
        }
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        if (handler != null) {
            handler.removeCallbacks(tickRunnable);
        }
        if (screenReceiver != null) {
            try {
                unregisterReceiver(screenReceiver);
            } catch (Exception ignored) {}
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
