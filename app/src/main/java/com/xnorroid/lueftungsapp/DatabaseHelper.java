package com.xnorroid.lueftungsapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TABLE_NAME = "lueftungszeiten";
    private static final String COL1 = "ID";
    private static final String COL2 = "was";
    private static final String COL3 = "von";
    private static final String COL4 = "bis";
    private static final String COL5 = "Zeit";

    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + "("
                + COL1 + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL2 + " TEXT NOT NULL, "
                + COL3 + " TEXT NOT NULL, "
                + COL4 + " TEXT NOT NULL, "
                + COL5 + " Text NOT NULL);";
        db.execSQL( createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Schreibt Daten in Datenbank
     *
     * @param was
     * @param von
     * @param bis
     * @param Zeit
     */
    public void addData(String was, String von, String bis, String Zeit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, was);
        contentValues.put(COL3, von);
        contentValues.put(COL4, bis);
        contentValues.put(COL5, Zeit);

        db.insert(TABLE_NAME, null, contentValues);
    }

    /**
     * Gibt alle Daten aus Datenbank zurück
     *
     * @return cursor
     */
    public Cursor getData() {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = " SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL1 + " DESC "; //sortieren nach Datum
        return db.rawQuery(selectQuery, null);
    }

    /**
     * Delete from database
     *
     * @param id
     */
    public void deleteId(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE "
                + COL1 + " = '" + id + "'";
        db.execSQL(query);
    }
}