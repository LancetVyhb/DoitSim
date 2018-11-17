package com.DoIt.CloudAsyncs;

import android.app.Activity;
import android.widget.Toast;

import com.DoIt.DaoToJson;
import com.DoIt.Progress;
import com.DoIt.GreenDaos.Dao.Joins;

import org.json.JSONObject;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CloudCodeListener;

public class UpdateJoinByCloud {
    private CloudAsyncsListener listener;
    private JSONObject params;
    private Progress upload;
    private Activity activity;
    private AsyncCustomEndpoints asc;
    private CloudCodeListener updateJoin;
    private Joins joins;
    private String projectObjectId;

    public UpdateJoinByCloud(Activity activity,Joins joins, String projectObjectId){
        this.asc = new AsyncCustomEndpoints();
        this.params = new JSONObject();
        this.activity = activity;
        this.joins = joins;
        this.projectObjectId = projectObjectId;
    }

    public void setListener(CloudAsyncsListener listener){
        this.listener = listener;
    }

    public void setCloudAsc() {
        if (joins.getRole() == 1) joins.setRole(2);
        else joins.setRole(1);
        try {
            params.put("join", DaoToJson.joinsToJson(joins, false));
            params.put("projectId", projectObjectId);
        } catch (Exception e) {
            Toast.makeText(activity, "数据转换失败" + e.toString(), Toast.LENGTH_SHORT).show();
            listener.onFailed(e);
        }
        updateJoin = new CloudCodeListener() {
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
                asc.callEndpoint("updateJoin", params, updateJoin);
            }
        });
        upload.startProgress("正在上传数据，请稍等");
    }
}
