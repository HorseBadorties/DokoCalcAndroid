package de.splitnass.android.util;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.splitnass.R;
import de.splitnass.data.Runde;
import de.splitnass.data.Spieltag;
import de.splitnass.data.Spieler;

public class RundenblattItemView extends LinearLayout {

    private Spieltag spieltag;
    private int spielerCount;

    public static final String COL_RUNDE = "Runde";
    public static final String COL_BOECKE = "Böcke";
    public static final String COL_PUNKTE = "Punkte";
    public static final String COL_NOTIZ = "Notiz";


    public RundenblattItemView(Context context, Spieltag spieltag) {
        super(context);
        this.spieltag = spieltag;

        int smallerWidth = getResources().getInteger(R.integer.RundenblattItemSmallerWidth);
        doAddView(createTextView(COL_RUNDE), smallerWidth, 0.0f);
        for (Spieler s : spieltag.getSpieler()) {
            doAddView(createTextView(s.getName()), LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f);
        }
        spielerCount = spieltag.getSpieler().size();
        doAddView(createTextView(COL_BOECKE), smallerWidth, 0.0f);
        doAddView(createTextView(COL_PUNKTE), smallerWidth, 0.0f);
        //doAddView(createTextView(COL_NOTIZ), LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
    }

    private void doAddView(View v, int width, float weight) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                width, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(1, 1, 1, 1);
        layoutParams.weight = weight;
        addView(v, layoutParams);
    }

    public void fillAsHeader() {
        findTextView(COL_RUNDE).setText(COL_RUNDE);
        for (Spieler s : spieltag.getSpieler()) {
            findTextView(s.getName()).setText(s.getName());
        }
        findTextView(COL_BOECKE).setText(COL_BOECKE);
        findTextView(COL_PUNKTE).setText(COL_PUNKTE);
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setBackgroundColor(Color.DKGRAY);
        }
        //findTextView(COL_NOTIZ).setText(COL_NOTIZ);
    }

    public void fill(Runde r) {
        findTextView(COL_RUNDE).setText(String.valueOf(r.getId()));
        for (Spieler s : spieltag.getSpieler()) {
            String text = "";
            if (r.isGestartet()) {
                if (r.isBeendet()) {
                    text = "*";
                    if (r.getGewinner().contains(s)) {
                        text = String.valueOf(spieltag.getPunktestand(r, s));
                    } else if (!r.getSpieler().contains(s)) {
                        text = "-";
                    }
                } else if (r.getGeber().equals(s)) {
                    text = "Geber";
                } else if (!s.isAktiv()) {
                    text = "-";
                }
            } else {
                if (!s.isAktiv()) {
                    text = "-";
                }
            }
            findTextView(s.getName()).setText(text);
        }
        findTextView(COL_BOECKE).setText(boeckeString(r.getBoecke()));
        if (r.isBeendet()) {
            findTextView(COL_PUNKTE).setText(String.valueOf(r.getErgebnis()));
        } else {
            findTextView(COL_PUNKTE).setText("");
        }
        //findTextView(COL_NOTIZ).setText(r.getErgebnisString());
    }

    private static String boeckeString(int boecke) {
        switch (boecke) {
        case 1: return "|";
        case 2: return "||";
        case 3: return "|||";
        }
        return "";
    }

    private TextView findTextView(String columnName) {
        return (TextView)findViewById(Math.abs(columnName.hashCode()));
    }

    private View createTextView(String columnName) {
        View result = inflate(getContext(), R.layout.rundenblattitem, null);
        result.setId(Math.abs(columnName.hashCode()));
        return result;
    }

    public Spieltag getSpieltag() {
        return spieltag;
    }

    public int getSpielerCount() {
        return spielerCount;
    }

}
