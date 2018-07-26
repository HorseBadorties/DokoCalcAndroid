package de.splitnass.android.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import de.splitnass.R;
import de.splitnass.data.Spieler;

import java.util.ArrayList;
import java.util.List;

public class SelectPlayerDialog extends DialogFragment {

    public interface SelectPlayerDialogListener {
        public void onPlayerSelected(SelectPlayerDialog dialog);
        public void onWrongNumberOfPlayerSelected(SelectPlayerDialog dialog);
    }

    public static class SelectPlayerDialogAdapter implements SelectPlayerDialogListener {
        public void onPlayerSelected(SelectPlayerDialog dialog) {}
        public void onWrongNumberOfPlayerSelected(SelectPlayerDialog dialog) {
            new AlertDialog.Builder(dialog.getActivity())
                    .setTitle("Fehler")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage("Ungültige Anzahl Spieler ausgewählt!")
                    .setPositiveButton("OK", null)
                    .create().show();
        }
    }

    private String title;
    private String[] spielerNamen;
    private int minSelectionCount;
    private int maxSelectionCount;
    private List<Spieler> selectedSpieler = new ArrayList<Spieler>();
    private SelectPlayerDialogListener mListener;


    /*
    * Wenn der Rotationssensor während des OurApp-Starts anschlägt,
    * wird seltsamerweise dieser Default-Konstruktor vom System gerufen und
    * es kommt zu einem zweiten - leeren - SelectPlayerDialog!?
    *
    * http://stackoverflow.com/questions/456211/activity-restart-on-rotation-android?lq=1
    * http://stackoverflow.com/questions/1111980/how-to-handle-screen-orientation-change-when-progress-dialog-and-background-thre?rq=1
    *
     */
    public SelectPlayerDialog() {
        super();
    }

    public SelectPlayerDialog(String title, List<Spieler> spieler, int minSelectionCount, int maxSelectionCount,
                              SelectPlayerDialogListener listener)
    {
        this(title, spieler, minSelectionCount, maxSelectionCount);
        mListener = listener;
    }

    public SelectPlayerDialog(String title, List<Spieler> spieler, int minSelectionCount, int maxSelectionCount) {
        this.title = title;
        spielerNamen = new String[spieler.size()];
        for (int i = 0; i < spieler.size(); i++) {
            spielerNamen[i] = spieler.get(i).getName();
        }
        this.minSelectionCount = minSelectionCount;
        this.maxSelectionCount = maxSelectionCount;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setTitle(title)
                .setIcon(R.drawable.ic_launcher)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if ((minSelectionCount != -1 && minSelectionCount > selectedSpieler.size())
                                || (maxSelectionCount != -1 && maxSelectionCount < selectedSpieler.size())) {
                            if (mListener != null) {
                                mListener.onWrongNumberOfPlayerSelected(SelectPlayerDialog.this);
                            }
                        } else {
                            if (mListener != null) {
                                mListener.onPlayerSelected(SelectPlayerDialog.this);
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", null);
        if (minSelectionCount == 1 && maxSelectionCount == 1) {
            builder.setSingleChoiceItems(spielerNamen, -1,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selectedSpieler.clear();
                            selectedSpieler.add(Spieler.byName(spielerNamen[which]));
                        }
                    });
        } else {
            builder.setMultiChoiceItems(spielerNamen, null,
                    new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which,
                                            boolean isChecked) {
                            if (isChecked) {
                                selectedSpieler.add(Spieler.byName(spielerNamen[which]));
                            } else {
                                selectedSpieler.remove(Spieler.byName(spielerNamen[which]));
                            }
                        }
                    });
        }

        return builder.create();
    }

    public List<Spieler> getSelectedSpieler() {
        return selectedSpieler;
    }
}
