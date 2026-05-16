package com.oushodh.shoron.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.oushodh.shoron.service.AlarmService;
import com.oushodh.shoron.util.AlarmScheduler;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(AlarmScheduler.EXTRA_REMINDER_ID, -1);
        Log.d(TAG, "Alarm fired id=" + id);
        if (id < 0) return;

        Intent svc = new Intent(context, AlarmService.class);
        svc.putExtra(AlarmService.EXTRA_REMINDER_ID, id);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(svc);
        } else {
            context.startService(svc);
        }
    }
}
