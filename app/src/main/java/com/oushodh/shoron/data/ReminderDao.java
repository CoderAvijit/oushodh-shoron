package com.oushodh.shoron.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReminderDao {

    @Insert
    long insert(Reminder reminder);

    @Update
    void update(Reminder reminder);

    @Delete
    void delete(Reminder reminder);

    @Query("SELECT * FROM reminders ORDER BY hour ASC, minute ASC")
    LiveData<List<Reminder>> getAll();

    @Query("SELECT * FROM reminders ORDER BY hour ASC, minute ASC")
    List<Reminder> getAllSync();

    @Query("SELECT * FROM reminders WHERE enabled = 1")
    List<Reminder> getAllEnabledSync();

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    Reminder getById(long id);

    @Query("UPDATE reminders SET takenToday = :taken, lastTakenMillis = :time WHERE id = :id")
    void markTaken(long id, boolean taken, long time);

    @Query("UPDATE reminders SET takenToday = 0")
    void resetTakenForNewDay();

    @Query("DELETE FROM reminders WHERE id = :id")
    void deleteById(long id);
}
