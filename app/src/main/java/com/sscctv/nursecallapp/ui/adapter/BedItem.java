package com.sscctv.nursecallapp.ui.adapter;

public class BedItem {

    private String num;
    private String addr;
    private String model;
    private String room;

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getRoom() {return  room;}

    public void setRoom(String room) {this.room = room;}

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public BedItem(String room, String num, String addr, String model) {
        this.room = room;
        this.num = num;
        this.addr = addr;
        this.model = model;

    }


}
