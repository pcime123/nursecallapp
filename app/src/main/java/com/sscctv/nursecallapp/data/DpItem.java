package com.sscctv.nursecallapp.data;

import android.widget.CheckBox;

public class DpItem {

    private static final String MD5 = "DPITEM";
    private boolean isSelected;
    private String model;
    private String ipAddr;
    private String macAddr;
    private String location;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public String getMacAddr() {
        return macAddr;
    }

    public void setMacAddr(String macAddr) {
        this.macAddr = macAddr;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public DpItem (boolean isSelected, String model, String ipAddr, String macAddr, String location) {
        this.isSelected = isSelected;
        this.model = model;
        this.ipAddr = ipAddr;
        this.macAddr = macAddr;
        this.location = location;
    }


}
