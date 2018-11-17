package com.DoIt.CloudAsyncs;

import android.app.Activity;
import android.widget.Toast;

import com.DoIt.DaoToJson;
import com.DoIt.Progress;
import com.DoIt.GreenDaos.Dao.Joins;
import com.DoIt.GreenDaos.Dao.ProjectItems;

import org.json.JSONObject;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CloudCodeListener;

public class SetProjectItemByCloud {
    private CloudAsyncsListener listener;
    private JSONObject params;
    private Progress upload;
    private Activity activity;
    private AsyncCustomEndpoints asc;
    private CloudCodeListener setProjectItem;
    private String content;
    private Integer option;
    private Joins self;
    private ProjectItems parent;

    public SetProjectItemByCloud(
            Activity activity,
            String content,
            Integer option,
            Joins self,
            ProjectItems parent
    ){
        this.asc = new AsyncCustomEndpoints();
        this.params = new JSONObject();
        this.activity = activity;
        this.content = content;
        this.option = option;
        this.self = self;
        this.parent = parent;
    }

    public void setListener(CloudAsyncsListener listener){
        this.listener = listener;
    }

    public void convertData() {
        ProjectItems projectItems = new ProjectItems();
        projectItems.setContent(content);
        projectItems.setOption(option);
        projectItems.setType(parent.getType() + 1);
        try {
            params.put("projectItem",
                    DaoToJson.projectItemsToJson(projectItems, true));
            params.put("joinId", self.getObjectId());
            params.put("parentId", this.parent.getObjectId());
            params.put("projectId", self.getProjects().getObjectId());
            setCloudAsc();
        } catch (Exception e) {
            Toast.makeText(activity,
                    "数据转换失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            listener.onFailed(e);
        }
    }

    private void setCloudAsc(){
        setProjectItem = new CloudCodeListener() {
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
                asc.callEndpoint("setProjectItem",params,setProjectItem);
            }
        });
        upload.startProgress("正在上传数据，请稍等");
    }
}
