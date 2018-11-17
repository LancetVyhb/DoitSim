package com.DoIt.CloudAsyncs;

import android.app.Activity;
import android.widget.Toast;

import com.DoIt.Daos;
import com.DoIt.GreenDaos.Dao.Joins;
import com.DoIt.GreenDaos.Dao.ProjectItems;
import com.DoIt.GreenDaos.Dao.Projects;
import com.DoIt.DaoToJson;
import com.DoIt.GreenDaos.Dao.Subjects;
import com.DoIt.Progress;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.BmobInstallationManager;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CloudCodeListener;

public class SetProjectByCloud {
    private SimpleDateFormat formatter;
    private AsyncCustomEndpoints asc;
    private Activity activity;
    private CloudAsyncsListener listener;
    private JSONObject params;
    private Progress upload;
    private CloudCodeListener setProject;
    private Projects projects;
    private JSONObject power;
    private ProjectItems target;
    private List<Subjects> list;
    private String content, title;
    private Integer privacy;

    public SetProjectByCloud(
            Activity activity,
            List<Subjects> list,
            JSONObject power,
            String content,
            String title,
            Integer privacy
    ) {
        this.formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        this.asc = new AsyncCustomEndpoints();
        this.params = new JSONObject();
        this.activity = activity;
        this.list = list;
        this.power = power;
        this.content = content;
        this.title = title;
        this.privacy = privacy;
    }

    public void setListener(CloudAsyncsListener listener) {
        this.listener = listener;
    }

    public void convertData() {
        projects = new Projects();
        projects.setStruct(power.toString());
        projects.setTitle(title);
        projects.setNumber(1);

        target = new ProjectItems();
        target.setType(0);
        target.setContent(content);
        target.setOption(0);

        JSONArray joinerIdList = new JSONArray();
        JSONArray joinerList = new JSONArray();
        try {
            if (list.size() != 1) {
                for (int i = 1; i < list.size(); i++) {
                    joinerIdList.put(i - 1, list.get(i).getObjectId());
                    joinerList.put(i - 1, DaoToJson.subjectsToJson(list.get(i)));
                }
            }
            params.put("channels", joinerIdList);
            params.put("joinerList", joinerList);
            params.put("project", DaoToJson.projectsToJson(projects, true));
            params.put("sender", DaoToJson.subjectsToJson(list.get(0)));
            params.put("target", DaoToJson.projectItemsToJson(target, true));
            params.put("privacy", privacy);
            params.put("installationId",
                    BmobInstallationManager.getInstance().getCurrentInstallation().getObjectId());
            setCloudAsc();
        } catch (Exception e) {
            Toast.makeText(activity, "数据转换失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            listener.onFailed(e);
        }
    }

    private void setCloudAsc() {
        setProject = new CloudCodeListener() {
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
                asc.callEndpoint("setProject", params, setProject);
            }
        }).startProgress("正在上传数据，请稍等");
    }

    private void dealCloudResult(Object o) {
        try {
            JSONObject result = new JSONObject(o.toString());
            JSONObject setProject = result.getJSONObject("setProject");
            JSONObject setTarget = result.getJSONObject("setTarget");
            JSONArray setJoinList = result.getJSONArray("setJoinList");
            JSONArray setItemList = result.getJSONArray("setItemList");

            projects.setObjectId(setProject.getString("objectId"));
            projects.setCreatedAt(formatter.parse(setProject.getString("createdAt")));
            projects.setUpdatedAt(formatter.parse(setProject.getString("createdAt")));
            projects.setSender(Daos.getInt(activity).getSelf());
            projects.setIsSelf(true);
            projects = Daos.getInt(activity)
                    .getProjects(Daos.getInt(activity).getDaoSession().insert(projects));

            Joins selfJoins = new Joins();
            List<Joins> joinsList = new ArrayList<>();
            for (int i = 0; i < setJoinList.length(); i++) {
                JSONObject success = (new JSONObject
                        (setJoinList.get(i).toString())).getJSONObject("success");
                if (i == 0) {
                    selfJoins.setObjectId(success.getString("objectId"));
                    selfJoins.setCreatedAt(formatter.parse(success.getString("createdAt")));
                    selfJoins.setUpdatedAt(formatter.parse(success.getString("createdAt")));
                    selfJoins.setProjects(projects);
                    selfJoins.setJoiner(list.get(i));
                    if (Daos.getInt(activity).getNowJoinList().size() == 0) selfJoins.setClickTime(0);
                    else selfJoins.setClickTime(Daos.getInt(activity).getNowJoinList().get(0).getClickTime() + 1);
                    selfJoins.setImportance(0);
                    selfJoins.setRole(0);
                    selfJoins.setNewItem(0);
                    selfJoins.setIsSelf(true);
                    selfJoins.setPrivacy(privacy);
                    selfJoins.setStatus(0);
                    selfJoins.setWorkedAt(new Date());
                    selfJoins = Daos.getInt(activity)
                            .getJoins(Daos.getInt(activity).getDaoSession().insert(selfJoins));
                } else {
                    Joins otherJoin = new Joins();
                    otherJoin.setObjectId(success.getString("objectId"));
                    otherJoin.setCreatedAt(formatter.parse(success.getString("createdAt")));
                    otherJoin.setUpdatedAt(formatter.parse(success.getString("createdAt")));
                    otherJoin.setProjects(projects);
                    otherJoin.setJoiner(list.get(i));
                    otherJoin.setRole(2);
                    otherJoin.setIsSelf(false);
                    otherJoin = Daos.getInt(activity)
                            .getJoins(Daos.getInt(activity).getDaoSession().insert(otherJoin));
                    joinsList.add(otherJoin);
                }
            }

            target.setCreatedAt(formatter.parse(setTarget.getString("createdAt")));
            target.setUpdatedAt(formatter.parse(setTarget.getString("createdAt")));
            target.setObjectId(setTarget.getString("objectId"));
            target.setProjects(projects);
            target.setSender(selfJoins);
            target.setIsSelf(true);
            target.setTotal(list.size() - 1);
            target.setAgree(0);
            target.setReject(0);
            target = Daos.getInt(activity)
                    .getProjectItems(Daos.getInt(activity).getDaoSession().insert(target));

            for (int i = 0; i < setItemList.length(); i++) {
                JSONObject success = (new JSONObject
                        (setItemList.get(i).toString())).getJSONObject("success");
                ProjectItems items = new ProjectItems();
                items.setObjectId(success.getString("objectId"));
                items.setCreatedAt(formatter.parse(success.getString("createdAt")));
                items.setUpdatedAt(formatter.parse(success.getString("createdAt")));
                items.setProjects(projects);
                items.setSender(joinsList.get(i));
                items.setParent(target);
                items.setContent("");
                items.setOption(3);
                items.setType(1);
                items.setBeReplied(true);
                items.setIsSelf(false);
                items.setTotal(0);
                items.setAgree(0);
                items.setReject(0);
                Daos.getInt(activity).getDaoSession().insert(items);
            }
            long[] id = new long[1];
            id[0] = selfJoins.getId();
            listener.onSuccess(id);
        } catch (Exception e) {
            Toast.makeText(activity, "数据存储失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            listener.onFailed(e);
        }
    }
}
