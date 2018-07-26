package de.splitnass.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import de.splitnass.android.activities.SettingsActivity;
import de.splitnass.util.ErgebnisToUschiText;

public class Uschi extends ErgebnisToUschiText implements SharedPreferences.OnSharedPreferenceChangeListener {

    private TextToSpeech textToSpeech;
    private Context mContext;
    private Timer timer;
    private Random random = new Random();
    private transient boolean isActive = true;
    private SharedPreferences prefs;

    private static final int AVERAGE_DELAY_IN_SECONDS = 600;

    public Uschi(Context context) {
        mContext = context;
        textToSpeech = new TextToSpeech(mContext,
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            int result = textToSpeech.setLanguage(Locale.GERMAN);
                            if (result == TextToSpeech.LANG_MISSING_DATA ||
                                    result == TextToSpeech.LANG_NOT_SUPPORTED)
                            {
                                textToSpeech = null;
                            }
                        } else {
                            textToSpeech = null;
                        }
                    }
                }  // TextToSpeech.OnInitListener
        );
        timer = new Timer();
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.registerOnSharedPreferenceChangeListener(this);
        if (PreferenceManager.getDefaultSharedPreferences(mContext)
                .getBoolean(SettingsActivity.PREF_USCHI_COMMENTS, false))
        {
            resumeCommenting();
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingsActivity.PREF_USCHI_COMMENTS)) {
            if (sharedPreferences.getBoolean(SettingsActivity.PREF_USCHI_COMMENTS, false)) {
                resumeCommenting();
            } else {
                stopCommenting();
            }
        }
    }

    public void stopCommenting() {
        Log.d("Uschi", "stopCommenting()");
        isActive = false;
        timer.cancel();
    }

    public void resumeCommenting() {
        if (!isActive && prefs.getBoolean(SettingsActivity.PREF_USCHI_COMMENTS, false))
        {
            Log.d("Uschi", "resumeCommenting()");
            timer = new Timer();
            isActive = true;
            scheduleTimer(random.nextInt(AVERAGE_DELAY_IN_SECONDS));
        }
    }

    private void scheduleTimer(int delayInSeconds) {
        if (!isActive) return;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                new Handler(mContext.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        randomKommentar();
                    }
                });
            }
        }, delayInSeconds * 1000); //Millis
    }

    public void shutdown() {
        stopCommenting();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    public void sprichResultat() {
        String uschisText = null;
        String pref = prefs.getString(SettingsActivity.PREF_USCHI_TELLS_RESULTS, "SHUT_UP") ;
        if ("EXPLAIN_RESULT".equals(pref)) {
            uschisText = getErgebnisAsText();
        } else if ("TELL_RESULT".equals(pref)) {
            uschisText = getPunkteAsText();
        }
        if (uschisText != null) {
            speak(uschisText, false);
        }

    }

    public void kommentiere(final String kommentar) {
        if (!isActive) return;
        speak(kommentar, true);
    }

    //ensure we speak on the main thread
    private void speak(final String text, final boolean waitIfSpeaking) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            doSpeak(text, waitIfSpeaking);
        } else {
            new Handler(mContext.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    doSpeak(text, waitIfSpeaking);
                }
            });
        }
    }
    private void doSpeak(final String text, final boolean waitIfSpeaking) {
        if (waitIfSpeaking && textToSpeech.isSpeaking()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doSpeak(text, waitIfSpeaking);
                }
            }, 5000); //wait five seconds
        } else {
            if (prefs.getBoolean(SettingsActivity.PREF_USCHI_SPEAKS, false)) {
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    private void randomKommentar() {
        if (!isActive) return;
        kommentiere(kommentare[random.nextInt(kommentare.length)]);
        int newDelay = random.nextInt(AVERAGE_DELAY_IN_SECONDS);
        Log.d("Uschi", String.format("Nächster Kommentar in %d Sekunden", newDelay));
        scheduleTimer(newDelay);
    }

    private String[] kommentare = new String[] {
        "Meine Brüste sind heute besonders empfindlich.",
        "Levent,,, das kann es doch nicht sein.",
        "Ralf,,, findest Du mich hübsch?",
        "Mein Name ist Uschi. Uschi wie Muschi, nur ohne M.",
        "Mir ist langweilig.",
        "Ich bin sehr erregt.",
        "Kölle alaaf.",
        "Was für ein wunderschöner Abend.",
        "Jungs,,,, ich hab Euch sehr lieb.",
        "Ganz schön langweilig.",
        "Dabei sein ist alles.",
        "Hallo? ,,,, Hallo?",
        "Nur die Liebe zählt.",
        "Eier,,, wir brauchen Eier.",
        "Seid Ihr noch da?",
        "Oh, Oh oh,,,,, oh.",
        "Gido,,, Du bist super.",
        "Spielt Ihr noch lange?",
        "Levent,,,,,, ich vermisse Dich!"
    };


}
