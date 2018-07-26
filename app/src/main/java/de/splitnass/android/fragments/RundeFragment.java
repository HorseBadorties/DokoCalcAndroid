package de.splitnass.android.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.splitnass.R;


public class RundeFragment extends Fragment  {



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View result = inflater.inflate(R.layout.fragment_runde, container, false);

        return result;

    }

    public RundenInfoFragment getRundeninfoFragment() {
        return (RundenInfoFragment)getChildFragmentManager().findFragmentById(R.id.rundeninfoFragment);
    }

    public AngesagtFragment getAngesagtFragment() {
        return (AngesagtFragment)getChildFragmentManager().findFragmentById(R.id.angesagtFragment);
    }

    public GespieltFragment getGespieltFragment() {
        return (GespieltFragment)getChildFragmentManager().findFragmentById(R.id.gespieltFragment);
    }

    public RundenblattFragment getRundenblattFragment() {
        return (RundenblattFragment)getChildFragmentManager().findFragmentById(R.id.rundenblattFragment);
    }










}