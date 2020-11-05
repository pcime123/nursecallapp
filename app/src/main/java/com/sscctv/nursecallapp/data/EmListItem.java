package com.sscctv.nursecallapp.data;

public class EmListItem {

    private String roomInfo, device, ward, room, serial;
    private long time;
    private String request;

    public EmListItem(String roomInfo, String device, String ward, String room, String serial, long time, String request) {
        this.roomInfo = roomInfo;
        this.device = device;
        this.ward = ward;
        this.room = room;
        this.serial = serial;
        this.time = time;
        this.request = request;
    }

    public String getDeviceInfo() {
        return roomInfo;
    }

    public void setDeviceInfo(String roomInfo) {
        this.roomInfo = roomInfo;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
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

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
}
