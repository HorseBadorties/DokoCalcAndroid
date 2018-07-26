package de.splitnass.android.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.text.Html;
import android.view.*;
import android.widget.*;

import de.splitnass.R;
import de.splitnass.android.OurApp;
import de.splitnass.android.activities.MainActivity;

import de.splitnass.data.Runde;
import de.splitnass.data.Spieler;


public class RundenInfoFragment extends Fragment implements GestureDetector.OnGestureListener {

    private GestureDetectorCompat mDetector;
    private static final String FIVE_SPACES = "&nbsp&nbsp&nbsp&nbsp&nbsp";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View result = inflater.inflate(R.layout.fragment_rundeninfo, container, false);
        result.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mDetector.onTouchEvent(event);
            }
        });

        mDetector = new GestureDetectorCompat(this.getActivity(),this);
        return result;

    }

    public void fill(Runde r) {
        OurApp app = (OurApp)getActivity().getApplicationContext();
        String status = "";
        if (r.isDummy()) {
            status = "Dummy-Runde";
        } else if (r.isBeendet()) {
            status = "bereits gespielte Runde";
        }  else if (r.isLaufend()) {
            status = "aktuell laufende Runde";
        }  else {
            status = "noch nicht begonnene Runde";
        }
        setTitle(String.format("Runde Nr. %d von %d (%s)",
                r.getId(), app.getSpieltag().getRundenAnzahl(), status));

        String text = "";
        if (r.isDummy()) {
            text = r.getErgebnisString();
        } else if (r.isBeendet()) {
            text = String.format("Geber: %s %s Ergebnis: %d %s Gewinner: %s",
                    r.getGeber(), FIVE_SPACES, r.getErgebnis(), FIVE_SPACES, getGewinner(r));
        }  else if (r.isGestartet()) {
            text = String.format("Geber: %s %s Böcke: %d",
                    r.getGeber(), FIVE_SPACES, r.getBoeckeBeiBeginn());
            Runde vorherigeRunde = r.getSpieltag().getVorherigeRunde(r);
            if (vorherigeRunde != null) {
                text += String.format("%s Ergebnis vorherige Runde: %d",
                        FIVE_SPACES, vorherigeRunde.getErgebnis());
            }
            //text1 += "     (" + app.getAusstehendeBoecke() + ")";
        }  else {
            text = "Böcke: " + r.getBoecke();
        }
        if (r.isGestartet()) {
            text += "<br>Punktestand: " + r.getPunktestand();
        }
        setText(text);

    }


    private static String getGewinner(Runde r) {
        StringBuilder result = new StringBuilder();
        for (Spieler spieler : r.getGewinner()) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(spieler.getName());
        }
        return result.toString();
    }


    private void setTitle(String newText) {
        ((TextView) this.getView().findViewById(R.id.txtAktuelleRunde)).setText(newText);
    }
    private void setText(String newText) {
        ((TextView) this.getView().findViewById(R.id.text1)).setText(Html.fromHtml(newText));
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {}

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        ((MainActivity)getActivity()).geheZu(null);
    }


    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        boolean vorwaerts = e1.getX() > e2.getX();
        if (vorwaerts) {
            // ((MainActivity)getActivity()).forward(null);
        } else {
            // ((MainActivity)getActivity()).backward(null);
        }
        return true;
    }

}