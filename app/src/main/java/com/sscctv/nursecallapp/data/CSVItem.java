package com.sscctv.nursecallapp.data;

public class CSVItem {
    private String type;
    private String userName;
    private String fullName;
    private String caller;
    private String regName;
    private String regPass;

    public void setType(String type) {
        this.type = type;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public void setRegName(String regName) {
        this.regName = regName;
    }

    public void setRegPass(String regPass) {
        this.regPass = regPass;
    }

    public String getType() {
        return type;
    }

    public String getUserName() {
        return userName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCaller() {
        return caller;
    }

    public String getRegName() {
        return regName;
    }

    public String getRegPass() {
        return regPass;
    }
}
