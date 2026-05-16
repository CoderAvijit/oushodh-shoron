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

    private MediaPlayer ringtonePlayer;
    private MediaPlayer voicePlayer;
    private Vibrator vibrator;
    private PowerManager.WakeLock wakeLock;
    private long currentReminderId = -1;

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
        long id = intent != null ? intent.getLongExtra(EXTRA_REMINDER_ID, -1) : -1;
        if (id < 0) {
            stopSelf();
            return START_NOT_STICKY;
        }
        currentReminderId = id;

        // promote to foreground immediately
        startForeground(NotificationHelper.NOTIF_ID_ALARM,
                NotificationHelper.buildAlarmNotification(this, id, ""));

        Executors.newSingleThreadExecutor().execute(() -> {
            Reminder r = AppDatabase.getInstance(getApplicationContext())
                    .reminderDao().getById(id);
            if (r == null) {
                stopSelf();
                return;
            }
            // update notification with name
            startForeground(NotificationHelper.NOTIF_ID_ALARM,
                    NotificationHelper.buildAlarmNotification(this, id, r.getMedicineName()));
            startRingtone(r.getRingtoneUri());
            startVoice(r.getVoicePath());
            startVibration();
        });

        return START_STICKY;
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
        try { if (ringtonePlayer != null) { ringtonePlayer.stop(); ringtonePlayer.release(); } }
        catch (Exception ignored) {}
        ringtonePlayer = null;
        try { if (voicePlayer != null) { voicePlayer.stop(); voicePlayer.release(); } }
        catch (Exception ignored) {}
        voicePlayer = null;
        try { if (vibrator != null) vibrator.cancel(); } catch (Exception ignored) {}
        vibrator = null;
        try { if (wakeLock != null && wakeLock.isHeld()) wakeLock.release(); } catch (Exception ignored) {}
        wakeLock = null;
        super.onDestroy();
    }
}
