package com.oushodh.shoron.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.oushodh.shoron.data.AppDatabase;
import com.oushodh.shoron.data.Reminder;
import com.oushodh.shoron.service.AlarmService;
import com.oushodh.shoron.util.AlarmScheduler;

import java.util.concurrent.Executors;

public class AlarmActionReceiver extends BroadcastReceiver {

    public static final String ACTION_TAKEN = "com.oushodh.shoron.ACTION_TAKEN";
    public static final String ACTION_SNOOZE = "com.oushodh.shoron.ACTION_SNOOZE";
    public static final String EXTRA_ID = "extra_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        final long id = intent.getLongExtra(EXTRA_ID, -1);
        if (id < 0 || action == null) return;
        final Context appCtx = context.getApplicationContext();

        // Stop alarm service
        Intent stop = new Intent(appCtx, AlarmService.class);
        appCtx.stopService(stop);

        if (ACTION_TAKEN.equals(action)) {
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(appCtx);
                db.reminderDao().markTaken(id, true, System.currentTimeMillis());
                Reminder r = db.reminderDao().getById(id);
                if (r != null && r.isRepeatDaily() && r.isEnabled()) {
                    AlarmScheduler.schedule(appCtx, r);
                }
            });
        } else if (ACTION_SNOOZE.equals(action)) {
            AlarmScheduler.snooze(appCtx, id, 5 * 60 * 1000L);
        }
    }
}
