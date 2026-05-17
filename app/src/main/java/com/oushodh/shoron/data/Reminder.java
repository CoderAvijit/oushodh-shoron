package com.oushodh.shoron.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reminders")
public class Reminder {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String medicineName = "";

    private int hour;
    private int minute;

    private String note;
    private String voicePath;
    private String ringtoneUri;

    private boolean repeatDaily = true;
    private int repeatIntervalDays = 1;
    private boolean enabled = true;
    private boolean takenToday = false;

    private long lastTakenMillis = 0L;
    private long createdAt = System.currentTimeMillis();

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(@NonNull String medicineName) { this.medicineName = medicineName; }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }

    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getVoicePath() { return voicePath; }
    public void setVoicePath(String voicePath) { this.voicePath = voicePath; }

    public String getRingtoneUri() { return ringtoneUri; }
    public void setRingtoneUri(String ringtoneUri) { this.ringtoneUri = ringtoneUri; }

    public boolean isRepeatDaily() { return repeatDaily; }
    public void setRepeatDaily(boolean repeatDaily) { this.repeatDaily = repeatDaily; }

    public int getRepeatIntervalDays() { return repeatIntervalDays <= 0 ? 1 : repeatIntervalDays; }
    public void setRepeatIntervalDays(int days) { this.repeatIntervalDays = days <= 0 ? 1 : days; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isTakenToday() { return takenToday; }
    public void setTakenToday(boolean takenToday) { this.takenToday = takenToday; }

    public long getLastTakenMillis() { return lastTakenMillis; }
    public void setLastTakenMillis(long lastTakenMillis) { this.lastTakenMillis = lastTakenMillis; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
