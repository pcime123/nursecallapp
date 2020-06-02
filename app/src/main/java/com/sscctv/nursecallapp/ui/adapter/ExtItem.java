package com.sscctv.nursecallapp.ui.adapter;

public class ExtItem {

    private String num;
    private String name;
    private boolean isSelected;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public ExtItem(String num, String name, boolean isSelected) {
        this.num = num;
        this.name = name;
        this.isSelected = isSelected;
    }


}
