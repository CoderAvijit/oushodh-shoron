package com.oushodh.shoron.util;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.oushodh.shoron.OushodhApp;
import com.oushodh.shoron.R;
import com.oushodh.shoron.receiver.AlarmActionReceiver;
import com.oushodh.shoron.ui.AlarmActivity;

public class NotificationHelper {

    public static final int NOTIF_ID_ALARM = 9001;

    public static Notification buildAlarmNotification(Context ctx, long reminderId, String medicineName) {
        Intent fullScreenIntent = new Intent(ctx, AlarmActivity.class);
        fullScreenIntent.putExtra(AlarmActivity.EXTRA_REMINDER_ID, reminderId);
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent fullScreenPi = PendingIntent.getActivity(
                ctx, (int) reminderId, fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent takenIntent = new Intent(ctx, AlarmActionReceiver.class);
        takenIntent.setAction(AlarmActionReceiver.ACTION_TAKEN);
        takenIntent.putExtra(AlarmActionReceiver.EXTRA_ID, reminderId);
        PendingIntent takenPi = PendingIntent.getBroadcast(
                ctx, (int) (reminderId * 10 + 2), takenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent snoozeIntent = new Intent(ctx, AlarmActionReceiver.class);
        snoozeIntent.setAction(AlarmActionReceiver.ACTION_SNOOZE);
        snoozeIntent.putExtra(AlarmActionReceiver.EXTRA_ID, reminderId);
        PendingIntent snoozePi = PendingIntent.getBroadcast(
                ctx, (int) (reminderId * 10 + 3), snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(ctx, OushodhApp.CHANNEL_ALARM)
                .setSmallIcon(R.drawable.ic_pill)
                .setContentTitle(ctx.getString(R.string.notif_medicine_time))
                .setContentText(medicineName != null ? medicineName : "")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false)
                .setFullScreenIntent(fullScreenPi, true)
                .setContentIntent(fullScreenPi)
                .addAction(R.drawable.ic_check, ctx.getString(R.string.btn_taken), takenPi)
                .addAction(R.drawable.ic_snooze, ctx.getString(R.string.btn_snooze), snoozePi)
                .build();
    }
}
