package de.splitnass.android.activities;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import de.splitnass.R;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREF_SHOW_BERECHNUNG_PRUEFEN = "PREF_SHOW_BERECHNUNG_PRUEFEN";
    public static final String PREF_KEEP_SCREEN_ON = "PREF_KEEP_SCREEN_ON";
    public static final String PREF_USCHI_SPEAKS = "PREF_USCHI_SPEAKS";
    public static final String PREF_USCHI_TELLS_RESULTS = "PREF_USCHI_TELLS_RESULTS";
    public static final String PREF_USCHI_COMMENTS = "PREF_USCHI_COMMENTS";
    public static final String PREF_USCHI_FLIRTS = "PREF_USCHI_FLIRTS";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

}