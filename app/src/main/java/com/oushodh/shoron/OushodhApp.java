package com.oushodh.shoron;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.oushodh.shoron.util.NotificationHelper;

public class OushodhApp extends Application {

    public static final String CHANNEL_ALARM = "channel_alarm_high";
    public static final String CHANNEL_SERVICE = "channel_service";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm == null) return;

        NotificationChannel alarmChannel = new NotificationChannel(
                CHANNEL_ALARM,
                "ঔষধের অ্যালার্ম",
                NotificationManager.IMPORTANCE_HIGH);
        alarmChannel.setDescription("ঔষধ খাওয়ার অ্যালার্ম");
        alarmChannel.enableVibration(true);
        alarmChannel.enableLights(true);
        alarmChannel.setBypassDnd(true);
        alarmChannel.setLockscreenVisibility(NotificationManager.IMPORTANCE_HIGH);
        alarmChannel.setSound(null, null);
        nm.createNotificationChannel(alarmChannel);

        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_SERVICE,
                "চলমান অ্যালার্ম পরিষেবা",
                NotificationManager.IMPORTANCE_LOW);
        serviceChannel.setDescription("ব্যাকগ্রাউন্ড অ্যালার্ম সেবা");
        nm.createNotificationChannel(serviceChannel);
    }
}
