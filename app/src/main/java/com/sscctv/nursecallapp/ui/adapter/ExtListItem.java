package com.sscctv.nursecallapp.ui.adapter;

public class ExtListItem {

    private String num;
    private String name;
    private String ward;

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getBed() {
        return bed;
    }

    public void setBed(String bed) {
        this.bed = bed;
    }

    private String room;
    private String bed;

    public ExtListItem(String num, String name, String ward, String room, String bed) {
        this.num = num;
        this.name = name;
        this.ward = ward;
        this.room = room;
        this.bed = bed;
    }


}
