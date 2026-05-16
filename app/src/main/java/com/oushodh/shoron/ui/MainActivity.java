package com.oushodh.shoron.ui;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.oushodh.shoron.R;
import com.oushodh.shoron.data.Reminder;
import com.oushodh.shoron.ui.adapter.ReminderAdapter;
import com.oushodh.shoron.util.AlarmScheduler;
import com.oushodh.shoron.viewmodel.ReminderViewModel;

public class MainActivity extends AppCompatActivity {

    private ReminderViewModel vm;
    private ReminderAdapter adapter;
    private View emptyView;

    private final ActivityResultLauncher<String> notifPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {});

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView rv = findViewById(R.id.recycler);
        emptyView = findViewById(R.id.empty_view);
        FloatingActionButton fab = findViewById(R.id.fab_add);
        View btnSettings = findViewById(R.id.btn_settings);

        adapter = new ReminderAdapter(new ReminderAdapter.Listener() {
            @Override
            public void onToggle(Reminder r, boolean enabled) {
                r.setEnabled(enabled);
                vm.update(r);
                if (enabled) {
                    AlarmScheduler.schedule(MainActivity.this, r);
                } else {
                    AlarmScheduler.cancel(MainActivity.this, r.getId());
                }
            }

            @Override
            public void onEdit(Reminder r) {
                Intent i = new Intent(MainActivity.this, EditReminderActivity.class);
                i.putExtra(EditReminderActivity.EXTRA_ID, r.getId());
                startActivity(i);
            }

            @Override
            public void onDelete(Reminder r) {
                AlarmScheduler.cancel(MainActivity.this, r.getId());
                vm.delete(r);
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        vm = new ViewModelProvider(this).get(ReminderViewModel.class);
        vm.getAll().observe(this, list -> {
            adapter.submit(list);
            emptyView.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });

        fab.setOnClickListener(v -> startActivity(new Intent(this, AddReminderActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        requestRuntimePermissions();
        checkExactAlarmPermission();
    }

    private void requestRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (am != null && !am.canScheduleExactAlarms()) {
                Toast.makeText(this, R.string.toast_exact_alarm, Toast.LENGTH_LONG).show();
                Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                i.setData(Uri.parse("package:" + getPackageName()));
                try { startActivity(i); } catch (Exception ignored) {}
            }
        }
    }
}
