package com.antigravity.clock;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent != null ? intent.getAction() : null;
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || "android.intent.action.MY_PACKAGE_REPLACED".equals(action)) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] dIds = manager.getAppWidgetIds(new ComponentName(context, ClockWidgetProvider.class));
            int[] cIds = manager.getAppWidgetIds(new ComponentName(context, ClockCalendarWidgetProvider.class));
            if ((dIds != null && dIds.length > 0) || (cIds != null && cIds.length > 0)) {
                WidgetLiveUpdateService.start(context);
            }
        }
    }
}
