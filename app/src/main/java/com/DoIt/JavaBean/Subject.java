package com.DoIt.JavaBean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobUser;

public class Subject extends BmobObject {
    private String userName, phoneNumber, headImage;
    private Integer type;
    private BmobUser user;
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getUserName() {
        return userName;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public String getHeadImage() {
        return headImage;
    }
    public void setHeadImage(String headImage) {
        this.headImage = headImage;
    }
    public Integer getType() {
        return type;
    }
    public void setType(Integer type) {
        this.type = type;
    }
    public BmobUser getUser() {
        return user;
    }
    public void setUser(BmobUser user) {
        this.user = user;
    }
}
