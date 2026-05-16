package com.oushodh.shoron.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.oushodh.shoron.data.AppDatabase;
import com.oushodh.shoron.data.Reminder;
import com.oushodh.shoron.util.AlarmScheduler;

import java.util.List;
import java.util.concurrent.Executors;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent != null ? intent.getAction() : null;
        Log.d(TAG, "Boot action=" + action);
        final Context appCtx = context.getApplicationContext();
        final PendingResult pr = goAsync();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Reminder> all = AppDatabase.getInstance(appCtx)
                        .reminderDao()
                        .getAllEnabledSync();
                for (Reminder r : all) {
                    AlarmScheduler.schedule(appCtx, r);
                }
                Log.d(TAG, "Rescheduled " + all.size() + " reminders");
            } catch (Exception e) {
                Log.e(TAG, "Boot reschedule failed", e);
            } finally {
                pr.finish();
            }
        });
    }
}
