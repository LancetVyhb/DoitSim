package com.DoIt.Items;

import com.DoIt.JavaBean.ProjectItem;

public class OtherProjectAdapterItem {
    public ProjectItem projectItem;
    public boolean isOpen, isHide;
    public int childrenSize;
    public OtherProjectAdapterItem(ProjectItem projectItem) {
        this.projectItem = projectItem;
        isHide = false;
        isOpen = false;
        childrenSize = 0;
    }
    public OtherProjectAdapterItem() {
        isHide = false;
        isOpen = false;
        childrenSize = 0;
    }
}
