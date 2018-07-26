package de.splitnass.android.db;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class TableSpieltagSpieler implements BaseColumns {

    public static final String TABLE_NAME = "SpieltagSpieler";
    public static final String COLUMN_SPIELTAG_ID = "spieltagID";
    public static final String COLUMN_SPIELER = "spieler";
    public static final String COLUMN_IS_AKTIV = "isAktiv";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_SPIELTAG_ID + " INTEGER, " +
            COLUMN_SPIELER + " INTEGER, " +
            COLUMN_IS_AKTIV + " INTEGER)";

    static void createTable(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    static void dropTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

}
