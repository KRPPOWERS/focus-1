package com.focusguard.app;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import java.util.Calendar;

public class FocusMonitorService extends Service {
    public static final String CHANNEL_ID = "FocusGuardChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, buildNotification());
        scheduleMidnightReset();
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("FocusGuard Active")
                .setContentText("Your focus shield is running")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentIntent(pi)
                .setOngoing(true)
                .setSilent(true)
                .build();
    }

    private void createNotificationChannel() {
        NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, "FocusGuard", NotificationManager.IMPORTANCE_LOW);
        ch.setDescription("FocusGuard monitoring service");
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
    }

    private void scheduleMidnightReset() {
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.add(Calendar.DAY_OF_MONTH, 1);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, MidnightReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 99, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, midnight.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
