package com.sscctv.nursecallapp.data;

public class BoardItem {
    private String type;
    private String title;
//    private String writer;
    private long date;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

//    public String getWriter() {
//        return writer;
//    }
//
//    public void setWriter(String writer) {
//        this.writer = writer;
//    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public BoardItem ( String title,long date) {
//        this.type = type;
        this.title = title;
//        this.writer = writer;
        this.date = date;
    }
}
