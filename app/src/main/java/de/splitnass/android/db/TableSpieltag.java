package de.splitnass.android.db;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class TableSpieltag implements BaseColumns {

    public static final String TABLE_NAME = "Spieltag";
    public static final String COLUMN_IS_AKTIV = "isAktiv";
    public static final String COLUMN_START = "start";
    public static final String COLUMN_ENDE = "ende";
    //RUNDEN: TableRunde
    public static final String COLUMN_AKTUELLE_RUNDE = "aktuelleRunde";
    //SPIELER: TableSpieltagSpieler

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_IS_AKTIV + " INTEGER, " +
            COLUMN_START + " INTEGER, " +
            COLUMN_ENDE + " INTEGER, " +
            COLUMN_AKTUELLE_RUNDE + " INTEGER)";

    static void createTable(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    static void dropTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

    }

}
