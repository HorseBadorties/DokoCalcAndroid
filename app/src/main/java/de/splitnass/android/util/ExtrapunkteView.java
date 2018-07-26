package de.splitnass.android.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import de.splitnass.R;

public class ExtrapunkteView extends FrameLayout {

    private int value;
    private int minValue = -5;
    private int maxValue = 5;

    public ExtrapunkteView(Context context, int minValue, int maxValue) {
        this(context, null, 0);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public ExtrapunkteView(Context context) {
        this(context, -5, 5);
    }

    public ExtrapunkteView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public ExtrapunkteView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        doAddChildView();
    }

    private void doAddChildView() {
        addView(View.inflate(this.getContext(), R.layout.extrapunkte, null));
        ((Button)findViewById(R.id.btnMinus)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                minus();
            }
        });
        ((Button)findViewById(R.id.btnPlus)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                plus();
            }
        });
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        if (value > maxValue || value < minValue) {
            return;
        }
        Button btnMinus = (Button)findViewById(R.id.btnMinus);
        Button btnPlus = (Button)findViewById(R.id.btnPlus);
        TextView txtPunkte = (TextView)findViewById(R.id.txtPunkte);
        btnMinus.setEnabled(value > minValue);
        btnPlus.setEnabled(value < maxValue);
        txtPunkte.setText(String.valueOf(value));
        this.value = value;
    }

    public void plus() {
        setValue(value+1);
    }

    public void minus() {
        setValue(value-1);
    }


}
