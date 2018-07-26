package de.splitnass.util;

import de.splitnass.data.BerechnungsEvent;
import de.splitnass.data.BerechnungsListener;


public class ErgebnisToText implements BerechnungsListener {

    public static final String NEW_LINE = System.getProperty("line.separator");
    protected StringBuilder text = new StringBuilder();
    
    @Override
    public void handle(BerechnungsEvent event) {
       switch (event.getTyp()) {
           case KEIN_ERGEBNIS: {
               text.append("Kein Ergebnis - gespaltener Arsch");
               break;
           }
           case RE_UND_KONTRA_FALSCHE_ANSAGEN: {
               text.append("Re und Kontra haben falsche Ansagen gemacht: gespaltener Arsch");
               break;
           }
           case RE_OMA: {
               text.append("Re hat nichts angesagt und keine 6 oder besser gewonnen: gespaltener Arsch");
               break;
           }
           case KONTRA_OMA: {
               text.append("Kontra hat nichts angesagt und keine 6 oder besser gewonnen: gespaltener Arsch");
               break;
           }
           case SCHWARZ: {
               for (int i = 0; i < event.getValue(); i++) {
                    text.append("schwarz ");
               }
               text.append(", ");
               break;
           }
           case KEINE_3: {
               for (int i = 0; i < event.getValue(); i++) {
                   text.append("keine 3 ");
               }
               text.append(", ");
               break;
           }
           case KEINE_6: {
               for (int i = 0; i < event.getValue(); i++) {
                   text.append("keine 6 ");
               }
               text.append(", ");
               break;
           }
           case KEINE_9: {
               for (int i = 0; i < event.getValue(); i++) {
                   text.append("keine 9 ");
               }
               text.append(", ");
               break;
           }
           case HUNDERTZWANZIG: {
               for (int i = 0; i < event.getValue(); i++) {
                   text.append("120 ");
               }
               break;
           }
           case GEGEN_DIE_ALTEN: {
               text.append(NEW_LINE).append("gegen die Alten");
               break;
           }
           case GEGEN_DIE_SAU: {
               text.append(NEW_LINE).append("gegen die Sau");
               break;
           }
           case SOLO: {
               text.append(NEW_LINE).append("Solo");
               break;
           }
           case SOLO_VERLOREN: {
               text.append(NEW_LINE).append("Solo verloren");
               break;
           }
           case RE_VON_VORNEHEREIN: {
               text.append(NEW_LINE).append("Re von vorneherein");
               break;
           }
           case KONTRA_VON_VORNEHEREIN: {
               text.append(NEW_LINE).append("Kontra von vorneherein");
               break;
           }
           case EXTRAPUNKTE: {
               text.append(NEW_LINE);
               text.append(String.format("%d %s", event.getValue(),
                       (event.getValue() > 1 || event.getValue() < -1) ? "Extrapunkte" : "Extrapunkt" ));
               break;
           }
           case PUNKTE_VOR_BOCK: {
               text.append(NEW_LINE).append(String.format("-> %d %s",
                       event.getValue(), event.getValue() == 1 ? "Punkt" : "Punkte"));
               break;
           }
           case BOECKE: {
               text.append(NEW_LINE).append(String.format("%d %s",
                       event.getValue(), event.getValue() > 1 ? "Böcke" : "Bock"));
               break;
           }
           case ERGEBNIS: {
               text.append(NEW_LINE).append(NEW_LINE).append("Ergebnis: ");
               if (event.getValue()  == 0) {
                   text.append("gespaltener Arsch");
               } else {
                   text.append(String.format("%d %s",
                           event.getValue(), event.getValue() == 1 ? "Punkt" : "Punkte"));
               }
               break;
           }
       }
    }

    
    public String getErgebnisAsText() {
        return text.toString();  
    }

    public void clearErgebnisAsText() {
        text.setLength(0);
    }
    
}
