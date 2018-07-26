package de.splitnass.android.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import de.splitnass.data.Spieler;

public class TableSpieler implements BaseColumns {

    public static final String TABLE_NAME = "Spieler";
    public static final String COLUMN_NAME = "name";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME + " TEXT" + ")";

    static void createTable(SQLiteDatabase db) {
        db.execSQL(TableSpieler.CREATE_TABLE);
        for (Spieler s : Spieler.getAll()) {
            insertSpieler(db, s);
        }
    }

    static void dropTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

    }

    static void insertSpieler(SQLiteDatabase db, Spieler s) {
        ContentValues values = new ContentValues();
        values.put(_ID, s.getId());
        values.put(COLUMN_NAME, s.getName());
        db.insert(TABLE_NAME, null, values);
    }

    static void deleteSpieler(SQLiteDatabase db, Spieler s) {
        String selection = _ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(s.getId()) };
        db.delete(TABLE_NAME, selection, selectionArgs);
    }
}
