package de.splitnass.android;

import de.splitnass.data.Runde;
import de.splitnass.data.Spieltag;

public class OurApp extends android.app.Application {

    private Spieltag spieltag;

    public OurApp() {
        super();
    }

    public String getAusstehendeBoecke() {
        if (spieltag == null) return "";
        int doppelBock = 0, einfachBock = 0;
        Runde r = spieltag.getNaechsteRunde(spieltag.getAktuelleRunde());
        while (r != null) {
            if (r.getBoecke() == 2) {
                doppelBock++;
            }  else if (r.getBoecke() == 1) {
                einfachBock++;
            }  else break;
            r = spieltag.getNaechsteRunde(r);
        }
        StringBuilder message = new StringBuilder();
        if (doppelBock > 0) {
            message.append(doppelBock).append(doppelBock > 1 ? " Runden" : " Runde").append(" Doppelböcke");
        }
        if (einfachBock > 0) {
            if (message.length() > 0) {
                message.append(" und ");
            }
            message.append(einfachBock).append(einfachBock > 1 ? " Runden" : " Runde").append(" Einfachbock");
        }
        if (message.length() == 0) {
            message.append("Keine weiteren Bockrunden vorhanden!");
        }
        return message.toString();
    }

    public Spieltag getSpieltag() {
        return spieltag;
    }

    public void setSpieltag(Spieltag spieltag) {
        this.spieltag = spieltag;
    }
}
