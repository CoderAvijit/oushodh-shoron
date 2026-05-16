package com.oushodh.shoron.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.oushodh.shoron.R;
import com.oushodh.shoron.util.BatteryOptHelper;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TextView tvBatteryStatus = findViewById(R.id.tv_battery_status);
        TextView tvMfrNote = findViewById(R.id.tv_mfr_note);
        Button btnBattery = findViewById(R.id.btn_battery);
        Button btnBack = findViewById(R.id.btn_back);

        updateBattery(tvBatteryStatus);
        tvMfrNote.setText(BatteryOptHelper.manufacturerNote());

        btnBattery.setOnClickListener(v -> {
            try {
                startActivity(BatteryOptHelper.requestIgnoreIntent(this));
            } catch (Exception ignored) {}
        });
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBattery(findViewById(R.id.tv_battery_status));
    }

    private void updateBattery(TextView tv) {
        boolean ok = BatteryOptHelper.isIgnoringBatteryOptimizations(this);
        tv.setText(ok ? R.string.battery_optimized_off : R.string.battery_optimized_on);
    }
}
