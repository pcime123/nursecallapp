package com.sscctv.nursecallapp.ui.utils;

import android.content.ClipData;
import android.content.ClipDescription;
import android.view.View;

public class LongClickListener implements View.OnLongClickListener {
    @Override
    public boolean onLongClick(View view) {
        ClipData.Item item = new ClipData.Item((CharSequence) view.getTag());

        String[] mimeTypes = {
                ClipDescription.MIMETYPE_TEXT_PLAIN
        };
        ClipData data = new ClipData(view.getTag().toString(), mimeTypes, item);
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

        view.startDrag(data, shadowBuilder, view,0);
        view.setVisibility(View.VISIBLE);
        return view.performClick();

    }
}
