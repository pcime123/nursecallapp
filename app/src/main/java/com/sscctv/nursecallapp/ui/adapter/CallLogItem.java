package com.sscctv.nursecallapp.ui.adapter;

public class CallLogItem {

    private String deviceType, callType;
    private String  callLocation;
    private String callTime, callDate;
    private boolean isSelected;

    public CallLogItem(String deviceType, String callType, String callLocation, String callTime, String callDate, boolean isSelected) {
        this.deviceType = deviceType;
        this.callType = callType;
        this.callLocation = callLocation;
        this.callTime = callTime;
        this.callDate = callDate;
        this.isSelected = isSelected;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getCallLocation() {
        return callLocation;
    }

    public void setCallLocation(String callLocation) {
        this.callLocation = callLocation;
    }

    public String getCallTime() {
        return callTime;
    }

    public void setCallTime(String callTime) {
        this.callTime = callTime;
    }

    public String getCallDate() {
        return callDate;
    }

    public void setCallDate(String callDate) {
        this.callDate = callDate;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
