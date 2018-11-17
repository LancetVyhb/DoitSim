package com.DoIt.CloudAsyncs;

import android.app.Activity;
import android.widget.Toast;

import com.DoIt.GreenDaos.Dao.Projects;
import com.DoIt.DaoToJson;
import com.DoIt.Progress;

import org.json.JSONObject;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CloudCodeListener;

public class UpdateProjectByCloud {
    private CloudAsyncsListener listener;
    private JSONObject params;
    private Progress upload;
    private Activity activity;
    private AsyncCustomEndpoints asc;
    private CloudCodeListener updateProject;
    private Projects projects;

    public UpdateProjectByCloud(
            Activity activity,
            Projects projects
    ){
        this.asc = new AsyncCustomEndpoints();
        this.params = new JSONObject();
        this.activity = activity;
        this.projects = projects;
    }

    public void setListener(CloudAsyncsListener listener){
        this.listener = listener;
    }

    public void setUpdateProject() {
        try {
            params.put("project", DaoToJson.projectsToJson(projects, false));
        } catch (Exception e) {
            Toast.makeText(activity, "数据转换失败" + e.toString(), Toast.LENGTH_SHORT).show();
            listener.onFailed(e);
        }
        updateProject = new CloudCodeListener() {
            @Override
            public void done(Object o, BmobException e) {
                upload.finishProgress();
                if (e == null) {
                    Toast.makeText(activity, "数据上传成功", Toast.LENGTH_SHORT).show();
                    listener.onSuccess(new long[0]);
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
                asc.callEndpoint("updateProject", params, updateProject);
            }
        }).startProgress("正在上传数据，请稍等");
    }
}
