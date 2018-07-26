package de.splitnass.android.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;

import de.splitnass.android.OurApp;
import de.splitnass.R;
import de.splitnass.android.util.ToggleButtonGroupTableLayout;
import de.splitnass.data.Runde;


public class AngesagtFragment extends Fragment {

    private OurApp app;
    private transient boolean isFilling = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View result = inflater.inflate(R.layout.fragment_ansagen, container, false);
        AdapterView.OnItemSelectedListener selectionListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isFilling && view != null && app.getSpieltag() != null) {
                    app.getSpieltag().getAktuelleRunde().setReAngesagt(getReAngesagt());
                    app.getSpieltag().getAktuelleRunde().setKontraAngesagt(getKontraAngesagt());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        ((ToggleButtonGroupTableLayout)result.findViewById(R.id.reRadioGroup)).
                setOnItemSelectedListener(selectionListener);
        ((ToggleButtonGroupTableLayout)result.findViewById(R.id.kontraRadioGroup)).
                setOnItemSelectedListener(selectionListener);

        View.OnClickListener clickListener = new View.OnClickListener() {
            public void onClick(View v) {
                checkBoxChecked(v);
            }
        };
        ((Button)result.findViewById(R.id.vonVornehereinRe)).setOnClickListener(clickListener);
        ((Button)result.findViewById(R.id.vonVornehereinKontra)).setOnClickListener(clickListener);
        app = (OurApp)result.getContext().getApplicationContext();
        return result;

    }

    /*
     * Wird "von vorneherein" angeklickt, wird automatisch "Re" bzw. "Kontra" notiert, falls noch keine Ansage notiert wurde.
     */
    private void checkBoxChecked(View view) {
        if (((CheckBox) view).isChecked()) {
            ToggleButtonGroupTableLayout radioGroup = null;
            int firstId = -1;
            if (view.getId() == R.id.vonVornehereinRe) {
                radioGroup = (ToggleButtonGroupTableLayout) this.getView().findViewById(R.id.reRadioGroup);
                firstId = R.id.re;
            } else {
                radioGroup = (ToggleButtonGroupTableLayout) this.getView().findViewById(R.id.kontraRadioGroup);
                firstId = R.id.kontra;
            }
            if (radioGroup.getCheckedRadioButtonId() == -1) {
                radioGroup.check(firstId);
            }
        }
    }


    public boolean isReVonVorneherein() {
        return ((CheckBox) this.getView().findViewById(R.id.vonVornehereinRe)).isChecked();
    }

    private void setReVorneherein(boolean value) {
        ((CheckBox) this.getView().findViewById(R.id.vonVornehereinRe)).setChecked(value);
    }

    public int getReAngesagt() {
        ToggleButtonGroupTableLayout group =
                ((ToggleButtonGroupTableLayout) this.getView().findViewById(R.id.reRadioGroup));
        switch (group.getCheckedRadioButtonId())  {
            case R.id.re : return Runde.KEINE_120;
            case R.id.reKeine9 : return Runde.KEINE_9;
            case R.id.reKeine6 : return Runde.KEINE_6;
            case R.id.reKeine3 : return Runde.KEINE_3;
            case R.id.reSchwarz : return Runde.SCHWARZ;
            default : return 0;
        }
    }

    private void setReAngesagt(int value) {
        ToggleButtonGroupTableLayout group =
                ((ToggleButtonGroupTableLayout) this.getView().findViewById(R.id.reRadioGroup));
        switch (value)  {
            case Runde.KEINE_120 : group.check(R.id.re); break;
            case Runde.KEINE_9 : group.check(R.id.reKeine9); break;
            case Runde.KEINE_6 : group.check(R.id.reKeine6); break;
            case Runde.KEINE_3 : group.check(R.id.reKeine3); break;
            case Runde.SCHWARZ : group.check(R.id.reSchwarz); break;
            default : group.clearCheck();
        }
    }

    public boolean isKontraVonVorneherein() {
        return ((CheckBox) this.getView().findViewById(R.id.vonVornehereinKontra)).isChecked();
    }

    private void setKontraVorneherein(boolean value) {
        ((CheckBox) this.getView().findViewById(R.id.vonVornehereinKontra)).setChecked(value);
    }

    public int getKontraAngesagt() {
        ToggleButtonGroupTableLayout group =
                ((ToggleButtonGroupTableLayout) this.getView().findViewById(R.id.kontraRadioGroup));
        switch (group.getCheckedRadioButtonId())  {
            case R.id.kontra : return Runde.KEINE_120;
            case R.id.kontraKeine9 : return Runde.KEINE_9;
            case R.id.kontraKeine6 : return Runde.KEINE_6;
            case R.id.kontraKeine3 : return Runde.KEINE_3;
            case R.id.kontraSchwarz : return Runde.SCHWARZ;
            default : return 0;
        }
    }

    private void setKontraAngesagt(int value) {
        ToggleButtonGroupTableLayout group =
                ((ToggleButtonGroupTableLayout) this.getView().findViewById(R.id.kontraRadioGroup));
        switch (value)  {
            case Runde.KEINE_120 : group.check(R.id.kontra); break;
            case Runde.KEINE_9 : group.check(R.id.kontraKeine9); break;
            case Runde.KEINE_6 : group.check(R.id.kontraKeine6); break;
            case Runde.KEINE_3 : group.check(R.id.kontraKeine3); break;
            case Runde.SCHWARZ : group.check(R.id.kontraSchwarz); break;
            default : group.clearCheck();
        }
    }

    public void reset() {
        ((ToggleButtonGroupTableLayout) this.getView().findViewById(R.id.reRadioGroup)).reset();
        ((ToggleButtonGroupTableLayout) this.getView().findViewById(R.id.kontraRadioGroup)).reset();
        setReVorneherein(false);
        setReAngesagt(0);
        setKontraVorneherein(false);
        setKontraAngesagt(0);
    }

    public void fill(Runde r) {
        isFilling = true;
        try {
            setReVorneherein(r.getReVonVorneHerein() > 0);
            setReAngesagt(r.getReAngesagt());
            setKontraVorneherein(r.getKontraVonVorneHerein() > 0);
            setKontraAngesagt(r.getKontraAngesagt());
        } finally {
            isFilling = false;
        }
    }

}