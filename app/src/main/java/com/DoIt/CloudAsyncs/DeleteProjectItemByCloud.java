package com.DoIt.CloudAsyncs;

import android.app.Activity;
import android.widget.Toast;

import com.DoIt.Progress;
import com.DoIt.GreenDaos.Dao.ProjectItems;

import org.json.JSONObject;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CloudCodeListener;

public class DeleteProjectItemByCloud {
    private AsyncCustomEndpoints asc;
    private Activity activity;
    private CloudAsyncsListener listener;
    private JSONObject params;
    private Progress upload;
    private CloudCodeListener deleteProjectItem;
    private ProjectItems projectItems;

    public DeleteProjectItemByCloud(
            Activity activity,
            ProjectItems projectItems
    ){
        this.asc = new AsyncCustomEndpoints();
        this.params = new JSONObject();
        this.activity = activity;
        this.projectItems = projectItems;
    }

    public void setListener(CloudAsyncsListener listener){
        this.listener = listener;
    }

    public void convertData() {
        try {
            params.put("projectItemId", projectItems.getObjectId());
            params.put("projectId", projectItems.getProjects().getObjectId());
            setCloudAsc();
        } catch (Exception e) {
            listener.onFailed(e);
        }
    }

    private void setCloudAsc() {
        deleteProjectItem = new CloudCodeListener() {
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
                asc.callEndpoint("deleteProjectItem", params, deleteProjectItem);
            }
        }).startProgress("正在上传数据，请稍等");
    }
}
