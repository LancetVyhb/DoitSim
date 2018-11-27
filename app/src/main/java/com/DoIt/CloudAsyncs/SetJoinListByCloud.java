package com.DoIt.CloudAsyncs;

import android.app.Activity;
import android.widget.Toast;

import com.DoIt.DaoToJson;
import com.DoIt.GreenDaos.Dao.ProjectItems;
import com.DoIt.GreenDaos.Dao.Subjects;
import com.DoIt.Progress;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CloudCodeListener;

public class SetJoinListByCloud {
    private CloudAsyncsListener listener;
    private JSONObject params;
    private Progress upload;
    private Activity activity;
    private AsyncCustomEndpoints asc;
    private CloudCodeListener setJoinList;
    private List<Subjects> list;
    private ProjectItems target;

    public SetJoinListByCloud(
            Activity activity,
            List<Subjects> list,
            ProjectItems target
    ) {
        this.params = new JSONObject();
        this.asc = new AsyncCustomEndpoints();
        this.activity = activity;
        this.list = list;
        this.target = target;
    }

    public void setListener(CloudAsyncsListener listener) {
        this.listener = listener;
    }

    public void setCloudAsc() {
        try {
            JSONArray joinerList = new JSONArray();
            JSONArray channels = new JSONArray();
            for (int i = 0; i < list.size(); i++)
                joinerList.put(i, DaoToJson.subjectsToJson(list.get(i)));
            for (int i = 0; i < list.size(); i++)
                channels.put(i, list.get(i).getObjectId());
            params.put("joinerList", joinerList);
            params.put("channels", channels);
            params.put("project", DaoToJson.projectsToJson(target.getProjects(), false));
            params.put("parentId", target.getObjectId());
        } catch (Exception e) {
            Toast.makeText(activity, "数据转换失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            listener.onFailed(e);
        }
        setJoinList = new CloudCodeListener() {
            @Override
            public void done(Object o, BmobException e) {
                upload.finishProgress();
                if (e == null) listener.onSuccess(new long[0]);
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
                asc.callEndpoint("setJoinList", params, setJoinList);
            }
        }).startProgress("正在上传数据，请稍等");
    }
}