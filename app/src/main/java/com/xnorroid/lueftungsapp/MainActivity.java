package com.xnorroid.lueftungsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    public static boolean timerRunning = false;

    public static boolean getTimerRunning() {
        return timerRunning;
    }

    public static void setTimerRunning(boolean running) {
        timerRunning = running;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        EditText open = findViewById(R.id.editTextOpen);
        EditText closed = findViewById(R.id.editTextClosed);
        ToggleButton toggle = findViewById(R.id.toggleButtonOpenClose);
        Button start = findViewById(R.id.buttonStart);
        Button stop = findViewById(R.id.buttonStop);
        Button database = findViewById(R.id.buttonDatabase);

        //gespeicherter Standartwert setzen (wenn verfügbar)
        SharedPreferences mSharedPreferences = getSharedPreferences("data", 0);
        if (mSharedPreferences.contains("timeOpen")) {
            open.setText(String.valueOf(mSharedPreferences.getLong("timeOpen", 5)));
        }
        if (mSharedPreferences.contains("timeClosed")) {
            closed.setText(String.valueOf(mSharedPreferences.getLong("timeClosed", 20)));
        }
        if (mSharedPreferences.contains("openFirst")) {
            toggle.setChecked(!mSharedPreferences.getBoolean("openFirst", true));
        }

        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> { //toggleButtonOpenClose Knopf setzt Reihenfolge der Timer
            SharedPreferences.Editor edit = mSharedPreferences.edit();
            edit.putBoolean("openFirst", !isChecked);
            edit.apply();
        });

        database.setOnClickListener(v -> {
            //ListDataActivity Klasse aufrufen
            Intent intent = new Intent(MainActivity.this, ListDataActivity.class);
            startActivity(intent);
        });

        start.setOnClickListener(v -> {
            if (!getTimerRunning()) {


                if (Integer.parseInt(open.getText().toString()) == 0 || open.getText().toString().length() == 0) {
                    open.setText("1");
                }

                if (Integer.parseInt(closed.getText().toString()) == 0 || closed.getText().toString().length() == 0) {
                    closed.setText("1");
                }

                SharedPreferences.Editor edit = mSharedPreferences.edit();
                edit.putLong("timeOpen", Long.parseLong(open.getText().toString()));
                edit.putLong("timeClosed", Long.parseLong(closed.getText().toString()));
                edit.apply();

                this.startService();
            } else {
                toastMessage("Timer läuft bereits");
            }
        });

        stop.setOnClickListener(v -> {
            NotificationManagerCompat.from(this).cancelAll();
            this.stopService();
        });
    }

    private void startService() {
        setTimerRunning(true);

        Intent serviceIntent = new Intent(this, ForegroundService.class);
        ContextCompat.startForegroundService(this, serviceIntent); //Klasse ForegroundService im Vordergrund starten
    }

    private void stopService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent); //Klasse ForegroundService stoppen

        setTimerRunning(false);
    }

    /**
     * Toast Nachricht
     *
     * @param message Toast Nachricht
     */
    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}