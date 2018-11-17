package com.DoIt.Items;

import com.DoIt.JavaBean.Subject;

public class SearchSubjectItem {
    public Subject subject;
    public boolean isChose;
    public SearchSubjectItem(Subject subject){
        this.subject = subject;
        this.isChose = false;
    }
}
