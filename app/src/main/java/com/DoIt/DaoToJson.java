package com.DoIt;

import android.annotation.SuppressLint;

import com.DoIt.GreenDaos.Dao.Joins;
import com.DoIt.GreenDaos.Dao.ProjectItems;
import com.DoIt.GreenDaos.Dao.Projects;
import com.DoIt.GreenDaos.Dao.Subjects;

import org.json.JSONObject;

import java.text.SimpleDateFormat;

//DaoClass转换到JSONObject的方法都放在这里，用于构建云函数的输入参数
public class DaoToJson {

    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat formatter =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * subjects转换成JSONObject
     * @param subjects subjects
     */
    public static JSONObject subjectsToJson(Subjects subjects) {
        JSONObject subject = new JSONObject();
        try {
            subject.put("objectId", subjects.getObjectId());
            subject.put("userName", subjects.getUserName());
            subject.put("type", subjects.getType());
            if (subjects.getHeadImage() != null)
                subject.put("headImage", subjects.getHeadImage());
            if (subjects.getPhoneNumber() != null)
                subject.put("phoneNumber", subjects.getPhoneNumber());
            if (subjects.getCreatedAt() != null)
                subject.put("createdAt", formatter.format(subjects.getCreatedAt()));
            if (subjects.getUpdatedAt() != null)
                subject.put("updatedAt", formatter.format(subjects.getUpdatedAt()));
            return subject;
        } catch (Exception e) {
            e.printStackTrace();
            return subject;
        }
    }
    /**
     * joins转换成JSONObject
     * @param joins joins
     * @param isNew true则为新创建的，false则为已有的，还得把附带的joiner和projects也转换成JSONObject
     */
    public static JSONObject joinsToJson(Joins joins,boolean isNew) {
        JSONObject join = new JSONObject();
        try {
            if (joins.getObjectId() != null)
                join.put("objectId", joins.getObjectId());
            if (joins.getCreatedAt() != null)
                join.put("createdAt", formatter.format(joins.getCreatedAt()));
            if (joins.getUpdatedAt() != null)
                join.put("updatedAt", formatter.format(joins.getUpdatedAt()));
            join.put("role", joins.getRole());
            join.put("privacy", joins.getPrivacy());
            //把附带的joiner和projects也转换成JSONObject
            if (!isNew) {
                if (joins.getJoiner() != null)
                    join.put("joiner", subjectsToJson(joins.getJoiner()));
                if (joins.getProjects() != null)
                    join.put("project", projectsToJson(joins.getProjects(), false));
            }
            return join;
        } catch (Exception e) {
            e.printStackTrace();
            return join;
        }
    }
    /**
     * projects转换成JSONObject
     * @param projects projects
     * @param isNew true则为新创建的，false则为已有的，还得把附带的sender也转换成JSONObject
     */
    public static JSONObject projectsToJson(Projects projects, boolean isNew) {
        JSONObject project = new JSONObject();
        try {
            if (projects.getObjectId() != null)
                project.put("objectId", projects.getObjectId());
            if (projects.getUpdatedAt() != null)
                project.put("updatedAt", formatter.format(projects.getUpdatedAt()));
            if (projects.getCreatedAt() != null)
                project.put("createdAt", formatter.format(projects.getCreatedAt()));
            project.put("struct", projects.getStruct());
            project.put("number", projects.getNumber());
            project.put("title", projects.getTitle());
            //把附带的sender也转换成JSONObject
            if (!isNew) {
                if (projects.getSender() != null)
                    project.put("sender", subjectsToJson(projects.getSender()));
            }
            return project;
        } catch (Exception e) {
            e.printStackTrace();
            return project;
        }
    }
    /**
     * projectItems转换成JSONObject
     * @param projectItems projectItems
     * @param isNew true则为新创建的，false则为已有的，还得把附带的projects、sender、parent也转换成JSONObject
     */
    public static JSONObject projectItemsToJson(ProjectItems projectItems,boolean isNew) {
        JSONObject projectItem = new JSONObject();
        try {
            projectItem.put("content", projectItems.getContent());
            if (projectItems.getObjectId() != null)
                projectItem.put("objectId", projectItems.getObjectId());
            if (projectItems.getCreatedAt() != null)
                projectItem.put("createdAt", formatter.format(projectItems.getCreatedAt()));
            if (projectItems.getUpdatedAt() != null)
                projectItem.put("updatedAt", formatter.format(projectItems.getUpdatedAt()));
            projectItem.put("type", projectItems.getType());
            if (projectItems.getOption() != null)
                projectItem.put("option", projectItems.getOption());
            //把附带的sender、parent、projects也转换成JSONObject
            if (!isNew) {
                if (projectItems.getSender() != null)
                    projectItem.put("sender",
                            joinsToJson(projectItems.getSender(), false));
                if (projectItems.getProjects() != null)
                    projectItem.put("project",
                            projectsToJson(projectItems.getProjects(), false));
                if (projectItems.getParent() != null)
                    projectItem.put("parent",
                            projectItemsToJson(projectItems.getParent(), false));
            }
            return projectItem;
        } catch (Exception e) {
            e.printStackTrace();
            return projectItem;
        }
    }
}
