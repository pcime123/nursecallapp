package com.sscctv.nursecallapp.data;

public class RoomItem {

    private String num;
    private int color;

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
    public RoomItem(String num, int color) {
        this.num = num;
        this.color = color;
    }



}
