package com.sscctv.nursecallapp.ui.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.sscctv.nursecallapp.R;

public class LayoutBed extends LinearLayout {
    public LayoutBed(Context context) {
        super(context);

        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        inflater.inflate(R.layout.item_bed_icon, this, true);
    }
}
