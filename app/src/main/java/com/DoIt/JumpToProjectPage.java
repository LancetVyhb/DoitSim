package com.DoIt;

import android.app.Activity;
import android.content.Intent;

import com.DoIt.GreenDaos.Dao.Joins;
import com.DoIt.GreenDaos.Dao.Projects;
import com.DoIt.JavaBean.Project;
import com.DoIt.View.JoinedProject;
import com.DoIt.View.OtherProject;
import com.DoIt.View.RecordJoin;

public class JumpToProjectPage {
    /**
     * 跳转至某一任务界面
     * @param activity 上下文
     * @param project 由网络传输的来的任务
     */
    public static void jumpByProject(Activity activity, Project project){
        Projects projects = Daos.getInt(activity).checkProjectsExist(project.getObjectId());
        if (projects == null) {
            //如果这个project没有没有被存储
            Intent intent = new Intent(activity, OtherProject.class);
            intent.putExtra("projectObjectId", project.getObjectId());
            activity.startActivity(intent);
        } else {
            //如果这个project已被存储
            Joins joins = Daos.getInt(activity).getJoinByProject(projects.getId());
            if (joins != null) {
                //如果已经加入了这个project
                if (joins.getImportance() == 0) {
                    //如果加入的任务没有被归档
                    Intent intent = new Intent(activity, JoinedProject.class);
                    intent.putExtra("joinId", joins.getId());
                    activity.startActivity(intent);
                } else {
                    //如果加入的任务已被归档
                    Intent intent = new Intent(activity, RecordJoin.class);
                    intent.putExtra("joinId", joins.getId());
                    activity.startActivity(intent);
                }
            } else {
                //如果还没有加入到这个project
                Intent intent = new Intent(activity, OtherProject.class);
                intent.putExtra("projectObjectId", projects.getObjectId());
                activity.startActivity(intent);
            }
        }
    }
    /**
     * 跳转至某一任务界面
     * @param activity 上下文
     * @param projects 由本地数据库的来的任务
     */
    public static void jumpByProjects(Activity activity, Projects projects) {
        //如果这个project已被存储
        Joins joins = Daos.getInt(activity).getJoinByProject(projects.getId());
        if (joins != null) {
            //如果已经加入了这个project
            if (joins.getImportance() == 0) {
                //如果加入的任务没有被归档
                Intent intent = new Intent(activity, JoinedProject.class);
                intent.putExtra("joinId", joins.getId());
                activity.startActivity(intent);
            } else {
                //如果加入的任务已被归档
                Intent intent = new Intent(activity, RecordJoin.class);
                intent.putExtra("joinId", joins.getId());
                activity.startActivity(intent);
            }
        } else {
            //如果还没有加入到这个project
            Intent intent = new Intent(activity, OtherProject.class);
            intent.putExtra("projectObjectId", projects.getObjectId());
            activity.startActivity(intent);
        }
    }
}
