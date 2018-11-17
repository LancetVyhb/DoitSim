package com.DoIt.CloudAsyncs;

import android.app.Activity;
import android.widget.Toast;

import com.DoIt.Progress;
import com.DoIt.GreenDaos.Dao.Joins;
import com.DoIt.GreenDaos.Dao.ProjectItems;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CloudCodeListener;

public class DeleteSelfJoinByCloud {
    private AsyncCustomEndpoints asc;
    private Activity activity;
    private CloudAsyncsListener listener;
    private JSONObject params;
    private Progress upload;
    private CloudCodeListener deleteSelfJoin;
    private Joins joins;

    public DeleteSelfJoinByCloud(
            Activity activity,
            Joins joins
    ){
        this.asc = new AsyncCustomEndpoints();
        this.params = new JSONObject();
        this.activity = activity;
        this.joins = joins;
    }

    public void setListener(CloudAsyncsListener listener){
        this.listener = listener;
    }

    public void convertData() {
        List<ProjectItems> itemsList =joins.getProjectItemsList();
        JSONArray projectItemIdList = new JSONArray();
        try {
            for (int i = 0; i < itemsList.size(); i++)
                projectItemIdList.put(i, itemsList.get(i).getObjectId());
            params.put("projectItemIdList", projectItemIdList);
            params.put("joinId", joins.getObjectId());
            params.put("projectId", joins.getProjects().getObjectId());
            setCloudAsc();
        } catch (Exception e) {
            Toast.makeText(activity,
                    "数据转换失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            listener.onFailed(e);
        }
    }

    private void setCloudAsc(){
        deleteSelfJoin = new CloudCodeListener() {
            @Override
            public void done(Object o, BmobException e) {
                upload.finishProgress();
                if(e==null){
                    Toast.makeText(activity, "数据上传成功",Toast.LENGTH_SHORT).show();
                    listener.onSuccess(null);
                }else {
                    Toast.makeText(activity,
                            "数据上传失败"+e.getMessage(),Toast.LENGTH_SHORT).show();
                    listener.onFailed(e);
                }
            }
        };
        upload = new Progress(activity);
        upload.setThread(new Runnable() {
            @Override
            public void run() {
                asc.callEndpoint("deleteSelfJoin",params,deleteSelfJoin);
            }
        }).startProgress("正在上传数据，请稍等");
    }
}
