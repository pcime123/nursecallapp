package com.sscctv.nursecallapp.data;

public class EmCallLogItem {

    private String device;
    private String stat, ward;
    private String room, callTime;
    private long callDate;
    private boolean isSelected;

    public EmCallLogItem(String device, String stat, String ward, String room, long callDate) {
        this.device = device;
        this.stat = stat;
        this.ward = ward;
        this.room = room;
        this.callDate = callDate;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
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


    public long getCallDate() {
        return callDate;
    }

    public void setCallDate(long callDate) {
        this.callDate = callDate;
    }

}
