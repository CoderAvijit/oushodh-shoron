# ঔষধ স্মরণ (Oushodh Shoron)

Bengali medicine reminder app — continuously rings until the user confirms "ঔষধ খেয়েছি".

## Build

```
./gradlew assembleDebug
```

Open in Android Studio (Giraffe+) or run from VS Code with the Gradle CLI.

## Project layout

```
app/src/main/
├── AndroidManifest.xml
├── java/com/oushodh/shoron/
│   ├── OushodhApp.java
│   ├── data/         (Reminder, Dao, DB, Repository)
│   ├── viewmodel/    (ReminderViewModel)
│   ├── service/      (AlarmService - foreground)
│   ├── receiver/     (AlarmReceiver, BootReceiver, AlarmActionReceiver)
│   ├── ui/           (Splash, Main, Add, Edit, Alarm, Settings + adapter)
│   └── util/         (AlarmScheduler, NotificationHelper, VoiceRecorder, BatteryOptHelper)
└── res/              (layouts, drawables, themes, strings — Bengali)
```

## Required runtime permissions
POST_NOTIFICATIONS, RECORD_AUDIO, SCHEDULE_EXACT_ALARM (Android 12+), ignore battery optimization.
