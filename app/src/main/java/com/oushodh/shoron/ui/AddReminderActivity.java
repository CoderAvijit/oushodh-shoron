package com.oushodh.shoron.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.oushodh.shoron.R;
import com.oushodh.shoron.data.Reminder;
import com.oushodh.shoron.util.AlarmScheduler;
import com.oushodh.shoron.util.VoiceRecorder;
import com.oushodh.shoron.viewmodel.ReminderViewModel;

import java.io.File;

public class AddReminderActivity extends AppCompatActivity {

    protected ReminderViewModel vm;
    protected EditText etName, etNote;
    protected TimePicker timePicker;
    protected RadioGroup rgRepeat;
    protected TextView tvRingtone, tvVoiceStatus;
    protected Button btnPickRingtone, btnRecord, btnPlayVoice, btnSave, btnCancel;

    protected Uri ringtoneUri;
    protected File voiceFile;
    protected VoiceRecorder recorder;
    protected boolean isRecording = false;
    protected MediaPlayer previewPlayer;

    private final ActivityResultLauncher<String> micLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startRecording();
                else Toast.makeText(this, R.string.toast_mic_denied, Toast.LENGTH_SHORT).show();
            });

    private final ActivityResultLauncher<Intent> ringtoneLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
                if (res.getResultCode() == Activity.RESULT_OK && res.getData() != null) {
                    Uri u = res.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    if (u != null) {
                        ringtoneUri = u;
                        tvRingtone.setText(RingtoneManager.getRingtone(this, u).getTitle(this));
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);
        bindViews();
        vm = new ViewModelProvider(this).get(ReminderViewModel.class);

        ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        tvRingtone.setText(R.string.default_ringtone);

        btnPickRingtone.setOnClickListener(v -> pickRingtone());
        btnRecord.setOnClickListener(v -> toggleRecord());
        btnPlayVoice.setOnClickListener(v -> previewVoice());
        btnSave.setOnClickListener(v -> save());
        btnCancel.setOnClickListener(v -> finish());
    }

    protected void bindViews() {
        etName = findViewById(R.id.et_name);
        etNote = findViewById(R.id.et_note);
        timePicker = findViewById(R.id.time_picker);
        timePicker.setIs24HourView(false);
        rgRepeat = findViewById(R.id.rg_repeat);
        tvRingtone = findViewById(R.id.tv_ringtone);
        tvVoiceStatus = findViewById(R.id.tv_voice_status);
        btnPickRingtone = findViewById(R.id.btn_pick_ringtone);
        btnRecord = findViewById(R.id.btn_record);
        btnPlayVoice = findViewById(R.id.btn_play_voice);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    protected void pickRingtone() {
        Intent i = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
                RingtoneManager.TYPE_ALARM | RingtoneManager.TYPE_NOTIFICATION);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.choose_ringtone));
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringtoneUri);
        ringtoneLauncher.launch(i);
    }

    protected void toggleRecord() {
        if (!isRecording) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                micLauncher.launch(Manifest.permission.RECORD_AUDIO);
                return;
            }
            startRecording();
        } else {
            stopRecording();
        }
    }

    protected void startRecording() {
        recorder = new VoiceRecorder();
        File f = recorder.start(this, System.currentTimeMillis());
        if (f != null) {
            isRecording = true;
            btnRecord.setText(R.string.btn_stop_record);
            tvVoiceStatus.setText(R.string.recording);
        } else {
            Toast.makeText(this, R.string.toast_record_failed, Toast.LENGTH_SHORT).show();
        }
    }

    protected void stopRecording() {
        if (recorder != null) voiceFile = recorder.stop();
        isRecording = false;
        btnRecord.setText(R.string.btn_record);
        tvVoiceStatus.setText(voiceFile != null && voiceFile.exists()
                ? getString(R.string.voice_saved) : getString(R.string.no_voice));
        btnPlayVoice.setEnabled(voiceFile != null && voiceFile.exists());
    }

    protected void previewVoice() {
        if (voiceFile == null || !voiceFile.exists()) return;
        try {
            if (previewPlayer != null) { previewPlayer.release(); previewPlayer = null; }
            previewPlayer = new MediaPlayer();
            previewPlayer.setDataSource(voiceFile.getAbsolutePath());
            previewPlayer.prepare();
            previewPlayer.start();
        } catch (Exception e) {
            Toast.makeText(this, R.string.toast_play_failed, Toast.LENGTH_SHORT).show();
        }
    }

    protected void save() {
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            etName.setError(getString(R.string.error_name_required));
            return;
        }
        Reminder r = buildReminder(new Reminder());
        vm.insert(r, id -> runOnUiThread(() -> {
            r.setId(id);
            AlarmScheduler.schedule(getApplicationContext(), r);
            Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show();
            finish();
        }));
    }

    protected Reminder buildReminder(Reminder r) {
        timePicker.clearFocus();
        r.setMedicineName(etName.getText().toString().trim());
        r.setNote(etNote.getText().toString().trim());
        r.setHour(timePicker.getHour());
        r.setMinute(timePicker.getMinute());
        int interval = 1;
        int sel = rgRepeat.getCheckedRadioButtonId();
        if (sel == R.id.rb_2day) interval = 2;
        else if (sel == R.id.rb_3day) interval = 3;
        else if (sel == R.id.rb_weekly) interval = 7;
        r.setRepeatIntervalDays(interval);
        r.setRepeatDaily(interval == 1);
        r.setEnabled(true);
        r.setRingtoneUri(ringtoneUri != null ? ringtoneUri.toString() : null);
        if (voiceFile != null) r.setVoicePath(voiceFile.getAbsolutePath());
        return r;
    }

    @Override
    protected void onDestroy() {
        if (recorder != null && isRecording) recorder.cancel();
        if (previewPlayer != null) {
            try { previewPlayer.release(); } catch (Exception ignored) {}
            previewPlayer = null;
        }
        super.onDestroy();
    }
}
