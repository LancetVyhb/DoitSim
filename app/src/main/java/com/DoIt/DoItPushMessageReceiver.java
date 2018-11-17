package com.DoIt;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.DoIt.GreenDaos.Dao.Joins;
import com.DoIt.GreenDaos.Dao.ProjectItems;
import com.DoIt.GreenDaos.Dao.Subjects;
import com.DoIt.JavaBean.ProjectItem;
import com.DoIt.View.JoinedProject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import cn.bmob.push.PushConstants;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobInstallationManager;
import cn.bmob.v3.InstallationListener;
import cn.bmob.v3.exception.BmobException;

import static android.content.Context.NOTIFICATION_SERVICE;

public class DoItPushMessageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), PushConstants.ACTION_MESSAGE)) {
            //通知信息初始化
            Notification.Builder builder = new Notification.Builder(context);
            //显示在通知栏上的图标是必须的
            Bitmap btm = BitmapFactory.decodeResource(context.getResources(), R.mipmap.app_big_icon);
            builder.setLargeIcon(btm);
            builder.setSmallIcon(R.drawable.app_small_icon);
            //在通知栏上显示文字信息，可以不写
            builder.setTicker("你有一条新信息！");
            //这是状态栏上的信息
            builder.setDefaults(Notification.DEFAULT_ALL);

            Subjects self = Daos.getInt(context).getSelf();
            Intent send = new Intent("dataPush");
            long id = -1;
            boolean shouldNotice = true;
            JSONObject newData = null;
            JSONObject data = null;
            try {
                newData = new JSONObject(intent.getStringExtra("msg"));
                data = newData.getJSONObject("data");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (data != null) {
                //dataOption指数据变化类型，set为添加，update为更新，delete为删除
                //dateType指数据类型，如join、project、projectItem、invite
                String dataOption = newData.optString("dataOption");
                String dataType = newData.optString("dataType");
                switch (dataType) {
                    case "Join":
                        if (dataOption.equals("update")) {
                            //更新，一般与参与人职位升降有关
                            id = Daos.getInt(context).updateJoinToDao(data);
                            Joins joins = Daos.getInt(context).getJoins(id);
                            String userName = joins.getJoiner().getUserName();
                            if (userName.equals(self.getUserName())) userName = "你";
                            String[] message = {"被提升为管理员：", "被降为参与者："};
                            builder.setContentTitle(userName + message[joins.getRole() - 1]);
                        } else {
                            //删除
                            Joins joins = Daos.getInt(context)
                                    .checkJoinsExist(data.optString("objectId"));
                            id = joins.getId();
                            String userName = joins.getJoiner().getUserName();
                            if (userName.equals(self.getUserName())) {
                                //如果删除的join与当前用户相关，还得移除频道订阅
                                removeChannel(joins.getProjects().getObjectId());
                                userName = "你";
                                //任务参与人数减一
                            } else
                                Daos.getInt(context).minusProjectNumber(joins.getProjects().getObjectId());
                            builder.setContentTitle(userName + "退出了任务：");
                            Daos.getInt(context).deleteJoin
                                    (data.optString("objectId"), false);
                        }
                        break;
                    case "Project":
                        switch (dataOption) {
                            case "set":
                                //添加
                                id = Daos.getInt(context).setProjectToDao(data);
                                builder.setContentTitle("你已加入任务：");
                                builder.setContentText(data.optString("title"));
                                subscribeChannel(data.optString("objectId"), context);
                                break;
                            case "update":
                                //更新
                                id = Daos.getInt(context).updateProjectToDao(data);
                                builder.setContentTitle("任务设置已更改：");
                                break;
                            case "delete":
                                //删除
                                id = Daos.getInt(context).deleteProject(data.optString("objectId"));
                                builder.setContentTitle("任务已解散：");
                                removeChannel(data.optString("objectId"));
                                break;
                        }
                        break;
                    case "ProjectItem":
                        switch (dataOption) {
                            case "set":
                                JSONArray dataArray = newData.optJSONArray("data");
                                long idList[] = Daos.getInt(context).setOrUpdateProjectItemListToDao(dataArray);
                                send.putExtra("idList", idList);
                                if (dataArray.optJSONObject(0).optInt("type") == 1)
                                    builder.setContentTitle("有" +
                                            Integer.toString(idList.length) + "人加入了任务：");
                                else {
                                    ProjectItems items = Daos.getInt(context).getProjectItems(idList[0]);
                                    String userName = items.getSender().getJoiner().getUserName();
                                    if (userName.equals(self.getUserName())) userName = "你";
                                    //检查新消息是否与自己有关,否则不用通知
                                    if (Daos.getInt(context).checkProjectItemRelative(items.getId()))
                                        builder.setContentTitle(userName + "已回应：");
                                    else shouldNotice = false;
                                }
                                break;
                            case "update":
                                //更新
                                id = Daos.getInt(context).updateProjectItemToDao(data);
                                ProjectItems items = Daos.getInt(context).getProjectItems(id);
                                String userName = items.getSender().getJoiner().getUserName();
                                if (userName.equals(self.getUserName())) userName = "你";
                                //检查新消息是否与自己有关,否则不用通知
                                if (Daos.getInt(context).checkProjectItemRelative(items.getId()))
                                    builder.setContentTitle(userName + "已更新议程");
                                else shouldNotice = false;
                                break;
                            case "delete":
                                //删除
                                items = Daos.getInt(context)
                                        .checkProjectItemsExist(data.optString("objectId"));
                                id = items.getId();
                                //检查新消息是否与自己有关,否则不用通知
                                if (Daos.getInt(context).checkProjectItemRelative(items.getId())) {
                                    userName = items.getSender().getJoiner().getUserName();
                                    if (userName.equals(self.getUserName())) userName = "你";
                                    builder.setContentTitle(userName + "撤回了一条信息");
                                } else shouldNotice = false;
                                Daos.getInt(context).deleteProjectItem
                                        (data.optString("objectId"), false);
                                break;
                        }
                        break;
                }
                //获取与新消息相关的的joins
                Joins relative = Daos.getInt(context)
                        .getJoinByProject(newData.optString("dataChannels"));
                if (relative != null
                        && relative.getProjectItemsList() != null
                        && relative.getProjectItemsList().size() != 0
                        && relative.getImportance() == 0) {
                    //更新新消息相关的任务状态
                    Daos.getInt(context).setJoinStatus(relative);
                    //更新新消息相关的任务新消息通知
                    Daos.getInt(context).updateJoinsNewItem(relative.getId(), 1);
                    //设置跳转页面
                    Intent newIntent = new Intent(context, JoinedProject.class);
                    newIntent.putExtra("joinId", relative.getId());
                    builder.setContentText(relative.getProjects().getTitle());
                    builder.setContentIntent(PendingIntent.getActivity(context,
                            0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
                }
                send.putExtra("dataChannels", newData.optString("dataChannels"));
                send.putExtra("dataType", newData.optString("dataType"));
                send.putExtra("dataOption", newData.optString("dataOption"));
                send.putExtra("id", id);
                //发送通知
                NotificationManager manager =
                        (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                if (manager != null && shouldNotice)
                    manager.notify((int) (Math.random() * 100), builder.build());
                //发送广播
                context.sendBroadcast(send);
            }
        }
    }
    /**
     * 退出任务后退订频道
     * @param projectObjectId 要退订的频道
     */
    private void removeChannel(String projectObjectId) {
        BmobInstallationManager.getInstance().unsubscribe(Collections.singletonList(projectObjectId),
                new InstallationListener<BmobInstallation>() {
            @Override
            public void done(BmobInstallation bmobInstallation, BmobException e) { }
        });
    }
    /**
     * 加入任务后订阅频道
     * @param projectObjectId 要退订的频道
     * @param context 上下文
     */
    private void subscribeChannel(final String projectObjectId, Context context) {
        BmobInstallationManager.getInstance().subscribe(Collections.singletonList(projectObjectId),
                new InstallationListener<BmobInstallation>() {
            private Context context;
            @Override
            public void done(BmobInstallation bmobInstallation, BmobException e) {
                if (e == null) getProjectItemList(projectObjectId, context);
            }
            private InstallationListener<BmobInstallation> setContext(Context context){
                this.context = context;
                return this;
            }
        }.setContext(context));
    }
    /**
     * 加入任务后获取任务内容
     * @param projectObjectId 要退订的频道
     * @param context 上下文
     */
    private void getProjectItemList(String projectObjectId, Context context){
        Bmobs.getProjectItemListByProject(projectObjectId, new Bmobs.Result<List<ProjectItem>>() {
            private Context context;
            @Override
            public void onData(List<ProjectItem> projectItems, BmobException e) {
                if (e == null) {
                    //存储任务议程
                    Daos.getInt(context).setOrUpdateProjectItemListToDao(projectItems);
                    //获取与新消息相关的的joins
                    Joins relative = Daos.getInt(context)
                            .getJoinByProject(projectItems.get(0).getProject().getObjectId());
                    //更新新消息相关的任务状态
                    Daos.getInt(context).setJoinStatus(relative);
                    //更新新消息相关的任务新消息通知
                    Daos.getInt(context).updateJoinsNewItem(relative.getId(), projectItems.size());
                    //设置并发送广播
                    Intent send = new Intent("dataPush");
                    send.putExtra("dataChannels", relative.getProjects().getObjectId());
                    send.putExtra("dataType", "Join");
                    send.putExtra("dataOption", "set");
                    send.putExtra("id", relative.getId());
                    context.sendBroadcast(send);
                }
            }
            private Bmobs.Result<List<ProjectItem>> setContext(Context context){
                this.context = context;
                return this;
            }
        }.setContext(context));
    }
}

