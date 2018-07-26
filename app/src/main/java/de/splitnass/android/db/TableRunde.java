package de.splitnass.android.db;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class TableRunde implements BaseColumns {

    public static final String TABLE_NAME = "Runde";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_SPIELTAG_ID = "spieltagID";
    public static final String COLUMN_START = "start";
    public static final String COLUMN_ENDE = "ende";
    //SPIELER: TableRundeSpieler
    public static final String COLUMN_GEBER = "geber";
    public static final String COLUMN_AUFSPIELER = "aufspieler";
    //GEWINNER: TableRundeSpieler
    public static final String COLUMN_RE_VON_VORNE_HEREIN = "reVonVorneHerein";
    public static final String COLUMN_RE_ANGESAGT = "reAngesagt";
    public static final String COLUMN_KONTRA_VON_VORNE_HEREIN = "kontraVonVorneHerein";
    public static final String COLUMN_KONTRA_ANGESAGT = "kontraAngesagt";
    public static final String COLUMN_BOECKE = "boecke";
    public static final String COLUMN_BOECKE_BEI_BEGINN = "boeckeBeiBeginn";
    public static final String COLUMN_SOLO = "solo";
    public static final String COLUMN_RE = "re";
    public static final String COLUMN_KONTRA = "kontra";
    public static final String COLUMN_RE_GEWINNT = "reGewinnt";
    public static final String COLUMN_GEGEN_DIE_ALTEN = "gegenDieAlten";
    public static final String COLUMN_GEGEN_DIE_SAU = "gegenDieSau";
    public static final String COLUMN_EXTRAPUNKTE = "extrapunkte";
    public static final String COLUMN_ARMUT = "armut";
    public static final String COLUMN_HERZ_GEHT_RUM = "herzGehtRum";
    public static final String COLUMN_ERGBENIS = "ergebnis";
    public static final String COLUMN_ERGEBNIS_STRING = "ergebnisString";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_ID + " INTEGER, " +
            COLUMN_SPIELTAG_ID + " INTEGER, " +
            COLUMN_START + " INTEGER, " +
            COLUMN_ENDE + " INTEGER, " +
            COLUMN_GEBER + " INTEGER, " +
            COLUMN_AUFSPIELER + " INTEGER, " +
            COLUMN_RE_VON_VORNE_HEREIN + " INTEGER, " +
            COLUMN_RE_ANGESAGT + " INTEGER, " +
            COLUMN_KONTRA_VON_VORNE_HEREIN + " INTEGER, " +
            COLUMN_KONTRA_ANGESAGT + " INTEGER, " +
            COLUMN_BOECKE + " INTEGER, " +
            COLUMN_BOECKE_BEI_BEGINN + " INTEGER, " +
            COLUMN_SOLO + " INTEGER, " +
            COLUMN_RE + " INTEGER, " +
            COLUMN_KONTRA + " INTEGER, " +
            COLUMN_RE_GEWINNT + " INTEGER, " +
            COLUMN_GEGEN_DIE_ALTEN + " INTEGER, " +
            COLUMN_GEGEN_DIE_SAU + " INTEGER, " +
            COLUMN_EXTRAPUNKTE + " INTEGER, " +
            COLUMN_ARMUT + " INTEGER, " +
            COLUMN_HERZ_GEHT_RUM + " INTEGER, " +
            COLUMN_ERGBENIS + " INTEGER, " +
            COLUMN_ERGEBNIS_STRING + " TEXT)";

    static void createTable(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    static void dropTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

    }

}
