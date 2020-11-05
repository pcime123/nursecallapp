package com.sscctv.nursecallapp.ui.utils;

import com.sscctv.nursecallapp.data.ExtItem;
import com.sscctv.nursecallapp.data.ExtListItem;

import java.util.Comparator;

public class ExtItemCompare implements Comparator<ExtItem> {

    private static final String TAG = ExtItemCompare.class.getSimpleName();

    int ret = 0;

    @Override
    public int compare(ExtItem e1, ExtItem t1) {

        if (e1.getNum().compareTo(t1.getNum()) == 0) {
            if (e1.getName().compareTo(t1.getName()) < 0) {
                ret = -1;
            } else if (e1.getName().compareTo(t1.getName()) == 0) {
                ret = 0;
            } else if (e1.getName().compareTo(t1.getName()) > 0) {
                ret = 1;
            }
        } else if(e1.getNum().compareTo(t1.getNum()) < 0) {
            ret = -1;
        } else if(e1.getNum().compareTo(t1.getNum()) > 0) {
            ret = 1;
        }

        return ret;
    }
}
