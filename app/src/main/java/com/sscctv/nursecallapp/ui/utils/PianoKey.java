package com.sscctv.nursecallapp.ui.utils;

import android.graphics.RectF;

public class PianoKey {

    public int sound;
    public RectF rect;
    public boolean down;

    public PianoKey(RectF rect, int sound) {
        this.sound = sound;
        this.rect = rect;
    }
}
