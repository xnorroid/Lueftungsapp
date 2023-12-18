package com.xnorroid.lueftungsapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Eintrag löschen");
        setContentView(R.layout.edit_data_layout);
        Button delete = findViewById(R.id.buttonDelete);
        TextView line1 = findViewById(R.id.textViewLine1);
        TextView line2 = findViewById(R.id.textViewLine2);
        TextView item_id = findViewById(R.id.textViewItem_id);
        DatabaseHelper mDatabaseHelper = new DatabaseHelper(EditDataActivity.this);

        //extra aus ListDataActivity empfangen
        Intent receivedIntent = getIntent();
        String Zeile1 = receivedIntent.getStringExtra("line1");
        String Zeile2 = receivedIntent.getStringExtra("line2");
        int id = receivedIntent.getIntExtra("item_id", -1); //-1 ist der Standardwert

        line1.setText(Zeile1); //Text in Textfelder schreiben
        line2.setText(Zeile2);
        item_id.setText(String.valueOf(id));

        delete.setOnClickListener(view -> {
            mDatabaseHelper.deleteId(id);
            line1.setText("");
            line2.setText("");
            item_id.setText("");
            toastMessage("Eintrag wurde aus Datenbank gelöscht");
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