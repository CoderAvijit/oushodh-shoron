package com.oushodh.shoron.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.oushodh.shoron.data.Reminder;
import com.oushodh.shoron.data.ReminderRepository;

import java.util.List;

public class ReminderViewModel extends AndroidViewModel {

    private final ReminderRepository repo;
    private final LiveData<List<Reminder>> all;

    public ReminderViewModel(@NonNull Application application) {
        super(application);
        repo = new ReminderRepository(application);
        all = repo.getAll();
    }

    public LiveData<List<Reminder>> getAll() { return all; }
    public ReminderRepository getRepo() { return repo; }

    public void insert(Reminder r, ReminderRepository.OnInsert cb) { repo.insert(r, cb); }
    public void update(Reminder r) { repo.update(r); }
    public void delete(Reminder r) { repo.delete(r); }
    public void markTaken(long id, boolean taken) { repo.markTaken(id, taken); }
}
