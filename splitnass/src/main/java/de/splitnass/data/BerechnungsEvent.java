package de.splitnass.data;

import java.util.EventObject;

public class BerechnungsEvent extends EventObject {


    private Typ typ;
    private int value;


    public BerechnungsEvent(Runde source, Typ typ, int value) {
        super(source);
        this.typ = typ;
        this.value = value;
    }

    @Override
    public Runde getSource() {
        return (Runde)super.getSource();
    }

    public Typ getTyp() {
        return typ;
    }

    public int getValue() {
        return value;
    }

    public static enum Typ {
        KEIN_ERGEBNIS,
        RE_UND_KONTRA_FALSCHE_ANSAGEN,
        RE_OMA,
        KONTRA_OMA,
        SCHWARZ,
        KEINE_3,
        KEINE_6,
        KEINE_9,
        HUNDERTZWANZIG,
        GEGEN_DIE_ALTEN,
        GEGEN_DIE_SAU,
        SOLO,
        SOLO_VERLOREN,
        RE_VON_VORNEHEREIN,
        KONTRA_VON_VORNEHEREIN,
        EXTRAPUNKTE,
        PUNKTE_VOR_BOCK,
        BOECKE,
        ERGEBNIS
    }

}
