package de.splitnass.android.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import de.splitnass.android.fragments.SpieltagInfoFragment;

public class SpieltaginfoActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SpieltagInfoFragment()).commit();
    }




}