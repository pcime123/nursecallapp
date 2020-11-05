package com.sscctv.nursecallapp.data;

public class EmergencyItem {

    private String room;
    private int stat;

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }

    public EmergencyItem(String room, int stat) {
        this.room = room;
        this.stat = stat;
    }


}
