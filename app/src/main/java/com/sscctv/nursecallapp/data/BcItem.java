package com.sscctv.nursecallapp.data;

public class BcItem {

    private String num;
    private String name;
    private boolean isSetup;
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

    public boolean isSetup() {
        return isSetup;
    }

    public void setSetup(boolean isSelected) {
        this.isSetup = isSelected;
    }

    public BcItem(String num, String name, boolean isSetup) {
        this.num = num;
        this.name = name;
        this.isSetup = isSetup;
    }


}
