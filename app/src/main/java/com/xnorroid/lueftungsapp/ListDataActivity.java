package com.xnorroid.lueftungsapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class ListDataActivity extends AppCompatActivity {

    private DatabaseHelper mDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Datenbank");
        setContentView(R.layout.list_layout);

        mDatabaseHelper = new DatabaseHelper(ListDataActivity.this);
    }

    //Action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.export, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Exportieren Button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.buttonExport) { //expotiert hier
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) { //nur fragen wen niedriger Android 11
                //Chfeck ob Berechtigung erteilt
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    //Keine Berechtigung
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    // Permission is granted
                    exportFile(); //expotiert hier
                }
            } else {
                // ist größer/gleich Android 11
                exportFile(); //expotiert hier
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportFile() { //expotiert hier
        SQLiteDatabase mSQLiteDatabase = mDatabaseHelper.getReadableDatabase();
        Cursor c;

        try {
            String TABLE_NAME = "lueftungszeiten";
            String COL4 = "bis";
            c = mSQLiteDatabase.rawQuery(" SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL4 + " DESC ", null); //sortieren nach Datum
            int rowcount;
            int colcount;
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String filename = "Lueftungsapp_Daten.csv";
            File saveFile = new File(downloadDir, filename); //Datei erstellen
            FileWriter fw = new FileWriter(saveFile);

            BufferedWriter bw = new BufferedWriter(fw);
            rowcount = c.getCount();
            colcount = c.getColumnCount();
            if (rowcount > 0) {
                c.moveToFirst();

                for (int i = 0; i < colcount; i++) {
                    if (i != colcount - 1) {

                        bw.write(c.getColumnName(i) + ";");

                    } else {

                        bw.write(c.getColumnName(i));

                    }
                }
                bw.newLine();

                for (int i = 0; i < rowcount; i++) {
                    c.moveToPosition(i);

                    for (int j = 0; j < colcount; j++) {
                        if (j != colcount - 1)
                            bw.write(c.getString(j) + ";");
                        else
                            bw.write(c.getString(j));
                    }
                    bw.newLine();
                }
                bw.flush();
                toastMessage("Datenbank exportiert nach Downloads");
            }
        } catch (Exception ex) {
            if (mSQLiteDatabase.isOpen()) {
                mSQLiteDatabase.close();
                toastMessage(ex.getMessage());
            }

        } finally {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.populateListView();
    }

    private void populateListView() {
        //Daten in Listview laden
        Cursor data = mDatabaseHelper.getData();
        ArrayList<String[]> allItems = new ArrayList<>();

        while (data.moveToNext()) {
            //Werte aus ensprechender Spalte holen
            String id = data.getString(0);
            String was = data.getString(1);
            String von = data.getString(2);
            String bis = data.getString(3);
            String zeit = data.getString(4);

            String[] singleItem = new String[3]; //Stringarray mit den Daten für einen Eintrag in Listview
            singleItem[0] = (was + " für " + zeit + " Minuten"); //zeile1
            singleItem[1] = ("Von " + von + " bis " + bis); //zeile2
            singleItem[2] = id; //id
            allItems.add(singleItem); //zu Arraylist hinzufügen
        }

        ListView listView = findViewById(R.id.listView);

        ItemAdapter adapter = new ItemAdapter(this, 0, allItems); //Arraylist an Itemadapter senden
        listView.setAdapter(adapter); //Adapter setzen

        //onItemClickListener auf ListView
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            String[] singleItem = (String[]) adapterView.getItemAtPosition(i);
            Intent editScreenIntent = new Intent(ListDataActivity.this, EditDataActivity.class);
            editScreenIntent.putExtra("line1", singleItem[0]);
            editScreenIntent.putExtra("line2", singleItem[1]);
            editScreenIntent.putExtra("item_id", Integer.parseInt(singleItem[2]));
            startActivity(editScreenIntent);
        });
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