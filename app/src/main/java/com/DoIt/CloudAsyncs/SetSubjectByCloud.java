package com.DoIt.CloudAsyncs;

import android.app.Activity;
import android.widget.Toast;

import com.DoIt.Daos;

import org.json.JSONObject;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.BmobInstallationManager;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CloudCodeListener;

public class SetSubjectByCloud {
    private CloudAsyncsListener listener;
    private JSONObject params, subject;
    private Activity activity;
    private AsyncCustomEndpoints asc;
    private String userObjectId;
    private String tab;

    public SetSubjectByCloud(
            Activity activity,
            String userObjectId,
            String tab,
            JSONObject subject
    ){
        this.asc = new AsyncCustomEndpoints();
        this.params = new JSONObject();
        this.activity = activity;
        this.userObjectId = userObjectId;
        this.tab = tab;
        this.subject = subject;
    }

    public void setListener(CloudAsyncsListener listener){
        this.listener = listener;
    }

    public void convertData() {
        try {
            params.put("subject", subject);
            params.put("userId", userObjectId);
            params.put("installationId",
                    BmobInstallationManager.getInstance().getCurrentInstallation().getObjectId());
        } catch (Exception e) {
            Toast.makeText(activity, "数据转换失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            listener.onFailed(e);
        }
        CloudCodeListener signUp = new CloudCodeListener() {
            @Override
            public void done(Object o, BmobException e) {
                if (e == null) dealAscResult(o);
                else {
                    Toast.makeText(activity, "数据上传失败" + o.toString(), Toast.LENGTH_SHORT).show();
                    listener.onFailed(e);
                }
            }
        };
        asc.callEndpoint("setSubject", params, signUp);
    }

    private void dealAscResult(Object o) {
        try {
            JSONObject result = new JSONObject(o.toString());
            JSONObject setSubject = result.optJSONObject("setSubject");
            subject.put("objectId", setSubject.optString("objectId"));
            subject.put("createdAt", setSubject.optString("createdAt"));
            long[] id = new long[1];
            id[0] = Daos.getInt(activity).setSubjectToDao(subject, tab, true);
            listener.onSuccess(id);
        } catch (Exception e) {
            Toast.makeText(activity, "数据存储失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            listener.onFailed(e);
        }
    }
}
