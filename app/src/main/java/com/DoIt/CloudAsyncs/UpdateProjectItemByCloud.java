package com.DoIt.CloudAsyncs;

import android.app.Activity;
import android.widget.Toast;

import com.DoIt.Progress;
import com.DoIt.GreenDaos.Dao.ProjectItems;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CloudCodeListener;

public class UpdateProjectItemByCloud {
    private CloudAsyncsListener listener;
    private JSONObject params;
    private Progress upload;
    private Activity activity;
    private AsyncCustomEndpoints asc;
    private CloudCodeListener updateProjectItem;
    private List<ProjectItems> list;
    private ProjectItems projectItems;
    private String content, projectObjectId;
    private Integer newOption;

    public UpdateProjectItemByCloud(
            Activity activity,
            String projectObjectId,
            String content,
            Integer option,
            ProjectItems projectItems
    ){
        this.asc = new AsyncCustomEndpoints();
        this.params = new JSONObject();
        this.activity = activity;
        this.content = content;
        this.newOption = option;
        this.projectObjectId = projectObjectId;
        this.projectItems = projectItems;
    }

    public void setListener(CloudAsyncsListener listener){
        this.listener = listener;
    }

    public void convertData() {
        JSONObject projectItem = new JSONObject();
        if (projectItems.getType() != 2) list = projectItems.getChildren();
        if (projectItems.getType() == 0) {
            int childrenSize = list.size();
            for (int i = 0; i < childrenSize; i++)
                if (list.get(i).getChildren() != null)
                    list.addAll(list.get(i).getChildren());
        }
        JSONArray children = new JSONArray();
        try {
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    JSONObject child = new JSONObject();
                    child.put("objectId", list.get(i).getObjectId());
                    children.put(i, child);
                }
            }
            projectItem.put("objectId", projectItems.getObjectId());
            projectItem.put("option", newOption);
            projectItem.put("content", content);
            params.put("projectItem", projectItem);
            params.put("children", children);
            params.put("projectId", projectObjectId);
            setCloudAsc();
        } catch (Exception e) {
            Toast.makeText(activity, "数据转换失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            listener.onFailed(e);
        }
    }

    private void setCloudAsc() {
        updateProjectItem = new CloudCodeListener() {
            @Override
            public void done(Object o, BmobException e) {
                upload.finishProgress();
                if (e == null) {
                    Toast.makeText(activity, "数据上传成功", Toast.LENGTH_SHORT).show();
                    listener.onSuccess(new long[0]);
                } else {
                    Toast.makeText(activity, "数据上传失败" + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    listener.onFailed(e);
                }
            }
        };
        upload = new Progress(activity);
        upload.setThread(new Runnable() {
            @Override
            public void run() {
                asc.callEndpoint("updateProjectItem", params, updateProjectItem);
            }
        }).startProgress("正在上传数据，请稍等");
    }
}
