package com.oushodh.shoron.ui;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.oushodh.shoron.R;
import com.oushodh.shoron.data.AppDatabase;
import com.oushodh.shoron.data.Reminder;
import com.oushodh.shoron.util.AlarmScheduler;

import java.io.File;
import java.util.concurrent.Executors;

public class EditReminderActivity extends AddReminderActivity {

    public static final String EXTRA_ID = "extra_id";
    private long reminderId = -1;
    private Reminder current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reminderId = getIntent().getLongExtra(EXTRA_ID, -1);
        if (reminderId < 0) { finish(); return; }
        Executors.newSingleThreadExecutor().execute(() -> {
            current = AppDatabase.getInstance(getApplicationContext())
                    .reminderDao().getById(reminderId);
            if (current == null) {
                runOnUiThread(this::finish);
                return;
            }
            runOnUiThread(this::populate);
        });
    }

    private void populate() {
        etName.setText(current.getMedicineName());
        etNote.setText(current.getNote() == null ? "" : current.getNote());
        timePicker.setHour(current.getHour());
        timePicker.setMinute(current.getMinute());
        swRepeat.setChecked(current.isRepeatDaily());
        if (current.getRingtoneUri() != null) {
            ringtoneUri = Uri.parse(current.getRingtoneUri());
            try {
                tvRingtone.setText(RingtoneManager.getRingtone(this, ringtoneUri).getTitle(this));
            } catch (Exception ignored) {
                tvRingtone.setText(R.string.default_ringtone);
            }
        }
        if (current.getVoicePath() != null) {
            File f = new File(current.getVoicePath());
            if (f.exists()) {
                voiceFile = f;
                tvVoiceStatus.setText(R.string.voice_saved);
                btnPlayVoice.setEnabled(true);
            }
        }
    }

    @Override
    protected void save() {
        if (current == null) return;
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            etName.setError(getString(R.string.error_name_required));
            return;
        }
        buildReminder(current);
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(getApplicationContext())
                    .reminderDao().update(current);
            AlarmScheduler.cancel(getApplicationContext(), current.getId());
            AlarmScheduler.schedule(getApplicationContext(), current);
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.toast_updated, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
