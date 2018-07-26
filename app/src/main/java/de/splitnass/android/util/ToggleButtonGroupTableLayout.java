package de.splitnass.android.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.RadioButton;
import android.widget.LinearLayout;
import android.widget.TableRow;


public class ToggleButtonGroupTableLayout extends LinearLayout  implements OnClickListener {

    private RadioButton activeRadioButton;
    private AdapterView.OnItemSelectedListener selectionListener;


    public ToggleButtonGroupTableLayout(Context context) {
        super(context);
    }

    public ToggleButtonGroupTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        selectionListener = listener;
    }

    @Override
    public void onClick(View v) {
        final RadioButton rb = (RadioButton) v;
        if ( activeRadioButton != null ) {
            activeRadioButton.setChecked(false);
        }
        rb.setChecked(true);
        setActiveRadioButton(rb);
    }

    @Override
    public void addView(View child, int index,
                        android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        setChildrenOnClickListener(child);
    }

    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, params);
        setChildrenOnClickListener(child);
    }

    private void setChildrenOnClickListener(View child) {
        if ( child instanceof RadioButton ) {
            child.setOnClickListener(this);
        } else if (child instanceof LinearLayout) {
            for (int i = 0; i < ((LinearLayout)child).getChildCount(); i++) {
                if ( ((LinearLayout)child).getChildAt(i) instanceof RadioButton ) {
                    ((LinearLayout)child).getChildAt(i).setOnClickListener(this);
                }
            }
        }
    }

    public int getCheckedRadioButtonId() {
        if ( activeRadioButton != null ) {
            return activeRadioButton.getId();
        }

        return -1;
    }

    private void setActiveRadioButton(RadioButton radioButton) {
        activeRadioButton = radioButton;
        if (selectionListener != null) {
            selectionListener.onItemSelected(null, radioButton, 0, 0);
        }
    }

    public void reset() {
        setActiveRadioButton(null);
        for (int x = 0; x < getChildCount(); x++) {
            View child =  getChildAt(x);
            if ( child instanceof RadioButton ) {
                ((RadioButton) child).setChecked(false);
            }  else if (child instanceof  TableRow) {
                TableRow tr = (TableRow)child;
                for (int i=0; i < tr.getChildCount(); i++) {
                    View v = tr.getChildAt(i);
                    if ( v instanceof RadioButton ) {
                        ((RadioButton) v).setChecked(false);
                    }
                }
            }
        }

    }

    public void clearCheck() {
        if ( activeRadioButton != null ) {
            activeRadioButton.setChecked(false);
        }
        setActiveRadioButton(null);
    }

    public void check(int id) {
        RadioButton newActive = (RadioButton)findViewById(id);
        if (activeRadioButton != null && activeRadioButton != newActive) {
            activeRadioButton.setChecked(false);
        }
        setActiveRadioButton(newActive);
        activeRadioButton.setChecked(true);

    }

}
