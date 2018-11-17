package com.DoIt.GreenDaos.Dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Tabs {
    @Id
    private Long id;
    private String name;
    @Generated(hash = 1229215696)
    public Tabs(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    @Generated(hash = 1863161359)
    public Tabs() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
