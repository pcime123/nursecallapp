package com.sscctv.nursecallapp.data;

public class CallLogItem {

    private String callerId;
    private String callTime, callDate;
    private String callDir, callStaus;
    private String callNumber;
    private boolean isSelected;

    public CallLogItem(String callNumber, String callerId, String callDir, String callStatus, String callTime, String callDate, boolean isSelected) {
        this.callNumber = callNumber;
        this.callerId = callerId;
        this.callDir = callDir;
        this.callStaus = callStatus;
        this.callTime = callTime;
        this.callDate = callDate;
        this.isSelected = isSelected;
    }

    public String getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }


    public String getCallerId() {
        return callerId;
    }

    public void setCallerId(String callerId) {
        this.callerId = callerId;
    }

    public String getCallDir() {
        return callDir;
    }

    public void setCallDir(String callDir) {
        this.callDir = callDir;
    }

    public String getCallStaus() {
        return callStaus;
    }

    public void setCallStaus(String callStaus) {
        this.callStaus = callStaus;
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
