package de.splitnass.android.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.*;
import android.widget.*;

import de.splitnass.R;
import de.splitnass.android.util.ExtrapunkteView;
import de.splitnass.data.Runde;
import de.splitnass.rules.Solo;


public class GespieltFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    /*
     * "GegenSieSau" gibt es bei einigen Soli nicht und "Armut" nicht bei Solo...
     */
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        Solo selectedSolo = (Solo) parent.getItemAtPosition(pos);

        CheckBox cbGegegnDieSau = (CheckBox) this.getView().findViewById(R.id.gegenDieSau);
        if (!selectedSolo.isSauMoeglich()) {
            cbGegegnDieSau.setChecked(false);
        }
        cbGegegnDieSau.setEnabled(selectedSolo.isSauMoeglich());

        CheckBox cbArmut = (CheckBox) this.getView().findViewById(R.id.armut);
        if (selectedSolo != Solo.KEIN_SOLO) {
            cbArmut.setChecked(false);
        }
        cbArmut.setEnabled(selectedSolo == Solo.KEIN_SOLO);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        this.getView().findViewById(R.id.gegenDieSau).setEnabled(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View result = inflater.inflate(R.layout.fragment_gespielt, container, false);

        //Solo-Spinner füllen
        ArrayAdapter<Solo> adapterSolo = new ArrayAdapter<Solo>(this.getActivity(), R.layout.spinner, Solo.getAll());

        adapterSolo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinnerSolo = (Spinner) result.findViewById(R.id.solo);
        spinnerSolo.setAdapter(adapterSolo);
        //ItemListener für Solo
        spinnerSolo.setOnItemSelectedListener(this);

        return result;
    }

    public int getRe() {
        RadioGroup reKontraGroup = (RadioGroup)this.getView().findViewById(R.id.radioButtonGroupReKontra);
        int reKontraId = reKontraGroup.getCheckedRadioButtonId();
        if (reKontraId != R.id.reGewinnt) return 0;

        RadioGroup keineGroup = (RadioGroup)this.getView().findViewById(R.id.radioButtonGroupKeine);

        switch (keineGroup.getCheckedRadioButtonId()) {
        case -1:
            return Runde.KEINE_120;
        case R.id.gespielt120:
            return Runde.KEINE_120;
        case R.id.gespieltKeine9:
            return Runde.KEINE_9;
        case R.id.gespieltKeine6:
            return Runde.KEINE_6;
        case R.id.gespieltKeine3:
            return Runde.KEINE_3;
        case R.id.gespieltSchwarz:
            return Runde.SCHWARZ;
        default:
            return 0;
        }
    }

    private void setRe(int value) {
        RadioGroup reKontraGroup = (RadioGroup)this.getView().findViewById(R.id.radioButtonGroupReKontra);
        RadioGroup keineGroup = (RadioGroup)this.getView().findViewById(R.id.radioButtonGroupKeine);
        switch (value) {
            case Runde.KEINE_120:
                reKontraGroup.check(R.id.reGewinnt);
                keineGroup.check(R.id.gespielt120);
                break;
            case Runde.KEINE_9:
                reKontraGroup.check(R.id.reGewinnt);
                keineGroup.check(R.id.gespieltKeine9);
                break;
            case Runde.KEINE_6:
                reKontraGroup.check(R.id.reGewinnt);
                keineGroup.check(R.id.gespieltKeine6);
                break;
            case Runde.KEINE_3:
                reKontraGroup.check(R.id.reGewinnt);
                keineGroup.check(R.id.gespieltKeine3);
                break;
            case Runde.SCHWARZ:
                reKontraGroup.check(R.id.reGewinnt);
                keineGroup.check(R.id.gespieltSchwarz);
                break;
        }
    }


    public int getKontra() {
        RadioGroup reKontraGroup = (RadioGroup)this.getView().findViewById(R.id.radioButtonGroupReKontra);
        int reKontraId = reKontraGroup.getCheckedRadioButtonId();
        if (reKontraId != R.id.kontraGewinnt) return 0;

        RadioGroup keineGroup = (RadioGroup)this.getView().findViewById(R.id.radioButtonGroupKeine);

        switch (keineGroup.getCheckedRadioButtonId()) {
            case -1:
                return Runde.KEINE_120;
            case R.id.gespielt120:
                return Runde.KEINE_120;
            case R.id.gespieltKeine9:
                return Runde.KEINE_9;
            case R.id.gespieltKeine6:
                return Runde.KEINE_6;
            case R.id.gespieltKeine3:
                return Runde.KEINE_3;
            case R.id.gespieltSchwarz:
                return Runde.SCHWARZ;
            default:
                return 0;
        }
    }

    private void setKontra(int value) {
        RadioGroup reKontraGroup = (RadioGroup)this.getView().findViewById(R.id.radioButtonGroupReKontra);
        RadioGroup keineGroup = (RadioGroup)this.getView().findViewById(R.id.radioButtonGroupKeine);
        switch (value) {
            case Runde.KEINE_120:
                reKontraGroup.check(R.id.kontraGewinnt);
                keineGroup.check(R.id.gespielt120);
                break;
            case Runde.KEINE_9:
                reKontraGroup.check(R.id.kontraGewinnt);
                keineGroup.check(R.id.gespieltKeine9);
                break;
            case Runde.KEINE_6:
                reKontraGroup.check(R.id.kontraGewinnt);
                keineGroup.check(R.id.gespieltKeine6);
                break;
            case Runde.KEINE_3:
                reKontraGroup.check(R.id.kontraGewinnt);
                keineGroup.check(R.id.gespieltKeine3);
                break;
            case Runde.SCHWARZ:
                reKontraGroup.check(R.id.kontraGewinnt);
                keineGroup.check(R.id.gespieltSchwarz);
                break;
        }
    }

    public Solo getSolo() {
        return (Solo) ((Spinner) this.getView().findViewById(R.id.solo)).getSelectedItem();
    }

    private void setSolo(Solo value) {
        Solo newSolo = value != null ? value : Solo.KEIN_SOLO;
        Spinner spinner = (Spinner) this.getView().findViewById(R.id.solo);
        spinner.setSelection(((ArrayAdapter) spinner.getAdapter()).getPosition(newSolo));
    }

    public boolean isGegenDieSau() {
        return ((CheckBox) this.getView().findViewById(R.id.gegenDieSau)).isChecked();
    }

    private void setGegenDieSau(boolean value) {
        ((CheckBox) this.getView().findViewById(R.id.gegenDieSau)).setChecked(value);
    }

    public boolean isArmut() {
        return ((CheckBox) this.getView().findViewById(R.id.armut)).isChecked();
    }

    private void setArmut(boolean value) {
        ((CheckBox) this.getView().findViewById(R.id.armut)).setChecked(value);
    }

    public int getExtrapunkte() {
        return ((ExtrapunkteView) this.getView().findViewById(R.id.extrapunkte)).getValue();
    }

    private void setExtrapunkte(int value) {
        ((ExtrapunkteView) this.getView().findViewById(R.id.extrapunkte)).setValue(value);

    }

    public void reset() {
        setSolo(Solo.KEIN_SOLO);
        setGegenDieSau(false);
        setExtrapunkte(0);
        setArmut(false);
        ((RadioGroup)this.getView().findViewById(R.id.radioButtonGroupReKontra)).clearCheck();
        ((RadioGroup)this.getView().findViewById(R.id.radioButtonGroupKeine)).clearCheck();
    }

    public void fill(Runde r) {
        setSolo(r.getSolo());
        setGegenDieSau(r.isGegenDieSau());
        setExtrapunkte(r.getExtrapunkte());
        setArmut(r.isArmut());
        ((RadioGroup)this.getView().findViewById(R.id.radioButtonGroupReKontra)).clearCheck();
        ((RadioGroup)this.getView().findViewById(R.id.radioButtonGroupKeine)).clearCheck();
        setRe(r.getRe());
        setKontra(r.getKontra());
    }

}