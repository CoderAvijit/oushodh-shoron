package com.oushodh.shoron.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.oushodh.shoron.data.Reminder;
import com.oushodh.shoron.receiver.AlarmReceiver;

import java.util.Calendar;

public class AlarmScheduler {

    private static final String TAG = "AlarmScheduler";
    public static final String EXTRA_REMINDER_ID = "extra_reminder_id";
    public static final String EXTRA_SNOOZE = "extra_snooze";

    public static void schedule(Context ctx, Reminder r) {
        schedule(ctx, r, false);
    }

    public static void schedule(Context ctx, Reminder r, boolean afterTaken) {
        if (r == null || !r.isEnabled()) return;
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        long triggerAt = computeNextTrigger(r, afterTaken);
        PendingIntent pi = buildPi(ctx, r.getId());

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    am.setAlarmClock(new AlarmManager.AlarmClockInfo(triggerAt, pi), pi);
                } else {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
                }
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            }
            Log.d(TAG, "Scheduled id=" + r.getId() + " at " + triggerAt + " afterTaken=" + afterTaken);
        } catch (SecurityException e) {
            Log.e(TAG, "Exact alarm denied; using inexact", e);
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }

    public static void snooze(Context ctx, long reminderId, long delayMillis) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        long triggerAt = System.currentTimeMillis() + delayMillis;
        Intent i = new Intent(ctx, AlarmReceiver.class);
        i.setAction("com.oushodh.shoron.ALARM_FIRE_" + reminderId);
        i.putExtra(EXTRA_REMINDER_ID, reminderId);
        i.putExtra(EXTRA_SNOOZE, true);
        PendingIntent pi = PendingIntent.getBroadcast(
                ctx,
                (int) reminderId,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            }
        } catch (SecurityException e) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }

    public static void cancel(Context ctx, long id) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        am.cancel(buildPi(ctx, id));
    }

    private static PendingIntent buildPi(Context ctx, long id) {
        Intent i = new Intent(ctx, AlarmReceiver.class);
        i.setAction("com.oushodh.shoron.ALARM_FIRE_" + id);
        i.putExtra(EXTRA_REMINDER_ID, id);
        return PendingIntent.getBroadcast(
                ctx,
                (int) id,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static long computeNextTrigger(Reminder r, boolean afterTaken) {
        int interval = Math.max(1, r.getRepeatIntervalDays());
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, r.getHour());
        c.set(Calendar.MINUTE, r.getMinute());
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long now = System.currentTimeMillis();

        if (afterTaken) {
            c.add(Calendar.DAY_OF_YEAR, interval);
        } else if (c.getTimeInMillis() <= now) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        return c.getTimeInMillis();
    }
}
