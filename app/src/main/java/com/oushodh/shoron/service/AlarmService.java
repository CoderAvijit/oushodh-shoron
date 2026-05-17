package com.oushodh.shoron.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import com.oushodh.shoron.data.AppDatabase;
import com.oushodh.shoron.data.Reminder;
import com.oushodh.shoron.util.NotificationHelper;

import java.io.File;
import java.util.concurrent.Executors;

public class AlarmService extends Service {

    private static final String TAG = "AlarmService";
    public static final String EXTRA_REMINDER_ID = "extra_reminder_id";
    public static final String ACTION_STOP = "com.oushodh.shoron.ACTION_STOP_ALARM";

    private MediaPlayer ringtonePlayer;
    private MediaPlayer voicePlayer;
    private Vibrator vibrator;
    private PowerManager.WakeLock wakeLock;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "OushodhShoron::AlarmWakeLock");
            wakeLock.setReferenceCounted(false);
            wakeLock.acquire(10 * 60 * 1000L);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            Log.d(TAG, "Received STOP");
            cleanupAndStop();
            return START_NOT_STICKY;
        }

        long id = intent != null ? intent.getLongExtra(EXTRA_REMINDER_ID, -1) : -1;
        if (id < 0) {
            cleanupAndStop();
            return START_NOT_STICKY;
        }

        startForeground(NotificationHelper.NOTIF_ID_ALARM,
                NotificationHelper.buildAlarmNotification(this, id, ""));

        Executors.newSingleThreadExecutor().execute(() -> {
            Reminder r = AppDatabase.getInstance(getApplicationContext())
                    .reminderDao().getById(id);
            if (r == null) {
                stopSelf();
                return;
            }
            startForeground(NotificationHelper.NOTIF_ID_ALARM,
                    NotificationHelper.buildAlarmNotification(this, id, r.getMedicineName()));
            startRingtone(r.getRingtoneUri());
            startVoice(r.getVoicePath());
            startVibration();
        });

        return START_STICKY;
    }

    private void cleanupAndStop() {
        releaseMedia();
        try { stopForeground(STOP_FOREGROUND_REMOVE); } catch (Exception ignored) {}
        stopSelf();
    }

    private void releaseMedia() {
        try {
            if (ringtonePlayer != null) {
                if (ringtonePlayer.isPlaying()) ringtonePlayer.stop();
                ringtonePlayer.release();
            }
        } catch (Exception ignored) {}
        ringtonePlayer = null;

        try {
            if (voicePlayer != null) {
                if (voicePlayer.isPlaying()) voicePlayer.stop();
                voicePlayer.release();
            }
        } catch (Exception ignored) {}
        voicePlayer = null;

        try { if (vibrator != null) vibrator.cancel(); } catch (Exception ignored) {}
        vibrator = null;

        try { if (wakeLock != null && wakeLock.isHeld()) wakeLock.release(); } catch (Exception ignored) {}
        wakeLock = null;
    }

    private void startRingtone(String uriStr) {
        try {
            Uri uri;
            if (uriStr != null && !uriStr.isEmpty()) {
                uri = Uri.parse(uriStr);
            } else {
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            ringtonePlayer = new MediaPlayer();
            ringtonePlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            ringtonePlayer.setDataSource(this, uri);
            ringtonePlayer.setLooping(true);
            ringtonePlayer.prepare();

            AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (am != null) {
                int max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                am.setStreamVolume(AudioManager.STREAM_ALARM, max, 0);
            }
            ringtonePlayer.start();
        } catch (Exception e) {
            Log.e(TAG, "Ringtone failed", e);
        }
    }

    private void startVoice(String path) {
        if (path == null || path.isEmpty()) return;
        File f = new File(path);
        if (!f.exists()) return;
        try {
            voicePlayer = new MediaPlayer();
            voicePlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build());
            voicePlayer.setDataSource(f.getAbsolutePath());
            voicePlayer.setLooping(true);
            voicePlayer.prepare();
            voicePlayer.start();
        } catch (Exception e) {
            Log.e(TAG, "Voice play failed", e);
        }
    }

    private void startVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vm = (VibratorManager) getSystemService(VIBRATOR_MANAGER_SERVICE);
            if (vm != null) vibrator = vm.getDefaultVibrator();
        } else {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        }
        if (vibrator == null) return;
        long[] pattern = {0, 800, 500, 800, 500};
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
    }

    @Override
    public void onDestroy() {
        releaseMedia();
        super.onDestroy();
    }

    public static void stop(android.content.Context ctx) {
        Intent i = new Intent(ctx, AlarmService.class);
        i.setAction(ACTION_STOP);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(i);
            } else {
                ctx.startService(i);
            }
        } catch (Exception e) {
            ctx.stopService(new Intent(ctx, AlarmService.class));
        }
    }
}
