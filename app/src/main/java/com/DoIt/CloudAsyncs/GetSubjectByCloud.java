package com.DoIt.CloudAsyncs;

import android.app.Activity;
import android.widget.Toast;

import com.DoIt.Daos;
import com.DoIt.Progress;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.BmobInstallationManager;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CloudCodeListener;

public class GetSubjectByCloud {
    private AsyncCustomEndpoints asc;
    private Activity activity;
    private CloudAsyncsListener listener;
    private JSONObject params;
    private Progress upload;
    private CloudCodeListener getSubject;
    private String userObjectId;

    public GetSubjectByCloud(
            Activity activity,
            String userObjectId
    ){
        this.asc = new AsyncCustomEndpoints();
        this.params = new JSONObject();
        this.activity = activity;
        this.userObjectId = userObjectId;
    }

    public void setListener(CloudAsyncsListener listener) {
        this.listener = listener;
    }

    public void convertData() {
        try {
            params.put("userId", userObjectId);
            params.put("installationId",
                    BmobInstallationManager.getInstance().getCurrentInstallation().getObjectId());
            setCloudAsc();
        } catch (Exception e) {
            Toast.makeText(activity, "数据转换失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            listener.onFailed(e);
        }
    }

    private void setCloudAsc() {
        getSubject = new CloudCodeListener() {
            @Override
            public void done(Object o, BmobException e) {
                upload.finishProgress();
                if (e == null) dealCloudResult(o);
                else {
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
                asc.callEndpoint("getSubject", params, getSubject);
            }
        }).startProgress("正在上传数据，请稍等");
    }

    private void dealCloudResult(Object o) {
        try {
            JSONObject result = new JSONObject(o.toString());
            JSONObject subject = result.getJSONObject("getSubject");
            JSONArray joinList = result.getJSONArray("getJoinList");
            JSONArray projectItemList = result.getJSONArray("getProjectItemList");

            if (Daos.getInt(activity).getSelf() == null)
                Daos.getInt(activity).setSubjectToDao(subject, "我", true);
            Daos.getInt(activity).setOrUpdateJoinListToDao(joinList);
            Daos.getInt(activity).divideProjectItemList(projectItemList);
            Daos.getInt(activity).setAllJoinStatus();
            listener.onSuccess(null);
        } catch (Exception e) {
            Toast.makeText(activity, "数据存储失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            listener.onFailed(e);
        }
    }
}
