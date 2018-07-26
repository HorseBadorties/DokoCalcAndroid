package de.splitnass.android.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import de.splitnass.R;
import de.splitnass.android.OurApp;
import de.splitnass.android.activities.MainActivity;
import de.splitnass.android.db.DatabaseHelper;
import de.splitnass.android.db.SpieltagPersistor;
import de.splitnass.data.Spieler;
import de.splitnass.data.Spieltag;

public class SpieltageFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_spieltage, container, false);

        TableLayout table = (TableLayout)view.findViewById(R.id.spieltage_table);
        addRow(table, "ID", "Datum", "Gespielte Runden", "Ergebnis");
        for (Spieltag s : loadSpieltage()) {
            addRow(table,
                    String.valueOf(s.getId()),
                    String.format("%tF", s.getStart()),
                    String.valueOf(s.getAktuelleRunde().getId()),
                    sortByPunkte(s.getPunktestand(s.getAktuelleRunde())).toString());
        }
        return view;
    }

    private Map<Spieler, Integer> sortByPunkte(Map<Spieler, Integer> punktestand) {
        TreeMap<Spieler, Integer> result =
                new TreeMap<Spieler, Integer>(new ValueComparator(punktestand));
        result.putAll(punktestand);
        return result;
    }

    private static class ValueComparator implements Comparator<Spieler> {
        Map<Spieler, Integer> base;

        public ValueComparator(Map<Spieler, Integer> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with
        // equals.
        public int compare(Spieler a, Spieler b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }

    private void addRow(TableLayout table, String... col) {
        TableRow tr = new TableRow(getContext());
        for (String column : col) {
            tr.addView(makeView(column));
        }
        table.addView(tr);
        tr.setClickable(true);
        tr.setFocusable(true);

        tr.setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(View v) {
                String id = ((TextView)((TableRow)v).getChildAt(0)).getText().toString();
                Toast.makeText(SpieltageFragment.this.getContext(), id + " clicked", Toast.LENGTH_SHORT).show();
                Spieltag s = SpieltagPersistor.loadById(
                        DatabaseHelper.getInstance(getActivity().getApplication().getApplicationContext()),
                        Integer.valueOf(id));
                OurApp app = (OurApp) getActivity().getApplication();
                Spieltag bisherigerSpieltag = app.getSpieltag();
                if (s != null) {
                    if (bisherigerSpieltag != null && s.getId() != bisherigerSpieltag.getId()) {
                        bisherigerSpieltag.removeAllListener();
                        app.setSpieltag(s);
                    } else if (bisherigerSpieltag == null) {
                        app.setSpieltag(s);
                    }
                }
                return true;
            }
        });
    }

    private View makeView(String s) {
        TextView txt = (TextView)View.inflate(getContext(), R.layout.spieltage_item, null);
        txt.setText(s);
        txt.setTextColor(Color.WHITE);
        return txt;
    }

    public List<Spieltag> loadSpieltage() {
        List<Spieltag> result = new ArrayList<Spieltag>();
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(getActivity().getApplication().getApplicationContext());
        for (long id : SpieltagPersistor.getSpieltagIDs(dbHelper)) {
            result.add(SpieltagPersistor.loadById(dbHelper, id));
        }
        return result;
    }

}
