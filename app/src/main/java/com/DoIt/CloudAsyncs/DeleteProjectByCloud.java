package com.DoIt.CloudAsyncs;

import android.app.Activity;
import android.widget.Toast;

import com.DoIt.Progress;
import com.DoIt.GreenDaos.Dao.Joins;
import com.DoIt.GreenDaos.Dao.ProjectItems;
import com.DoIt.GreenDaos.Dao.Projects;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CloudCodeListener;

public class DeleteProjectByCloud {
    private AsyncCustomEndpoints asc;
    private Activity activity;
    private CloudAsyncsListener listener;
    private JSONObject params;
    private Progress upload;
    private CloudCodeListener deleteProject;
    private Projects projects;

    public DeleteProjectByCloud(
            Activity activity,
            Projects projects
    ) {
        this.asc = new AsyncCustomEndpoints();
        this.params = new JSONObject();
        this.activity = activity;
        this.projects = projects;
    }

    public void setListener(CloudAsyncsListener listener) {
        this.listener = listener;
    }

    public void convertData() {
        List<Joins> joinsList = projects.getJoinsList();
        List<ProjectItems> projectItemsList = projects.getProjectItemsList();
        try {
            JSONArray joinIdList = new JSONArray();
            JSONArray projectItemIdList = new JSONArray();
            for (int i = 0; i < joinsList.size(); i++)
                joinIdList.put(i, joinsList.get(i).getObjectId());
            for (int i = 0; i < projectItemsList.size(); i++)
                projectItemIdList.put(i, projectItemsList.get(i).getObjectId());
            params.put("projectItemIdList", projectItemIdList);
            params.put("joinIdList", joinIdList);
            params.put("projectId", projects.getObjectId());
            setCloudAsc();
        } catch (Exception e) {
            Toast.makeText(activity,
                    "数据转换失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            listener.onFailed(e);
        }
    }

    private void setCloudAsc() {
        deleteProject = new CloudCodeListener() {
            @Override
            public void done(Object o, BmobException e) {
                upload.finishProgress();
                if (e == null) {
                    Toast.makeText(activity, "数据上传成功", Toast.LENGTH_SHORT).show();
                    listener.onSuccess(null);
                } else {
                    Toast.makeText(activity,
                            "数据上传失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    listener.onFailed(e);
                }
            }
        };
        upload = new Progress(activity);
        upload.setThread(new Runnable() {
            @Override
            public void run() {
                asc.callEndpoint("deleteProject", params, deleteProject);
            }
        }).startProgress("正在上传数据，请稍等");
    }
}
