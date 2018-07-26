package de.splitnass.android.db;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import de.splitnass.data.Runde;
import de.splitnass.data.Spieler;
import de.splitnass.data.Spieltag;
import de.splitnass.rules.Solo;

import java.util.ArrayList;
import java.util.List;

public class SpieltagPersistor {

    public static synchronized void save(DatabaseHelper dbHelper, Spieltag spieltag) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {

            if (spieltag.getId() > 0) {
                //Spieltag wurde schon mal gespeichert - alten Satz löschen
                delete(db, spieltag);
            }

            if (spieltag.isGestartet() && !spieltag.isBeendet()) {
                //alte Spieltage als inaktiv markieren
                db.execSQL("UPDATE " + TableSpieltag.TABLE_NAME + " SET " + TableSpieltag.COLUMN_IS_AKTIV + " = 0");
            }
            //Spieltag
            ContentValues spieltagValues = new ContentValues();
            spieltagValues.put(TableSpieltag.COLUMN_IS_AKTIV, spieltag.isGestartet() && !spieltag.isBeendet());
            spieltagValues.put(TableSpieltag.COLUMN_START, spieltag.getStart() != null ? spieltag.getStart().getTime() : 0);
            spieltagValues.put(TableSpieltag.COLUMN_ENDE, spieltag.getEnde() != null ? spieltag.getEnde().getTime() : 0);
            spieltagValues.put(TableSpieltag.COLUMN_AKTUELLE_RUNDE, spieltag.getAktuelleRunde() != null ? spieltag.getAktuelleRunde().getId() : null);
            long newSpieltagId = db.insert(TableSpieltag.TABLE_NAME, null, spieltagValues);
            spieltag.setId(newSpieltagId);

            //SpieltagSpieler
            for (Spieler spieler : spieltag.getSpieler()) {
                ContentValues values = new ContentValues();
                values.put(TableSpieltagSpieler.COLUMN_SPIELTAG_ID, newSpieltagId);
                values.put(TableSpieltagSpieler.COLUMN_SPIELER, spieler.getId());
                values.put(TableSpieltagSpieler.COLUMN_IS_AKTIV, spieler.isAktiv());
                db.insert(TableSpieltagSpieler.TABLE_NAME, null, values);
            }

            //Runde
            for (Runde runde : spieltag.getRunden()) {
                ContentValues values = new ContentValues();
                values.put(TableRunde.COLUMN_ID, runde.getId());
                values.put(TableRunde.COLUMN_SPIELTAG_ID, newSpieltagId);
                values.put(TableRunde.COLUMN_START, runde.getStart() != null ? runde.getStart().getTime() : 0);
                values.put(TableRunde.COLUMN_ENDE, runde.getEnde() != null ? runde.getEnde().getTime() : 0);
                values.put(TableRunde.COLUMN_GEBER, runde.getGeber() != null ? runde.getGeber().getId() : 0);
                values.put(TableRunde.COLUMN_AUFSPIELER, runde.getAufspieler() != null ? runde.getAufspieler().getId() : 0);
                values.put(TableRunde.COLUMN_RE_VON_VORNE_HEREIN, runde.getReVonVorneHerein());
                values.put(TableRunde.COLUMN_RE_ANGESAGT, runde.getReAngesagt());
                values.put(TableRunde.COLUMN_KONTRA_VON_VORNE_HEREIN, runde.getKontraVonVorneHerein());
                values.put(TableRunde.COLUMN_KONTRA_ANGESAGT, runde.getKontraAngesagt());
                values.put(TableRunde.COLUMN_BOECKE, runde.getBoecke());
                values.put(TableRunde.COLUMN_BOECKE_BEI_BEGINN, runde.getBoeckeBeiBeginn());
                values.put(TableRunde.COLUMN_SOLO, runde.getSolo() != null ? runde.getSolo().getId() : 0);
                values.put(TableRunde.COLUMN_RE, runde.getRe());
                values.put(TableRunde.COLUMN_KONTRA, runde.getKontra());
                values.put(TableRunde.COLUMN_RE_GEWINNT, runde.isReGewinnt());
                values.put(TableRunde.COLUMN_GEGEN_DIE_ALTEN, runde.isGegenDieAlten());
                values.put(TableRunde.COLUMN_GEGEN_DIE_SAU, runde.isGegenDieSau());
                values.put(TableRunde.COLUMN_EXTRAPUNKTE, runde.getExtrapunkte());
                values.put(TableRunde.COLUMN_ARMUT, runde.isArmut());
                values.put(TableRunde.COLUMN_HERZ_GEHT_RUM, runde.isHerzGehtRum());
                values.put(TableRunde.COLUMN_ERGBENIS, runde.getErgebnis());
                values.put(TableRunde.COLUMN_ERGEBNIS_STRING, runde.getErgebnisString());
                long newRundeId = db.insert(TableRunde.TABLE_NAME, null, values);
                runde.set_id(newRundeId);
                //RundeSpieler
                for (Spieler spieler : runde.getSpieler()) {
                    ContentValues valuesSpieler = new ContentValues();
                    valuesSpieler.put(TableRundeSpieler.COLUMN_RUNDE_ID, newRundeId);
                    valuesSpieler.put(TableRundeSpieler.COLUMN_SPIELER, spieler.getId());
                    valuesSpieler.put(TableRundeSpieler.COLUMN_IS_GEWINNER, runde.getGewinner().contains(spieler));
                    db.insert(TableRundeSpieler.TABLE_NAME, null, valuesSpieler);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private static void delete(SQLiteDatabase db, Spieltag spieltag) {
        db.execSQL("Delete from " + TableRundeSpieler.TABLE_NAME + " where " + TableRundeSpieler.COLUMN_RUNDE_ID
                + " in (select " + TableRunde._ID + " from " + TableRunde.TABLE_NAME
                + " where " + TableRunde.COLUMN_SPIELTAG_ID + " = " + spieltag.getId() + ")");
        db.execSQL("Delete from " + TableRunde.TABLE_NAME
                + " where " + TableRunde.COLUMN_SPIELTAG_ID + " = " + spieltag.getId());
        db.execSQL("Delete from " + TableSpieltagSpieler.TABLE_NAME
                + " where " + TableSpieltagSpieler.COLUMN_SPIELTAG_ID + " = " + spieltag.getId());
        db.execSQL("Delete from " + TableSpieltag.TABLE_NAME + " where "
                + TableSpieltag._ID + " = " + spieltag.getId());
    }

    public static synchronized Spieltag loadById(DatabaseHelper dbHelper, long id) {
        return load(dbHelper, TableSpieltag._ID + " = " + id);
    }

    public static synchronized Spieltag loadAktivenSpieltag(DatabaseHelper dbHelper) {
        return load(dbHelper, TableSpieltag.COLUMN_IS_AKTIV + " = 1");
    }

    private static Spieltag load(DatabaseHelper dbHelper, String selection) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //Spieltag
        String[] rowsSpieltag = {
                TableSpieltag._ID,
                TableSpieltag.COLUMN_START,
                TableSpieltag.COLUMN_ENDE,
                TableSpieltag.COLUMN_AKTUELLE_RUNDE
        };
        Cursor cursor =
                db.query(TableSpieltag.TABLE_NAME, rowsSpieltag, selection, null, null, null, null);
        if (!cursor.moveToFirst()) {
            return null;
        }
        long spieltagId = cursor.getLong(cursor.getColumnIndexOrThrow(TableSpieltag._ID));
        Spieltag spieltag = new Spieltag();
        spieltag.setId(spieltagId);
        long start = cursor.getLong(cursor.getColumnIndexOrThrow(TableSpieltag.COLUMN_START));
        spieltag.setStart(start > 0 ? new java.util.Date(start) : null);
        long ende = cursor.getLong(cursor.getColumnIndexOrThrow(TableSpieltag.COLUMN_ENDE));
        spieltag.setEnde(ende > 0 ? new java.util.Date(ende) : null);
        int aktuelleRunde = cursor.getInt(cursor.getColumnIndexOrThrow(TableSpieltag.COLUMN_AKTUELLE_RUNDE));
        cursor.close();

        //SpieltagSpieler
        String[] rowsSpieltagSpieler = {
                TableSpieltagSpieler.COLUMN_SPIELER,
                TableSpieltagSpieler.COLUMN_IS_AKTIV
        };
        cursor = db.query(TableSpieltagSpieler.TABLE_NAME, rowsSpieltagSpieler,
                TableSpieltagSpieler.COLUMN_SPIELTAG_ID + " = " + spieltagId, null, null, null, null);
        List<Spieler> spieler = new ArrayList<Spieler>();
        while (cursor.moveToNext()) {
            Spieler sp = Spieler.byId(cursor.getInt(cursor.getColumnIndexOrThrow(TableSpieltagSpieler.COLUMN_SPIELER)));
            sp.setIsAktiv(cursor.getInt(cursor.getColumnIndexOrThrow(TableSpieltagSpieler.COLUMN_IS_AKTIV)) == 1);
            spieler.add(sp);
        };
        cursor.close();
        spieltag.setSpieler(spieler);

        //Runde
        cursor = db.query(TableRunde.TABLE_NAME, null,
                TableRunde.COLUMN_SPIELTAG_ID + " = " + spieltagId, null, null, null, TableRunde.COLUMN_ID + " ASC");
        List<Runde> runden = new ArrayList<Runde>();
        while (cursor.moveToNext()) {
            Runde r = new Runde(spieltag, cursor.getInt(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_ID)));
            r.set_id(cursor.getInt(cursor.getColumnIndexOrThrow(TableRunde._ID)));
            start = cursor.getLong(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_START));
            r.setStart(start > 0 ? new java.util.Date(start) : null);
            ende = cursor.getLong(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_ENDE));
            r.setEnde(ende > 0 ? new java.util.Date(ende) : null);
            r.setGeber(Spieler.byId(cursor.getInt(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_GEBER))));
            r.setAufspieler(Spieler.byId(cursor.getInt(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_AUFSPIELER))));
            r.setReVonVorneHerein(cursor.getInt(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_RE_VON_VORNE_HEREIN)));
            r.setReAngesagt(cursor.getInt(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_RE_ANGESAGT)));
            r.setKontraVonVorneHerein(cursor.getInt(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_KONTRA_VON_VORNE_HEREIN)));
            r.setKontraAngesagt(cursor.getInt(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_KONTRA_ANGESAGT)));
            r.setSolo(Solo.getById(cursor.getInt(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_SOLO))));
            r.setRe(cursor.getInt(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_RE)));
            r.setKontra(cursor.getInt(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_KONTRA)));
            r.setReGewinnt(cursor.getInt(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_RE_GEWINNT)) == 1);
            r.setGegenDieAlten(cursor.getInt(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_GEGEN_DIE_ALTEN)) == 1);
            r.setGegenDieSau(cursor.getInt(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_GEGEN_DIE_SAU)) == 1);
            r.setExtrapunkte(cursor.getInt(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_EXTRAPUNKTE)));
            r.setArmut(cursor.getInt(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_ARMUT))== 1);
            r.setHerzGehtRum(cursor.getInt(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_HERZ_GEHT_RUM)) == 1);
            r.setBoecke(cursor.getInt(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_BOECKE)));
            r.setBoeckeBeiBeginn(cursor.getInt(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_BOECKE_BEI_BEGINN)));
            r.setErgebnis(cursor.getInt(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_ERGBENIS)));
            r.setErgebnisString(cursor.getString(cursor.getColumnIndexOrThrow(TableRunde.COLUMN_ERGEBNIS_STRING)));
            runden.add(r);
        };
        cursor.close();
        //RundeSpieler
        String[] rowsRundeSpieler = {
                TableRundeSpieler.COLUMN_SPIELER,
                TableRundeSpieler.COLUMN_IS_GEWINNER
        };
        for (Runde r : runden) {
            cursor = db.query(TableRundeSpieler.TABLE_NAME, rowsRundeSpieler,
                    TableRundeSpieler.COLUMN_RUNDE_ID + " = " + r.get_id(), null, null, null, null);
            List<Spieler> spielerDerRunde = new ArrayList<Spieler>();
            List<Spieler> gewinner = new ArrayList<Spieler>();
            while (cursor.moveToNext()) {
                Spieler sp = Spieler.byId(cursor.getInt(cursor.getColumnIndexOrThrow(TableRundeSpieler.COLUMN_SPIELER)));
                spielerDerRunde.add(sp);
                if (cursor.getInt(cursor.getColumnIndexOrThrow(TableRundeSpieler.COLUMN_IS_GEWINNER)) == 1) {
                    gewinner.add(sp);
                }
            };
            cursor.close();
            r.setSpieler(spielerDerRunde);
            r.setGewinner(gewinner);
        }
        spieltag.setRunden(runden);
        spieltag.setAktuelleRunde(runden.get(aktuelleRunde-1));
        return spieltag;
    }

    public static synchronized List<Long> getSpieltagIDs(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] rows = { TableSpieltag._ID };
        Cursor cursor = db.query(TableSpieltag.TABLE_NAME, rows, null, null, null, null, null);
        List<Long> result = new ArrayList<Long>();
        while (cursor.moveToNext()) {
            result.add(cursor.getLong(cursor.getColumnIndexOrThrow(TableSpieltag._ID)));
        }
        cursor.close();
        return result;
    }

}
