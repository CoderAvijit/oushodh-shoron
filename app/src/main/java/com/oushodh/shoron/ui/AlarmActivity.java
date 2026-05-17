package com.oushodh.shoron.ui;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.oushodh.shoron.R;
import com.oushodh.shoron.data.AppDatabase;
import com.oushodh.shoron.data.Reminder;
import com.oushodh.shoron.receiver.AlarmActionReceiver;
import com.oushodh.shoron.service.AlarmService;
import com.oushodh.shoron.util.AlarmScheduler;

import java.util.concurrent.Executors;

public class AlarmActivity extends AppCompatActivity {

    public static final String EXTRA_REMINDER_ID = "extra_reminder_id";
    private long reminderId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupWindow();
        setContentView(R.layout.activity_alarm);

        reminderId = getIntent().getLongExtra(EXTRA_REMINDER_ID, -1);
        TextView tvName = findViewById(R.id.tv_medicine);
        TextView tvNote = findViewById(R.id.tv_note);
        Button btnTaken = findViewById(R.id.btn_taken);
        Button btnSnooze = findViewById(R.id.btn_snooze);

        if (reminderId >= 0) {
            Executors.newSingleThreadExecutor().execute(() -> {
                Reminder r = AppDatabase.getInstance(getApplicationContext())
                        .reminderDao().getById(reminderId);
                if (r != null) {
                    runOnUiThread(() -> {
                        tvName.setText(r.getMedicineName());
                        tvNote.setText(r.getNote() == null || r.getNote().isEmpty()
                                ? getString(R.string.alarm_default_note) : r.getNote());
                    });
                }
            });
        }

        btnTaken.setOnClickListener(v -> onTaken());
        btnSnooze.setOnClickListener(v -> onSnooze());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, reminderId);
    }

    private void setupWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (km != null) km.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
    }

    private void onTaken() {
        AlarmService.stop(getApplicationContext());
        if (reminderId >= 0) {
            final long id = reminderId;
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                db.reminderDao().markTaken(id, true, System.currentTimeMillis());
                Reminder r = db.reminderDao().getById(id);
                if (r != null && r.isEnabled()) {
                    AlarmScheduler.schedule(getApplicationContext(), r, true);
                }
            });
        }
        finishAndRemoveTask();
    }

    private void onSnooze() {
        AlarmService.stop(getApplicationContext());
        if (reminderId >= 0) {
            AlarmScheduler.snooze(this, reminderId, 5 * 60 * 1000L);
        }
        finishAndRemoveTask();
    }

    @Override
    public void onBackPressed() {
        // Block back button while alarm active
    }
}
