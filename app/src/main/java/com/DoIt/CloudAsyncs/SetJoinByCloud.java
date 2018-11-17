package com.DoIt.CloudAsyncs;

import android.app.Activity;
import android.widget.Toast;

import com.DoIt.DaoToJson;
import com.DoIt.Daos;
import com.DoIt.GreenDaos.Dao.Projects;
import com.DoIt.Progress;
import com.DoIt.GreenDaos.Dao.Joins;
import com.DoIt.GreenDaos.Dao.ProjectItems;
import com.DoIt.GreenDaos.Dao.Subjects;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.BmobInstallationManager;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CloudCodeListener;

public class SetJoinByCloud {
    private CloudAsyncsListener listener;
    private JSONObject params;
    private Progress upload;
    private Activity activity;
    private AsyncCustomEndpoints asc;
    private CloudCodeListener setJoin;
    private Joins selfJoins;
    private ProjectItems selfItems;
    private Subjects self;
    private String projectObjectId;

    public SetJoinByCloud(
            Activity activity,
            String projectObjectId,
            String content,
            Integer option,
            Integer privacy
    ){
        this.params = new JSONObject();
        this.asc = new AsyncCustomEndpoints();
        this.selfItems = new ProjectItems();
        this.selfJoins = new Joins();
        this.self = Daos.getInt(activity).getSelf();
        this.activity = activity;
        this.projectObjectId = projectObjectId;
        this.selfJoins.setPrivacy(privacy);
        this.selfItems.setContent(content);
        this.selfItems.setOption(option);
    }

    public void setListener(CloudAsyncsListener listener){
        this.listener = listener;
    }

    public void setCloudAsc() {
        try {
            selfJoins.setRole(2);
            selfItems.setType(1);
            params.put("joiner", DaoToJson.subjectsToJson(self));
            params.put("join", DaoToJson.joinsToJson(selfJoins, true));
            params.put("projectItem", DaoToJson.projectItemsToJson(selfItems, true));
            params.put("projectId", projectObjectId);
            params.put("installationId",
                    BmobInstallationManager.getInstance().getCurrentInstallation().getObjectId());
        } catch (Exception e) {
            Toast.makeText(activity, "数据转换失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            listener.onFailed(e);
        }
        setJoin = new CloudCodeListener() {
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
                asc.callEndpoint("setJoin", params, setJoin);
            }
        }).startProgress("正在上传数据，请稍等");
    }

    private void dealCloudResult(Object o) {
        try {
            JSONObject result = new JSONObject(o.toString());
            JSONObject getProject = result.getJSONObject("getProject");
            JSONObject setJoin = result.getJSONObject("setJoin");
            JSONObject setProjectItem = result.getJSONObject("setProjectItem");
            JSONObject getProjectItemList = result.getJSONObject("getProjectItemList");
            JSONArray results = getProjectItemList.getJSONArray("results");

            if (getProject != null && getProject.getInt("number") <= 100) {
                Projects projects = Daos.getInt(activity).checkProjectsExist(projectObjectId);
                if (projects == null)
                    Daos.getInt(activity).setProjectToDao(getProject);
                else Daos.getInt(activity).updateProjectToDao(getProject);
                selfJoins.setId(Daos.getInt(activity).setJoinToDao(setJoin));
                Daos.getInt(activity).setOrUpdateProjectItemListToDao(results);
                Daos.getInt(activity).setProjectItemToDao(setProjectItem);

                long[] id = new long[1];
                id[0] = selfJoins.getId();
                listener.onSuccess(id);
            } else {
                Toast.makeText(activity, "任务已解散或人数已满，无法加入", Toast.LENGTH_SHORT).show();
                listener.onFailed(new Exception("任务已解散或人数已满，无法加入"));
            }
        } catch (Exception e) {
            Toast.makeText(activity, "数据存储失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            listener.onFailed(e);
        }
    }
}
