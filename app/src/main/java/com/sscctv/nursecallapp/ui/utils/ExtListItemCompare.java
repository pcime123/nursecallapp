package com.sscctv.nursecallapp.ui.utils;

import com.sscctv.nursecallapp.data.ExtListItem;

import java.util.Comparator;

public class ExtListItemCompare implements Comparator<ExtListItem> {

    private static final String TAG = ExtListItemCompare.class.getSimpleName();

    int ret = 0;

    @Override
    public int compare(ExtListItem e1, ExtListItem t1) {

        if (e1.getRoom().compareTo(t1.getRoom()) == 0) {
            if (e1.getBed().compareTo(t1.getBed()) < 0) {
                ret = -1;
            } else if (e1.getBed().compareTo(t1.getBed()) == 0) {
                ret = 0;
            } else if (e1.getBed().compareTo(t1.getBed()) > 0) {
                ret = 1;
            }
        } else if(e1.getRoom().compareTo(t1.getRoom()) < 0) {
            ret = -1;
        } else if(e1.getRoom().compareTo(t1.getRoom()) > 0) {
            ret = 1;
        }

        return ret;
    }
}
