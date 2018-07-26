package de.splitnass.android.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import de.splitnass.android.fragments.SpieltageFragment;
import de.splitnass.data.Spieltag;

public class SpieltageActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SpieltageFragment()).commit();
    }

    public void emailSpieltageAsJson(View view) {
        List<Spieltag> spieltage =
                ((SpieltageFragment)getSupportFragmentManager().getFragments().get(0)).loadSpieltage();

        Intent i = new Intent(Intent.ACTION_SENDTO);
        i.setData(Uri.parse("mailto:")); // only email apps should handle this
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"torsten.welches@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "Spieltage als Json");
        i.putExtra(Intent.EXTRA_TEXT   , Spieltag.listToJson(spieltage));

        if (i.resolveActivity(getPackageManager()) != null) {
            startActivity(i);
        }
    }




}