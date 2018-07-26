package de.splitnass.android.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "SplitnAss.db";

    private static DatabaseHelper instance;

    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        TableRunde.createTable(db);
        TableRundeSpieler.createTable(db);
        TableSpieler.createTable(db);
        TableSpieltag.createTable(db);
        TableSpieltagSpieler.createTable(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        TableRunde.dropTable(db);
        TableRunde.createTable(db);
        TableRundeSpieler.dropTable(db);
        TableRundeSpieler.createTable(db);
        TableSpieler.dropTable(db);
        TableSpieler.createTable(db);
        TableSpieltag.dropTable(db);
        TableSpieltag.createTable(db);
        TableSpieltagSpieler.dropTable(db);
        TableSpieltagSpieler.createTable(db);
    }
}
