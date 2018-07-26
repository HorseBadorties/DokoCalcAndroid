package de.splitnass.android.fragments;

import android.support.v4.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import de.splitnass.R;
import de.splitnass.android.util.RundenblattItemView;
import de.splitnass.data.Runde;
import de.splitnass.data.Spieltag;


public class RundenblattFragment extends Fragment {

    private Spieltag spieltag;
    private Runde runde;
    private ListView listView;
    private SpieltagAdapter spieltagAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View result = inflater.inflate(R.layout.fragment_rundenblatt, container, false);
        listView = (ListView)result.findViewById(R.id.rundenblattListe);
        listView.setDividerHeight(0);

        return result;

    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        ((ListView) getView().findViewById(R.id.rundenblattListe)).setOnItemClickListener(listener);
    }

    public void update(Spieltag spieltag) {
        this.spieltag = spieltag;

        //Header
        LinearLayout header = (LinearLayout)getView().findViewById(R.id.rundenblattHeader);
        header.removeAllViews();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        header.addView(createHeader(), layoutParams);

        spieltagAdapter = new SpieltagAdapter(this.spieltag);
        listView.setAdapter(spieltagAdapter);
        update(RundenblattFragment.this.spieltag.getAktuelleRunde(), false);
        getView().postDelayed(new Runnable() {
            @Override
            public void run() {
                adjustSelection(RundenblattFragment.this.spieltag.getAktuelleRunde());
            }
        }, 100);

    }

    public void update(Runde r, boolean scrollToRunde) {
        runde = r;
        spieltagAdapter.notifyDataSetChanged();
        if (scrollToRunde) {
            adjustSelection(r);
        }
    }

    private void adjustSelection(Runde r) {
        int visibleChildCount = (listView.getLastVisiblePosition() - listView.getFirstVisiblePosition()) + 1;
        int positionAktuelleRunde = r.getId()-1;
        int targetSelection = positionAktuelleRunde - visibleChildCount/2;
        listView.setSelection(targetSelection > 0 ? targetSelection : 0);
    }

    private View createHeader() {
        RundenblattItemView result = new RundenblattItemView(getActivity(), spieltag);
        result.fillAsHeader();
        return result;
    }


    private class SpieltagAdapter extends BaseAdapter {

        private Spieltag spieltag;

        private SpieltagAdapter(Spieltag spieltag) {
            this.spieltag = spieltag;
        }

        @Override
        public int getCount() {
            return spieltag != null ? spieltag.getRundenAnzahl() : 0;
        }

        @Override
        public Runde getItem(int position) {
            return spieltag.getRunden().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if ( !(convertView instanceof RundenblattItemView) ||
                    ((RundenblattItemView)convertView).getSpielerCount() != spieltag.getSpieler().size()) {
                convertView = new RundenblattItemView(parent.getContext(), spieltag);
            }
            //fill data
            Runde r = getItem(position);
            ((RundenblattItemView)convertView).fill(r);
            if (r == runde) {
                convertView.setBackgroundColor(getResources().getColor(R.color.RundenblattSelectionColor));
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }
            return convertView;
        }
    }

}