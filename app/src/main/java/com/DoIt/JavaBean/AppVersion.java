package com.DoIt.JavaBean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

public class AppVersion extends BmobObject{
    private Integer version_i;
    private String channel;
    private String ios_url;
    private boolean isforce;
    private BmobFile path;
    private String platform;
    private String target_size;
    private String update_log;
    private String version;
    private String android_url;

    public Integer getVersion_i() {
        return version_i;
    }
    public void setVersion_i(Integer version_i) {
        this.version_i = version_i;
    }
    public String getAndroid_url() {
        return android_url;
    }
    public void setAndroid_url(String android_url) {
        this.android_url = android_url;
    }
    public String getChannel() {
        return channel;
    }
    public void setChannel(String channel) {
        this.channel = channel;
    }
    public String getIos_url() {
        return ios_url;
    }
    public void setIos_url(String ios_url) {
        this.ios_url = ios_url;
    }
    public boolean isIsforce() {
        return isforce;
    }
    public void setIsforce(boolean isforce) {
        this.isforce = isforce;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getVersion() {
        return version;
    }
    public BmobFile getPath() {
        return path;
    }
    public void setPath(BmobFile path) {
        this.path = path;
    }
    public String getPlatform() {
        return platform;
    }
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    public String getTarget_size() {
        return target_size;
    }
    public void setTarget_size(String target_size) {
        this.target_size = target_size;
    }
    public String getUpdate_log() {
        return update_log;
    }
    public void setUpdate_log(String update_log) {
        this.update_log = update_log;
    }
}
