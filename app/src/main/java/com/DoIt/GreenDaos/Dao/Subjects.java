package com.DoIt.GreenDaos.Dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Subjects {
    @Id
    private Long id;
    private boolean isShow;
    private String userName;
    private String headImage;
    private String phoneNumber;
    private String tab;
    private String objectId;
    private Integer type;
    private Date createdAt;
    private Date updatedAt;
    @Generated(hash = 111015721)
    public Subjects(Long id, boolean isShow, String userName, String headImage,
            String phoneNumber, String tab, String objectId, Integer type,
            Date createdAt, Date updatedAt) {
        this.id = id;
        this.isShow = isShow;
        this.userName = userName;
        this.headImage = headImage;
        this.phoneNumber = phoneNumber;
        this.tab = tab;
        this.objectId = objectId;
        this.type = type;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    @Generated(hash = 1488062786)
    public Subjects() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public boolean getIsShow() {
        return this.isShow;
    }
    public void setIsShow(boolean isShow) {
        this.isShow = isShow;
    }
    public String getUserName() {
        return this.userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getHeadImage() {
        return this.headImage;
    }
    public void setHeadImage(String headImage) {
        this.headImage = headImage;
    }
    public String getPhoneNumber() {
        return this.phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getTab() {
        return this.tab;
    }
    public void setTab(String tab) {
        this.tab = tab;
    }
    public String getObjectId() {
        return this.objectId;
    }
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
    public Integer getType() {
        return this.type;
    }
    public void setType(Integer type) {
        this.type = type;
    }
    public Date getCreatedAt() {
        return this.createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public Date getUpdatedAt() {
        return this.updatedAt;
    }
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
