package com.xnorroid.lueftungsapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ForegroundService extends Service {

    private static final String CHANNEL_1 = "Dauerhafte Benachrichtigung";
    private static final String CHANNEL_2 = "Lüftungsbenachrichtigungen";
    private static final String ACTION_OPEN =
            "ACTION_CONFIRM_WINDOW_OPEN";
    private static final String ACTION_CLOSE =
            "ACTION_CONFIRM_WINDOW_CLOSED";
    public static long previousTime = 0;
    private static long timeOpenMs;
    private static long timeClosedMs;
    private final NotificationReceiver mReceiver = new NotificationReceiver();

    public static long getPreviousTime() {
        return previousTime;
    }

    public static void setPreviousTime(long time) {
        previousTime = time;
    }

    @Override
    public void onCreate() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_OPEN);
        intentFilter.addAction(ACTION_CLOSE);
        registerReceiver(mReceiver, intentFilter);
    }

    public void confirmWindowOpen() {
        this.saveInDatabase(true);
        this.timerClosed();
    }


    public void confirmWindowClosed() {
        this.saveInDatabase(false);
        this.timerOpen();
    }

    private void saveInDatabase(boolean open) {
        DatabaseHelper mDatabaseHelper = new DatabaseHelper(this);
        long timeUnix = System.currentTimeMillis();
        if (!(getPreviousTime() == 0)) { //beim ersten mal nichts hinzufügen
            String was;
            if (open) {
                was = "Geöffnet";
            } else {
                was = "Geschlossen";
            }

            Date dateVon = new java.util.Date(getPreviousTime());
            SimpleDateFormat sdfVon = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss"); //in Datumsformat umwandeln
            String von = sdfVon.format(dateVon); //in String speichern

            Date dateBis = new java.util.Date(timeUnix);
            SimpleDateFormat sdfBis = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss"); //in Datumsformat umwandeln
            String bis = sdfBis.format(dateBis); //in String speichern

            long differenz = timeUnix - getPreviousTime();
            long sekunden = (differenz / 1000) % 60;
            long minuten = (differenz / (1000 * 60)) % 60;
            String zeit = String.format("%02d:%02d", minuten, sekunden); //in String speichern

            mDatabaseHelper.addData(was, von, bis, zeit);
        }
        setPreviousTime(timeUnix);
    }

    private void timerOpen() {
        new CountDownTimer(timeOpenMs, 1000) {
            @Override
            public void onTick(long remainingMs) {
                if (!MainActivity.getTimerRunning()) {
                    cancel();
                }
                long seconds = (remainingMs / 1000) % 60;
                long minutes = (remainingMs / (1000 * 60)) % 60;
                String time = String.format("%02d:%02d", minutes, seconds);
                startForeground(1, permanentNotification(timeOpenMs, timeOpenMs - remainingMs, "Lüften!", "noch " + time));
            }

            @Override
            public void onFinish() {
                startForeground(1, permanentNotification(timeOpenMs, timeOpenMs, "Lüften!", "noch 00:00"));
                notifyClosed();
            }
        }.start();
    }

    private void timerClosed() {
        new CountDownTimer(timeClosedMs, 1000) {
            @Override
            public void onTick(long remainingMs) {
                if (!MainActivity.getTimerRunning()) {
                    cancel();
                }
                long seconds = (remainingMs / 1000) % 60;
                long minutes = (remainingMs / (1000 * 60)) % 60;
                String time = String.format("%02d:%02d", minutes, seconds);
                startForeground(1, permanentNotification(timeClosedMs, timeClosedMs - remainingMs, "Fenster schließen!", "noch " + time));
            }

            @Override
            public void onFinish() {
                startForeground(1, permanentNotification(timeClosedMs, timeClosedMs, "Fenster schließen!", "noch 00:00"));
                notifyOpen();
            }
        }.start();
    }

    private Notification permanentNotification(long max, long current, String title, String text) {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_1)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_timer)
                .setProgress((int) max, (int) current, false)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void notifyOpen() {
        Uri ringtone = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.ringtone);  //eigener Klingelton

        Intent openIntent = new Intent(ACTION_CLOSE);
        PendingIntent openPendingIntent = PendingIntent.getBroadcast
                (this, 2, openIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationManagerCompat.from(this).cancel(3);
        Notification openWindow = new NotificationCompat.Builder(this, CHANNEL_2)
                .setSmallIcon(R.drawable.ic_open)
                .setContentTitle("Bitte öffnen Sie die Fenster")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(ringtone)
                .addAction(R.mipmap.ic_launcher, "Bestätigen", openPendingIntent)
                .build();
        NotificationManagerCompat.from(this).notify(2, openWindow);
    }

    private void notifyClosed() {
        Uri ringtone = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.ringtone);  //eigener Klingelton

        Intent closeIntent = new Intent(ACTION_OPEN);
        PendingIntent closePendingIntent = PendingIntent.getBroadcast
                (this, 3, closeIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationManagerCompat.from(this).cancel(2);
        Notification closeWindow = new NotificationCompat.Builder(this, CHANNEL_2)
                .setSmallIcon(R.drawable.ic_closed)
                .setContentTitle("Bitte schließen Sie die Fenster")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(ringtone)
                .addAction(R.mipmap.ic_launcher, "Bestätigen", closePendingIntent)
                .build();
        NotificationManagerCompat.from(this).notify(3, closeWindow);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.createNotificationChannels();
        SharedPreferences mSharedPreferences = getSharedPreferences("data", 0);
        timeOpenMs = mSharedPreferences.getLong("timeOpen", 5) * 60000; //Zeit Offen lesen und in Millisekunden konvertieren
        timeClosedMs = mSharedPreferences.getLong("timeClosed", 20) * 60000; //Zeit Zu lesen und in Millisekunden konvertieren
        setPreviousTime(0); //Immer wenn neu gestartet wir auf null setzen

        startForeground(1, permanentNotification(1, 0, "Bestätigen Sie um den Timer zu starten", ""));
        if (mSharedPreferences.getBoolean("openFirst",true)) {
            this.notifyOpen();
        } else {
            this.notifyClosed();
        }
        //ab startForeground() beginnt der Foreground service
        //stopSelf() um den Foreground service selbst zu beenden
        return START_NOT_STICKY;
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //nur benötigt ab Android 8.0
            Uri ringtone = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.ringtone);  //eigener Klingelton
            //Uri alarmTon = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);  //Standard Klingelton
            if (ringtone == null) {
                ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);  //Backup falls es keinen Klingelton gibt
            }
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_1,
                    "Dauerhafte Benachrichtigung", NotificationManager.IMPORTANCE_LOW);

            NotificationChannel notifyChannel = new NotificationChannel(CHANNEL_2,
                    "Lüftungsbenachrichtigungen", NotificationManager.IMPORTANCE_HIGH);
            notifyChannel.enableVibration(true);
            notifyChannel.enableLights(true);
            notifyChannel.setSound(ringtone, null);

            NotificationManager mNotificationManager = getSystemService(NotificationManager.class);
            mNotificationManager.createNotificationChannel(serviceChannel);
            mNotificationManager.createNotificationChannel(notifyChannel);
        }
    }

    @Override
    public void onDestroy() { //Unregister mReciever wenn Activity beendet wird
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //Broadcast receiver Klasse wird gerufen wenn Bestätigen Knopf gedrückt wurde
    private class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); //Parameter intent enthält action
            switch (action) {
                case ACTION_CLOSE:
                    NotificationManagerCompat.from(context).cancel(2);
                    confirmWindowClosed();
                    break;
                case ACTION_OPEN:
                    NotificationManagerCompat.from(context).cancel(3);
                    confirmWindowOpen();
                    break;
            }
        }
    }
}