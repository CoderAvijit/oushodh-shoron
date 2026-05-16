package com.oushodh.shoron.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;

public class BatteryOptHelper {

    public static boolean isIgnoringBatteryOptimizations(Context ctx) {
        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        return pm != null && pm.isIgnoringBatteryOptimizations(ctx.getPackageName());
    }

    @SuppressLint("BatteryLife")
    public static Intent requestIgnoreIntent(Context ctx) {
        Intent i = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        i.setData(Uri.parse("package:" + ctx.getPackageName()));
        return i;
    }

    public static String manufacturerNote() {
        String mfr = android.os.Build.MANUFACTURER.toLowerCase();
        if (mfr.contains("xiaomi") || mfr.contains("redmi") || mfr.contains("poco")) {
            return "Xiaomi/MIUI: Settings → Apps → Manage apps → ঔষধ স্মরণ → Autostart ON, Battery saver → No restrictions.";
        }
        if (mfr.contains("oppo") || mfr.contains("realme")) {
            return "Oppo/Realme: Settings → Battery → App Battery Management → ঔষধ স্মরণ → Allow background activity ON.";
        }
        if (mfr.contains("vivo")) {
            return "Vivo: i-Manager → App manager → Autostart → enable ঔষধ স্মরণ.";
        }
        if (mfr.contains("huawei") || mfr.contains("honor")) {
            return "Huawei/Honor: Settings → Battery → App launch → ঔষধ স্মরণ → Manage manually, allow all.";
        }
        if (mfr.contains("samsung")) {
            return "Samsung: Settings → Apps → ঔষধ স্মরণ → Battery → Unrestricted.";
        }
        if (mfr.contains("oneplus")) {
            return "OnePlus: Settings → Battery → Battery optimization → ঔষধ স্মরণ → Don't optimize.";
        }
        return "Settings → Apps → ঔষধ স্মরণ → Battery → Unrestricted.";
    }
}
