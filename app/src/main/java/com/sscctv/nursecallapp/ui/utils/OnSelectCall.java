package com.sscctv.nursecallapp.ui.utils;

public interface OnSelectCall {
    void roomSelect(int position);
    void roomAllClear();
    void refresh();
    void starSelect(String num, boolean chk);
}
