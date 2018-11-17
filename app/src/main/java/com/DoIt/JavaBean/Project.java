package com.DoIt.JavaBean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.datatype.BmobRelation;

public class Project extends BmobObject{
    private Subject sender;
    private String title;
    private Integer number;
    private Integer type;
    private String struct;
    private BmobGeoPoint place;
    private BmobRelation theme;
    public Subject getSender() {
        return sender;
    }
    public void setSender(Subject sender) {
        this.sender = sender;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }
    public void setNumber(Integer number) {
        this.number = number;
    }
    public Integer getNumber() {
        return number;
    }
    public void setType(Integer type) {
        this.type = type;
    }
    public Integer getType() {
        return type;
    }
    public BmobGeoPoint getPlace() {
        return place;
    }
    public void setPlace(BmobGeoPoint place) {
        this.place = place;
    }
    public String getStruct() {
        return struct;
    }
    public void setStruct(String struct) {
        this.struct = struct;
    }
    public BmobRelation getTheme() {
        return theme;
    }
    public void setTheme(BmobRelation theme) {
        this.theme = theme;
    }
}
