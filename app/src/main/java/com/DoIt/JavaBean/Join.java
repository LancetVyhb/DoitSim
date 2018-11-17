package com.DoIt.JavaBean;

import cn.bmob.v3.BmobObject;

public class Join extends BmobObject{
    private Project project;
    private Subject joiner;
    private Integer role;
    private Integer privacy;
    public Project getProject() {
        return project;
    }
    public void setProject(Project project) {
        this.project = project;
    }
    public Subject getJoiner() {
        return joiner;
    }
    public void setJoiner(Subject joiner) {
        this.joiner = joiner;
    }
    public void setRole(Integer role) {
        this.role = role;
    }
    public Integer getRole() {
        return role;
    }
    public void setPrivacy(Integer privacy) {
        this.privacy = privacy;
    }
    public Integer getPrivacy() {
        return privacy;
    }
}
