package com.oushodh.shoron.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReminderRepository {

    private final ReminderDao dao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public ReminderRepository(Context ctx) {
        dao = AppDatabase.getInstance(ctx).reminderDao();
    }

    public LiveData<List<Reminder>> getAll() {
        return dao.getAll();
    }

    public List<Reminder> getAllEnabledSync() {
        return dao.getAllEnabledSync();
    }

    public Reminder getByIdSync(long id) {
        return dao.getById(id);
    }

    public void insert(Reminder r, OnInsert cb) {
        io.execute(() -> {
            long id = dao.insert(r);
            r.setId(id);
            if (cb != null) cb.onInserted(id);
        });
    }

    public void update(Reminder r) {
        io.execute(() -> dao.update(r));
    }

    public void delete(Reminder r) {
        io.execute(() -> dao.delete(r));
    }

    public void deleteById(long id) {
        io.execute(() -> dao.deleteById(id));
    }

    public void markTaken(long id, boolean taken) {
        io.execute(() -> dao.markTaken(id, taken, System.currentTimeMillis()));
    }

    public void resetTakenForNewDay() {
        io.execute(dao::resetTakenForNewDay);
    }

    public ExecutorService io() { return io; }

    public interface OnInsert {
        void onInserted(long id);
    }
}
