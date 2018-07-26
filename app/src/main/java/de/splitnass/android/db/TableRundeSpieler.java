package de.splitnass.android.db;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class TableRundeSpieler implements BaseColumns {

    public static final String TABLE_NAME = "RundeSpieler";
    public static final String COLUMN_RUNDE_ID = "rundeID";
    public static final String COLUMN_SPIELER = "spieler";
    public static final String COLUMN_IS_GEWINNER = "isGewinner";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_RUNDE_ID + " INTEGER, " +
            COLUMN_SPIELER + " INTEGER, " +
            COLUMN_IS_GEWINNER + " INTEGER)";

    static void createTable(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    static void dropTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

}
