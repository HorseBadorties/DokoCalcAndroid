package de.splitnass.android.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.splitnass.R;
import de.splitnass.android.OurApp;
import de.splitnass.android.db.DatabaseHelper;
import de.splitnass.android.db.SpieltagPersistor;
import de.splitnass.data.Runde;
import de.splitnass.data.Spieler;
import de.splitnass.data.Spieltag;

import java.util.*;


public class SpieltagInfoFragment extends Fragment {

    private Spieltag spieltag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_spieltaginfo, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        update();
    }

    public void update() {
        Spieltag spieltag = ((OurApp)getActivity().getApplication()).getSpieltag();
        TextView textView = (TextView)getView().findViewById(R.id.spieltaginfo_textView);
        if (spieltag == null) {
            textView.setText("Kein aktiver Spieltag.");
        } else {
            this.spieltag = spieltag;
            Runde letzteRunde = spieltag.getVorherigeRunde(spieltag.getAktuelleRunde());
            if (letzteRunde != null) {
                StringBuilder html = new StringBuilder();

                html.append("<html><body>");
                String bullet = String.valueOf('\u2022') + "  ";
                int gespielteRunden = spieltag.getAktuelleRunde().getId()-1;
                html.append(bullet + String.format("Runden gespielt: <b>%d von %d (%s)</b>. Spieltag läuft seit <b>%s</b>.<br>",
                        gespielteRunden, spieltag.getRundenAnzahl(),
                        getPercent(gespielteRunden,  spieltag.getRundenAnzahl()), getSpieldauer()));
                html.append(bullet + String.format("Spielstand: <b>%s</b><br>",
                        printSorted(spieltag.getPunktestand(spieltag.getAktuelleRunde()))));
                html.append(bullet + String.format("Anzahl gewonnene Runden: <b>%s</b><br>", printSorted(getAnzahlSiege())));
                List<Runde> hoechste = getRundenMitHoechstemErgebnis();
                html.append(bullet + String.format("Höchstes Ergebnis: <b>%d Punkte</b> (Runde %s)<br>", hoechste.get(0).getErgebnis(),
                        idList(hoechste)));
                int boeckeCount = getGespielteBoecke();
                html.append(bullet + String.format("Durchschnittliche Anzahl Böcke: <b>%.2g%n</b> pro gespielte Runde<br>",
                        (double)boeckeCount/gespielteRunden));
                int soloCount = getSoloCount();
                html.append(bullet + String.format("Anzahl/Prozentsatz Soli: <b>%d / %s</b><br>",
                        soloCount, getPercent(soloCount, gespielteRunden)));
                int herzGehtRum = getHerzGehtRumCount();
                html.append(bullet + String.format("Anzahl/Prozentsatz Herz geht rum: <b>%d / %s</b><br>",
                        herzGehtRum, getPercent(herzGehtRum, gespielteRunden)));
                int kontraRe = getKontraReCount();
                html.append(bullet + String.format("Anzahl/Prozentsatz Kontra/Re: <b>%d / %s</b><br>",
                        kontraRe, getPercent(kontraRe, gespielteRunden)));
                int gespaltenerArsch = getGespaltenerArschCount();
                html.append(bullet + String.format("Anzahl/Prozentsatz Gespaltener Arsch: <b>%d / %s</b><br>",
                        gespaltenerArsch, getPercent(gespaltenerArsch, gespielteRunden)));
                int armut = getArmutCount();
                html.append(bullet + String.format("Anzahl/Prozentsatz Armut: <b>%d / %s</b><br>",
                        armut, getPercent(armut, gespielteRunden)));
                int spieltage = getAnzahlSpieltage();
                html.append(bullet + String.format("Anzahl Spieltage: <b>%d </b><br>", spieltage));

                html.append("</body></html>");
                textView.setText(Html.fromHtml(html.toString()));
            } else {
                textView.setText("Es wurde noch keine Runde gespielt.");
            }
        }

    }

    private static final long MINUTES = 60 * 1000;
    private static final long HOURS =  60 * MINUTES;

    private int getAnzahlSpieltage() {
        return SpieltagPersistor.getSpieltagIDs(DatabaseHelper.getInstance(getView().getContext().getApplicationContext())).size();
    }

    private String getSpieldauer() {
        long diff = System.currentTimeMillis() - spieltag.getStart().getTime();
        long diffHours = diff / HOURS;
        long diffMinutes = (diff % HOURS) / MINUTES;
        StringBuilder result = new StringBuilder();
        if (diffHours > 0) {
            result.append(diffHours).append(diffHours == 1 ? " Stunde und " : " Stunden und ");
        }
        if (diffMinutes == 0) diffMinutes = 1;
        result.append(diffMinutes).append(diffMinutes == 1 ? " Minute" : " Minuten");
        return result.toString();

    }

    private int getSoloCount() {
        int result = 0;
        for (Runde r : spieltag.getRunden()) {
            if (r.isSolo()) result++;
        }
        return result;
    }

    private int getHerzGehtRumCount() {
        int result = 0;
        for (Runde r : spieltag.getRunden()) {
            if (r.isHerzGehtRum()) result++;
        }
        return result;
    }

    private int getArmutCount() {
        int result = 0;
        for (Runde r : spieltag.getRunden()) {
            if (r.isArmut()) result++;
        }
        return result;
    }

    private int getGespaltenerArschCount() {
        int result = 0;
        for (Runde r : spieltag.getRunden()) {
            if (r.isBeendet() && !r.isDummy() && r.getErgebnis() == 0) result++;
        }
        return result;
    }

    private int getKontraReCount() {
        int result = 0;
        for (Runde r : spieltag.getRunden()) {
            if (r.isBeendet() && !r.isDummy() && r.getReAngesagt()>0 && r.getKontraAngesagt()>0) result++;
        }
        return result;
    }

    private int getGespielteBoecke() {
        int result = 0;
        for (Runde r : spieltag.getRunden()) {
            if (r.isBeendet() && !r.isDummy()) result += r.getBoecke();
        }
        return result;
    }

    private List<Runde> getRundenMitHoechstemErgebnis() {
        List<Runde> result = new ArrayList<Runde>();
        for (Runde r : spieltag.getRunden()) {
            if (r.isBeendet() && !r.isDummy()) {
                if (result.isEmpty()) {
                    result.add(r);
                } else if (r.getErgebnis() > result.get(0).getErgebnis()) {
                    result.clear();
                    result.add(r);
                } else if (r.getErgebnis() == result.get(0).getErgebnis()) {
                    result.add(r);
                }
            }
        }
        return result;
    }

    private static String idList(List<Runde> runden) {
        if (runden.size() == 1) {
            return String.valueOf(runden.get(0).getId());
        } else {
            StringBuilder result = new StringBuilder();
            for (Runde r : runden) {
                if (result.length() > 0) {
                    result.append(", ");
                }
                result.append(r.getId());
            }
            return result.toString();
        }
    }

    private Map<Spieler, Integer> getAnzahlSiege() {
        Map<Spieler, Integer> result = new HashMap<Spieler, Integer>();
        for (Spieler s : spieltag.getAktiveSpieler()) {
            result.put(s, 0);
        }
        for (Runde r : spieltag.getRunden()) {
            if (r.isBeendet() && !r.isDummy()) {
                for (Spieler s : spieltag.getAktiveSpieler()) {
                    if (r.getGewinner().contains(s)) {
                        Integer anzahl = result.get(s);
                        result.put(s, ++anzahl);
                    }
                }
            }
        }
        return result;
    }


    private static String getPercent(double amount, double total) {
       return (int)(amount/total*100d) + "%";
    }

    private static String printSorted(Map<Spieler, Integer> values) {
        TreeSet<Integer> sorted = new TreeSet<Integer>(values.values());
        StringBuilder result = new StringBuilder();
        for (Integer value : sorted.descendingSet()) {
            for (Map.Entry<Spieler, Integer> entry : values.entrySet()) {
                if (entry.getValue().equals(value)) {
                    if (result.length() > 0) {
                        result.append(", ");
                    }
                    result.append(entry.getKey().getName()).append("=").append(value);
                }
            }
        }
        return result.toString();
    }


}