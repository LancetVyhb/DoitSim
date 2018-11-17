package com.DoIt.Items;

import com.DoIt.GreenDaos.Dao.ProjectItems;

public class ProjectAdapterItem {
    public boolean isOpen, isHide;
    public ProjectItems projectItems;
    public int childrenSize;
    public ProjectAdapterItem(ProjectItems projectItems) {
        this.projectItems = projectItems;
        isHide = false;
        isOpen = false;
        childrenSize = 0;
    }
    public ProjectAdapterItem() {
        isHide = false;
        isOpen = false;
        childrenSize = 0;
    }
}
