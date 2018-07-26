package de.splitnass.android.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import de.splitnass.data.*;

import de.splitnass.android.OurApp;
import de.splitnass.R;
import de.splitnass.android.db.DatabaseHelper;
import de.splitnass.android.db.SpieltagPersistor;
import de.splitnass.android.dialogs.SelectPlayerDialog;
import de.splitnass.android.fragments.*;
import de.splitnass.util.ErgebnisToText;
import de.splitnass.android.util.Uschi;




public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, SpieltagListener {

    public static final String CURRENT_RUNDE_ID = "currentRundeID";

    private ViewPager viewPager;
    //private RundeFragment rundeFragment;
    //private RundenblattFragment rundenblattFragment;

    private OurApp app;
    //Aktuelle angezeigte/selektierte Runde
    private Runde runde;

    private ProgressDialog progressDialog;
    private AsyncTask loaderTask;
    private AsyncTask saverTask;
    private Uschi uschi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (OurApp)getApplication();
        setContentView(R.layout.main);

        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        if (screenSize != Configuration.SCREENLAYOUT_SIZE_LARGE
                && screenSize != Configuration.SCREENLAYOUT_SIZE_XLARGE)
        {

          //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        }
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                invalidateOptionsMenu();
                if (position == 0) {
                    //getSupportActionBar().show();
                } else {
                    //getSupportActionBar().hide();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        uschi = new Uschi(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (app.getSpieltag() == null) {
            progressDialog = ProgressDialog.show(this, "", "Loading Spieltag-Data...");
            loaderTask = new AsyncTask<Void, Void, Spieltag>() {
                protected Spieltag doInBackground(Void... args) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {}
                    return SpieltagPersistor.loadAktivenSpieltag(DatabaseHelper.getInstance(getApplicationContext()));
                }
                protected void onPostExecute(Spieltag result) {
                    app.setSpieltag(result);
                    if (result != null) {
                        result.addListener(MainActivity.this);
                    }
                    if (!isFinishing() && !isCancelled()) {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                        doResume();
                        loaderTask = null;
                    }
                }
            }.execute();

        } else {
            app.getSpieltag().addListener(MainActivity.this);
            doResume();
        }

    }

    private void doResume() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        RundenblattFragment rundenblattFragment = getRundenblattFragment();
        if (rundenblattFragment != null) {
            rundenblattFragment.setOnItemClickListener(this);
        }

        findViewById(R.id.main_content).setKeepScreenOn(prefs.getBoolean(SettingsActivity.PREF_KEEP_SCREEN_ON, true));

        if (app.getSpieltag() != null) {
            fillRundenblattFragment();
            //resetAnsagenUndGespielt(null);
            int savedRundenID = getPreferences(Context.MODE_PRIVATE).getInt(CURRENT_RUNDE_ID, -1);
            if (savedRundenID != -1 && savedRundenID <= app.getSpieltag().getRundenAnzahl()) {
                zeige(app.getSpieltag().getRunden().get(savedRundenID-1), true);
            }  else {
                zeige(app.getSpieltag().getAktuelleRunde(), true);
            }

        } else {
            //kein Spieltag vorhanden - wir starten automatisch einen neuen
            /*
             * Dirty hack for:
             * http://stackoverflow.com/questions/456211/activity-restart-on-rotation-android?lq=1
             * http://stackoverflow.com/questions/1111980/how-to-handle-screen-orientation-change-when-progress-dialog-and-background-thre?rq=1
             */
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    try {
                        neuerSpieltag();
                    } catch (IllegalStateException ignore) {}
                }
            }.execute((Void)null);
        }
        uschi.resumeCommenting();

    }

    @Override
    protected void onPause() {
        super.onPause();
        uschi.stopCommenting();
        if (loaderTask != null) {
            loaderTask.cancel(false);
            loaderTask = null;
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        if (runde != null) {
            if (app.getSpieltag() != null && runde == app.getSpieltag().getAktuelleRunde()) {
                setRundenValues(runde);
            }
            saveCurrentRundeToPrefs();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCurrentRundeToPrefs() {
        if (runde != null) {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(CURRENT_RUNDE_ID, runde.getId());
            editor.commit();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveSpieltag();
    }

    @Override
    public void onDestroy() {
        app.getSpieltag().removeListener(this);
        uschi.shutdown();
        super.onDestroy();
    }

    private void saveSpieltag() {
        if (app.getSpieltag() != null) {
            SpieltagPersistor.save(DatabaseHelper.getInstance(getApplicationContext()), app.getSpieltag());
        }
    }

    private void saveSpieltagInBackground() {
        if (app.getSpieltag() != null) {
            saverTask = new AsyncTask<Void, Void, Void>() {
                protected Void doInBackground(Void... args) {
                    SpieltagPersistor.save(DatabaseHelper.getInstance(getApplicationContext()),
                            app.getSpieltag());
                    return null;
                }
                protected void onPostExecute(Spieltag result) {
                    saverTask = null;
                }
            }.execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.mnuHerzGehtRum).setEnabled(runde != null && runde.isLaufend() && !runde.isHerzGehtRum());
        //Blättern-Buttons nur auf der Hauptseite
        boolean isHautseite = viewPager.getCurrentItem() == 0;
        MenuItem mnuBack = menu.findItem(R.id.mnuBack);
        MenuItem mnuForward = menu.findItem(R.id.mnuForward);
        if (isHautseite) {
            mnuBack.setEnabled(runde != null && app.getSpieltag().getVorherigeRunde(runde) != null);
            mnuForward.setEnabled(runde != null && app.getSpieltag().getNaechsteRunde(runde) != null);
         } else {
            mnuBack.setVisible(false);
            mnuForward.setVisible(false);
        }

        menu.findItem(R.id.mnuRunde).setEnabled(app.getSpieltag() != null);
        MenuItem mnuRundeAbrechnen = menu.findItem(R.id.mnuRundeAbrechnen);
        if (isHautseite) {
            mnuRundeAbrechnen.setEnabled(runde != null && runde.isGestartet() && !runde.isDummy());
            if (runde != null) {
                mnuRundeAbrechnen.setTitle(String.format("Runde %d abrechnen", runde.getId()));
                if (runde.isBeendet()) {
                    mnuRundeAbrechnen.setTitle(String.format("Runde %d korrigieren", runde.getId()));
                }
            }
        } else {
            mnuRundeAbrechnen.setVisible(false);
        }
        menu.findItem(R.id.mnuReset).setEnabled(runde != null && runde.isGestartet());
        menu.findItem(R.id.mnuSetzeErgebnis).setEnabled(runde != null && runde.isBeendet());
        menu.findItem(R.id.mnuSetzeBoecke).setEnabled(runde != null);
        MenuItem mnuBerechnung = menu.findItem(R.id.mnuBerechnungPruefen);
        boolean showBerechnungPruefen = PreferenceManager.getDefaultSharedPreferences(this).
                getBoolean(SettingsActivity.PREF_SHOW_BERECHNUNG_PRUEFEN, false);
        mnuBerechnung.setShowAsAction(showBerechnungPruefen ?
                MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT : MenuItem.SHOW_AS_ACTION_NEVER);
        mnuBerechnung.setEnabled(runde != null && runde.isGestartet());
        menu.findItem(R.id.mnuSpieltaginfo).setEnabled(app.getSpieltag() != null);
        menu.findItem(R.id.mnuPunktestandEinesSpielersAendern).setEnabled(app.getSpieltag() != null);
        menu.findItem(R.id.mnuSetzeRundenanzahl).setEnabled(app.getSpieltag() != null);
        menu.findItem(R.id.mnuSpielerSteigtEin).setEnabled(app.getSpieltag() != null);
        menu.findItem(R.id.mnuSpielerSteigtAus).setEnabled(app.getSpieltag() != null);
        menu.findItem(R.id.mnuGeheZu).setEnabled(app.getSpieltag() != null);
        return true;
    }


    public void resetAnsagenUndGespielt(MenuItem item) {
        if (runde == app.getSpieltag().getAktuelleRunde()) {
            runde.reset();
        }
        getRundeFragment().getAngesagtFragment().reset();
        getRundeFragment().getGespieltFragment().reset();
        updateRundenblattFragment(false);
    }

    public void setzeErgebnis(MenuItem item) {
        final NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(0);
        picker.setMaxValue(100);
        picker.setValue(runde.getErgebnis());
        new AlertDialog.Builder(this)
                .setTitle("Neues Ergebnis:")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(picker)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        picker.clearFocus();
                        runde.setErgebnis(picker.getValue());
                        zeige(runde, false);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create().show();
    }

    public void setzeBoecke(MenuItem item) {
        final NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(0);
        picker.setMaxValue(runde.isBeendet() ? 3 : 2);
        int value = runde.isGestartet() ? runde.getBoeckeBeiBeginn() : runde.getBoecke();
        picker.setValue(value);
        new AlertDialog.Builder(this)
                .setTitle("Neue Anzahl Böcke:")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(picker)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        picker.clearFocus();
                        if (runde.isGestartet()) {
                            runde.setBoeckeBeiBeginn(picker.getValue());
                        } else {
                            runde.setBoecke(picker.getValue());
                        }
                        zeige(runde, false);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create().show();
    }

    public void startSpielertaginfo(MenuItem item) {
        startActivity(new Intent(this, SpieltaginfoActivity.class));
    }

    public void spielerSteigtEin(MenuItem item) {
        java.util.List<Spieler> auswahl = new java.util.ArrayList<Spieler>(Spieler.getAll());
        auswahl.removeAll(app.getSpieltag().getAktiveSpieler());
        SelectPlayerDialog selectPlayer = new SelectPlayerDialog("Wer steigt ein?", auswahl, 1, 1,
                new SelectPlayerDialog.SelectPlayerDialogAdapter() {
                    @Override
                    public void onPlayerSelected(SelectPlayerDialog dialog) {
                        Spieler neuerSpieler = dialog.getSelectedSpieler().get(0);
                        app.getSpieltag().spielerSteigtEin(neuerSpieler);
                        fillRundenblattFragment();
                        zeige(app.getSpieltag().getAktuelleRunde(), true);
                        toast(String.format("%s ist mit %d Punkten eingestiegen", neuerSpieler.getName(),
                                app.getSpieltag().getPunktestand(app.getSpieltag().getAktuelleRunde(), neuerSpieler)), true);
                    }
                });
        selectPlayer.show(getFragmentManager(), null);
    }

    public void spielerSteigtAus(MenuItem item) {
        if (app.getSpieltag().getAktiveSpieler().size() == 4) {
            showDialog("Bei nur vier aktiven Spielern kann niemand mehr aussteigen!", true);
            return;
        }
        SelectPlayerDialog selectPlayer = new SelectPlayerDialog("Wer steigt aus?",
                app.getSpieltag().getAktiveSpieler(), 1, 1,
                new SelectPlayerDialog.SelectPlayerDialogAdapter() {
                    @Override
                    public void onPlayerSelected(SelectPlayerDialog dialog) {
                        Spieler ausgestiegenerSpieler = dialog.getSelectedSpieler().get(0);
                        app.getSpieltag().spielerSteigtAus(ausgestiegenerSpieler);
                        zeige(app.getSpieltag().getAktuelleRunde(), false);
                        toast(ausgestiegenerSpieler.getName() + " ist ausgestiegen", true);
                    }
                });
        selectPlayer.show(getFragmentManager(), null);
    }

    public void punktestandEinesSpielersAendern(MenuItem item) {
        SelectPlayerDialog selectPlayer = new SelectPlayerDialog("Der Punktestand welches Spielers soll geändert werden?",
                app.getSpieltag().getSpieler(), 1, 1,
                new SelectPlayerDialog.SelectPlayerDialogAdapter() {
                    @Override
                    public void onPlayerSelected(SelectPlayerDialog dialog) {
                        final Spieler zuAendernderSpieler = dialog.getSelectedSpieler().get(0);
                        final int aktuellerPunktestand = app.getSpieltag().getPunktestand(
                                app.getSpieltag().getAktuelleRunde(), zuAendernderSpieler);
                        final NumberPicker picker = new NumberPicker(MainActivity.this);
                        picker.setMinValue(0);
                        picker.setMaxValue(10000);
                        picker.setValue(aktuellerPunktestand);
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Neuer Punktestand: ")
                                .setIcon(R.drawable.ic_launcher)
                                .setView(picker)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        picker.clearFocus();
                                        int neuerPunktestand = picker.getValue();
                                        if (neuerPunktestand != aktuellerPunktestand) {
                                            app.getSpieltag().setPunktestand(zuAendernderSpieler, neuerPunktestand);
                                            zeige(app.getSpieltag().getAktuelleRunde(), true);
                                            toast(String.format("Neuer Punktestand für %s: %d",
                                                    zuAendernderSpieler.getName(), neuerPunktestand), true);
                                        }
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .create().show();

                    }
                });
        selectPlayer.show(getFragmentManager(), null);
    }

    public void setzeRundenanzahl(MenuItem item) {
        final NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(app.getSpieltag().getAktuelleRunde().getId());
        picker.setMaxValue(150);
        picker.setValue(app.getSpieltag().getRundenAnzahl());
        new AlertDialog.Builder(this)
                .setTitle("Neue Rundenanzahl:")
                .setIcon(R.drawable.ic_launcher)
                .setView(picker)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        picker.clearFocus();
                        int neueRundenanzahl = picker.getValue();
                        if (neueRundenanzahl < app.getSpieltag().getAktuelleRunde().getId()) {
                            showDialog(String.format("Runde %d wurde bereits gespielt, die Rundenanzahl wurde nicht verändert!"
                                    , neueRundenanzahl), true);
                        } else {
                            app.getSpieltag().setGesamtRunden(picker.getValue());
                            zeige(app.getSpieltag().getAktuelleRunde(), false);
                            toast("Neue Rundenanzahl: " + app.getSpieltag().getRundenAnzahl(), true);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create().show();
    }

    public void geheZu(MenuItem item) {
        final NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(1);
        picker.setMaxValue(app.getSpieltag().getRundenAnzahl());
        picker.setValue(app.getSpieltag().getAktuelleRunde().getId());
        new AlertDialog.Builder(this)
                .setTitle("Nummer der gewünschten Runde:")
                .setIcon(R.drawable.ic_launcher)
                .setView(picker)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        picker.clearFocus();
                        zeige(app.getSpieltag().getRunden().get(picker.getValue() - 1), true);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create().show();
    }

    public void loadSpieltagByID(MenuItem item) {
        List<Long> ids = SpieltagPersistor.getSpieltagIDs(DatabaseHelper.getInstance(getApplicationContext()));
        ArrayAdapter<Long> values = new ArrayAdapter<Long>(
                MainActivity.this.getApplicationContext(), android.R.layout.simple_spinner_dropdown_item,
                ids);
        //values.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        final Spinner spinner = new Spinner(this);
        spinner.setAdapter(values);
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Spieltag-ID")
                .setIcon(R.drawable.ic_launcher)
                .setView(spinner)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        int spieltagID = Integer.valueOf(spinner.getSelectedItem().toString());
                        Spieltag newSpieltag = SpieltagPersistor.loadById(
                                DatabaseHelper.getInstance(getApplicationContext()),
                                spieltagID);
                        if (newSpieltag != null) {
                            if (app.getSpieltag() != null) {
                                app.getSpieltag().removeListener(MainActivity.this);
                            }
                            app.setSpieltag(newSpieltag);
                            newSpieltag.addListener(MainActivity.this);
                            runde = app.getSpieltag().getAktuelleRunde();
                            saveCurrentRundeToPrefs();
                            fillRundenblattFragment();
                            zeige(runde, true);
                        } else {
                            toast(String.format("Spieltag mit der ID %d existiert nicht" , spieltagID), true);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create().show();
    }

    public void neuerSpieltag(MenuItem item) {
        neuerSpieltag();
    }

    private void neuerSpieltag() {
        SelectPlayerDialog selectPlayer =
            new SelectPlayerDialog("4-6 Spieler in Sitzreihenfolge auswählen; Erster ist Geber!",
                Spieler.getAll(), 4, 6,
                new SelectPlayerDialog.SelectPlayerDialogAdapter() {
                    @Override
                    public void onPlayerSelected(final SelectPlayerDialog selectPlayerDialog) {
                        final NumberPicker picker = new NumberPicker(MainActivity.this);
                        picker.setMinValue(1);
                        picker.setMaxValue(150);
                        picker.setValue(42);
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Wieviele Runden sollen gespielt werden?")
                                .setIcon(R.drawable.ic_launcher)
                                .setView(picker)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        Spieltag newSpieltag = new Spieltag();
                                        picker.clearFocus();
                                        newSpieltag.setGesamtRunden(picker.getValue());
                                        newSpieltag.start(selectPlayerDialog.getSelectedSpieler(), selectPlayerDialog.getSelectedSpieler().get(0));
                                        app.setSpieltag(newSpieltag);
                                        newSpieltag.addListener(MainActivity.this);
                                        runde = app.getSpieltag().getAktuelleRunde();
                                        saveCurrentRundeToPrefs();
                                        fillRundenblattFragment();
                                        zeige(runde, true);
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .create().show();
                    }
                });
        selectPlayer.show(getFragmentManager(), null);
    }

    public void berechnungPruefen(MenuItem item) {
        ErgebnisToText e2t = new ErgebnisToText();
        Runde dummy = new Runde(app.getSpieltag(), -1);
        dummy.setBoecke(runde.getBoeckeBeiBeginn());
        dummy.addBerechnungsListener(e2t);
        ergebnisBerechnen(dummy);
        dummy.removeBerechnungsListener(e2t);
        showDialog(e2t.getErgebnisAsText(), false);
    }

    public void herzGehtRum(MenuItem item) {
        runde.setHerzGehtRum();
        invalidateOptionsMenu();
        updateRundenblattFragment(true);
        getRundeFragment().getRundeninfoFragment().fill(runde);
        toast(app.getSpieltag().getAktiveSpieler().size() + " Böcke wurden notiert!", true);
    }

    public void openSettings(MenuItem menuItem) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void forward(MenuItem item) {
        zeige(app.getSpieltag().getNaechsteRunde(runde), true);
    }

    public void backward(MenuItem item) {
        zeige(app.getSpieltag().getVorherigeRunde(runde), true);
    }

    public void rundeAbrechnen(MenuItem item) {

        if (runde.isLaufend()) {
            rundeAbrechnen();
        } else {
            //das Ergbnis einer bereits gespielten Runde wird korrigiert
            new AlertDialog.Builder(this)
                    .setMessage("Runde wurde bereits abgerechnet - Abrechnung korrigieren?")
                    .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                            runde.reset();
                            rundeAbrechnen();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create().show();
        }
    }

    public void sendMail(MenuItem item) {

        Intent i = new Intent(Intent.ACTION_SENDTO);
        i.setData(Uri.parse("mailto:")); // only email apps should handle this
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"torsten.welches@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "Spieltag Snapshot");
        i.putExtra(Intent.EXTRA_TEXT   , Spieltag.toJson(app.getSpieltag()));

        if (i.resolveActivity(getPackageManager()) != null) {
            startActivity(i);
        }

    }

    public void gesamtStatistik(MenuItem item) {
        startActivity(new Intent(this, SpieltageActivity.class));
    }

    private void rundeAbrechnen() {
        final Runde r = runde;

        ergebnisBerechnen(r);
        if (r.getRe() == 0 && r.getKontra() == 0) {
            new AlertDialog.Builder(this)
                .setMessage("Kein Ergebnis ausgewählt - Gespaltener Arsch?")
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        r.setGespaltenerArsch();
                        rundeAbrechnenUndNeu();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create().show();
            return;
        }
        if (r.getErgebnis() == 0) {
            r.setGespaltenerArsch();
            rundeAbrechnenUndNeu();
            showDialog(r.getErgebnisString(), false);
        } else {
            //Sieger ermitteln
            int anzahlSieger = 2;
            if (r.isSolo()) {
                anzahlSieger = r.isSoloVerloren() ? 3 : 1;
            }

            String titel = String.format("Ergebniss: %d %s - bitte %d Sieger wählen!",
                    r.getErgebnis(), r.getErgebnis() != 1 ? "Punkte" : "Punkt", anzahlSieger);
            SelectPlayerDialog selectPlayer = new SelectPlayerDialog(titel, r.getSpieler(), anzahlSieger, anzahlSieger,
                    new SelectPlayerDialog.SelectPlayerDialogAdapter() {
                        @Override
                        public void onPlayerSelected(SelectPlayerDialog dialog) {
                            runde.setGewinner(dialog.getSelectedSpieler());
                            if (runde == app.getSpieltag().getAktuelleRunde()) {
                                rundeAbrechnenUndNeu();
                            } else {
                                zeige(runde, true);
                            }
                        }
                    });
            selectPlayer.show(getFragmentManager(), null);
        }
    }

    private void ergebnisBerechnen(Runde r) {
        setRundenValues(r);
        uschi.clearErgebnisAsText();
        r.addBerechnungsListener(uschi);
        r.berechneErgebnis();
        r.removeBerechnungsListener(uschi);
        uschi.sprichResultat();
    }

    private void setRundenValues(Runde r) {
        AngesagtFragment ansagen = getRundeFragment().getAngesagtFragment();
        //Ansagen
        r.setReAngesagt(ansagen.getReAngesagt());
        r.setReVonVorneHerein(ansagen.isReVonVorneherein() ? 1 : 0);
        r.setKontraAngesagt(ansagen.getKontraAngesagt());
        r.setKontraVonVorneHerein(ansagen.isKontraVonVorneherein()  ? 1 : 0);
        //Gespielt
        GespieltFragment gespielt = getRundeFragment().getGespieltFragment();
        r.setRe(gespielt.getRe());
        r.setKontra(gespielt.getKontra());
        r.setSolo(gespielt.getSolo());
        r.setArmut(gespielt.isArmut());
        r.setGegenDieSau(gespielt.isGegenDieSau());
        r.setExtrapunkte(gespielt.getExtrapunkte());

    }

    private void showDialog(String message, boolean isError) {
        new AlertDialog.Builder(this)
                .setTitle(isError ? "Fehler" : "Info")
                .setIcon(isError ? android.R.drawable.ic_dialog_alert : android.R.drawable.ic_dialog_info)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .create().show();
    }

    private void toast(String message, boolean showLong) {
        Toast.makeText(this, message, showLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    private void rundeAbrechnenUndNeu() {
        app.getSpieltag().rundeAbrechnenUndNeu();
        //resetAnsagenUndGespielt(null);
        toast(String.format("Runde %d wurde abgerechnet", runde.getId()), false);
        zeige(app.getSpieltag().getAktuelleRunde(), true);
        saveSpieltagInBackground();
    }

    private void zeige(Runde r, boolean scrollToRunde) {
        if (r == null) return;
        if (runde == app.getSpieltag().getAktuelleRunde()) {
            setRundenValues(runde);
        }
        runde = r;
        RundeFragment rundeFragment = getRundeFragment();
        rundeFragment.getRundeninfoFragment().fill(r);
        updateRundenblattFragment(scrollToRunde);
        rundeFragment.getAngesagtFragment().fill(r);
        rundeFragment.getGespieltFragment().fill(r);
        invalidateOptionsMenu();

    }

    private RundenblattFragment getRundenblattFragment() {
        return (RundenblattFragment)getFragment(RundenblattFragment.class);
    }

    private RundeFragment getRundeFragment() {
        return (RundeFragment)getFragment(RundeFragment.class);
    }

    private Fragment getFragment(Class c) {
        Fragment result = null;
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f != null && c.isAssignableFrom(f.getClass())) {
                result = f;
                break;
            }
        }
        if (result ==  null) {
            System.out.println("*****");
        }
        return result;
    }

    private void fillRundenblattFragment() {
        RundenblattFragment rundenblatt = getRundenblattFragment();
        if (rundenblatt != null) {
            rundenblatt.update(app.getSpieltag());
        }
    }

    private void updateRundenblattFragment(boolean scrollToRunde) {
        RundenblattFragment rundenblatt = getRundenblattFragment();
        if (rundenblatt != null) {
            rundenblatt.update(runde, scrollToRunde);
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        zeige(app.getSpieltag().getRunden().get(position), false);
    }

    @Override
    public void rundeUpdated(Runde r) {
        Log.d("MainActivity", "rundeUpdated");
        if (r == runde) {
            getRundeFragment().getRundeninfoFragment().fill(r);
        }
        updateRundenblattFragment(false);
    }

    @Override
    public void spieltagUpdated() {

    }

    @Override
    public void aktuelleRundeChanged() {

    }

    @Override
    public void spielerSteigtEin() {

    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            //super.onBackPressed();
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    public class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new RundeFragment();
                case 1:
                    return new RundenblattFragment();

            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

    }
}
