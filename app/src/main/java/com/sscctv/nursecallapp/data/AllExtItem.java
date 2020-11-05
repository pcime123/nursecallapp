package com.sscctv.nursecallapp.data;

public class AllExtItem implements Comparable<AllExtItem> {

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

    public AllExtItem(String num, String name, boolean isSelected) {
        this.num = num;
        this.name = name;
        this.isSelected = isSelected;

    }


    @Override
    public int compareTo(AllExtItem allExtItem) {
        if (Integer.valueOf(this.num) < Integer.valueOf(allExtItem.getNum())) {
            return -1;
        } else if (Integer.valueOf(this.num) > Integer.valueOf(allExtItem.getNum())) {
            return 1;
        }
        return 0;
    }
}
