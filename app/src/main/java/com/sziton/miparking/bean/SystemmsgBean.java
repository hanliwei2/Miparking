package com.sziton.miparking.bean;

/**
 * Created by fwj on 2017/11/10.
 */

public class SystemmsgBean {
    private String date;
    private String content;

    public SystemmsgBean(String date,String content){
        this.date=date;
        this.content=content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
