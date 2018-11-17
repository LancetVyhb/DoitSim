package com.DoIt.JavaBean;

import cn.bmob.v3.BmobObject;

public class ProjectItem extends BmobObject{
    private Project project;
    private Join sender;
    private ProjectItem parent;
    private String content;
    private Integer option;
    private Integer type;
    public void setProject(Project project) {
        this.project = project;
    }
    public Project getProject() {
        return project;
    }
    public Join getSender() {
        return sender;
    }
    public void setSender(Join sender) {
        this.sender = sender;
    }
    public void setParent(ProjectItem parent) {
        this.parent = parent;
    }
    public ProjectItem getParent() {
        return parent;
    }
    public Integer getOption() {
        return option;
    }
    public void setOption(Integer option) {
        this.option = option;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getContent() {
        return content;
    }
    public void setType(Integer type) {
        this.type = type;
    }
    public Integer getType() {
        return type;
    }
}
