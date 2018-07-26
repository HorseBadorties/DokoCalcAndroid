package de.splitnass.util;

import de.splitnass.data.BerechnungsEvent;

public class ErgebnisToUschiText extends ErgebnisToText {

    private String punkteString = "Ergebnis: gespaltener Ahrsch";

    public String getPunkteAsText() {
        return punkteString;
    }

    @Override
    public void handle(BerechnungsEvent event) {
        switch (event.getTyp()) {
            case KEIN_ERGEBNIS: {
                text.append("Kein Ergebnis - gespaltener Ahrsch");
                break;
            }
            case RE_UND_KONTRA_FALSCHE_ANSAGEN: {
                text.append("Reh und Kontra haben falsche Ansagen gemacht: gespaltener Ahrsch");
                break;
            }
            case RE_OMA: {
                text.append("Reh hat nichts angesagt und keine 6 oder besser gewonnen: gespaltener Ahrsch");
                break;
            }
            case KONTRA_OMA: {
                text.append("Kontra hat nichts angesagt und keine 6 oder besser gewonnen: gespaltener Ahrsch");
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
                    text.append("Hundertzwanzig, ");
                }
                break;
            }
            case GEGEN_DIE_ALTEN: {
                text.append(NEW_LINE).append("gegen die Alten,");
                break;
            }
            case GEGEN_DIE_SAU: {
                text.append(NEW_LINE).append("gegen die Sau,");
                break;
            }
            case SOLO: {
                text.append(NEW_LINE).append("Solo,");
                break;
            }
            case SOLO_VERLOREN: {
                text.append(NEW_LINE).append("Solo verloren,");
                break;
            }
            case RE_VON_VORNEHEREIN: {
                text.append(NEW_LINE).append("Reh von vorne herein,");
                break;
            }
            case KONTRA_VON_VORNEHEREIN: {
                text.append(NEW_LINE).append("Kontra von vorne herein,");
                break;
            }
            case EXTRAPUNKTE: {
                text.append(NEW_LINE);
                if (event.getValue() > 0) {
                    text.append(event.getValue() != 1
                            ? String.valueOf(event.getValue()) : "Ein").append(" Tack:,");
                } else {
                    text.append(event.getValue() != -1
                            ? String.valueOf(Math.abs(event.getValue())) : "Ein").append(" Tack runter:,");
                }
                break;
            }
            case PUNKTE_VOR_BOCK: {
                text.append(NEW_LINE);
                text.append(": Macht ").append(event.getValue() != 1 ? String.valueOf(event.getValue()) : "Einen").
                        append(event.getValue() != 1 ? " Punkte:" : " Punkt:");
                break;
            }
            case BOECKE: {
                text.append(NEW_LINE).append(NEW_LINE);
                if (event.getValue() == 1) {
                    text.append(": ,Ein Bock:");
                } else if (event.getValue() > 1) {
                    text.append(": ").append(event.getValue()).append(" Böcke:");
                }
                break;
            }
            case ERGEBNIS: {
                punkteString = "Ergebnis der Runde: ";
                if (event.getValue() == 0) {
                    punkteString += "gespaltener Ahrsch";
                } else {
                    punkteString += (event.getValue() != 1 ? String.valueOf(event.getValue()) : "Ein")
                        + (event.getValue() != 1 ? " Punkte" : " Punkt");
                }
                text.append(NEW_LINE).append(NEW_LINE).append(punkteString);
                break;
            }
        }
    }

}
