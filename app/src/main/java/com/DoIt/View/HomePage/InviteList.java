package com.DoIt.View.HomePage;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.DoIt.Adapters.InviteListAdapter;
import com.DoIt.Bmobs;
import com.DoIt.DaoToJson;
import com.DoIt.Daos;
import com.DoIt.GreenDaos.Dao.Invite;
import com.DoIt.GreenDaos.Dao.Subjects;
import com.DoIt.Progress;
import com.DoIt.R;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;

public class InviteList extends Fragment {
    private InviteListAdapter adapter;
    private Progress progress;
    private Invite chooseInvite;
    private boolean hasAccepted;
    private String reply;
    private Subjects self;
    private BroadcastReceiver receiver;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate
                (R.layout.fragment_invite_list, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("InviteList");
        if (getContext() != null)
            getContext().registerReceiver(receiver, new IntentFilter("dataPush"));
      //  setAdapter();
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("InviteList");
        if (getContext() != null)
            getContext().unregisterReceiver(receiver);
    }
    /**
     *初始化页面
     *@param view view
     */
    private void initView(View view){
        adapter = new InviteListAdapter();
        RecyclerView recycler = view.findViewById(R.id.recycler);
        if((getActivity()) != null) {
            self = Daos.getInt(getActivity()).getSelf();
            recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
            recycler.addItemDecoration(new DividerItemDecoration
                    (getActivity(), DividerItemDecoration.VERTICAL));
            recycler.setFocusable(false);
            recycler.setAdapter(adapter);
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getStringExtra("dataType").equals("Invite")) {
                        setAdapter();
                    }
                }
            };
        }
        adapter.setListener(new InviteListAdapter.OnClickListener() {
            @Override
            public void getSender(View v, Subjects sender) {

            }
            @Override
            public void reply(View v, Invite invite, boolean isAccepted) {
                chooseInvite = invite;
                hasAccepted = isAccepted;
                if (isAccepted) reply = "同意邀请";
                else reply = "拒绝邀请";
                initConfirmDialog();
            }
            @Override
            public void cancel(View v, Invite invite) {

            }
            @Override
            public void delete(View v, Invite invite) {

                Toast.makeText(getActivity(), "已删除", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initConfirmDialog(){
        AlertDialog.Builder confirm = new AlertDialog.Builder(getActivity(), R.style.MyDialogTheme);
        confirm.setMessage("是否" + reply);
        confirm.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            //    replyInvite();
            }
        });
        confirm.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        confirm.show();
    }

    private void initJumpDialog(){
        AlertDialog.Builder jump = new AlertDialog.Builder(getActivity(), R.style.MyDialogTheme);
        jump.setMessage("已获取任务内容，是否前往任务页面？");
        jump.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        jump.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        jump.show();
    }

    private void setAdapter(){
        List<SubjectInviteItem> itemList = new ArrayList<>();
        Subjects self = Daos.getInt(getActivity()).getSelf();
        List<Invites> list = Daos.getInt(getActivity()).getInvitesListByJoinerId(self.getId());
        if (list != null) {
            for (Invites invites : list){
                SubjectInviteItem item = new SubjectInviteItem();
                item.invites = invites;
                item.projects = Daos.getInt(getActivity()).checkProjectsExist(invites.getDataObjectId());
                itemList.add(item);
            }
            adapter.setList(itemList);
        }
    }

    private void replyInvite(){
        if (getActivity() != null) {
            progress = new Progress(getActivity());
            progress.setThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject invite;
                    JSONObject newData = new JSONObject();
                    try {
                        invite = DaoToJson.invitesToJson(chooseItem.invites);
                        if (invite != null) invite.put("hasAccepted", hasAccepted);
                        newData.put("dataOption", "update");
                        newData.put("dataType", "Invite");
                        newData.put("data", invite);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    List<String> channels = new ArrayList<>();
                    channels.add(chooseItem.projects.getObjectId());
                    Bmobs.sendMessage(channels, newData, new Bmobs.Result<Object>() {
                        @Override
                        public void onData(Object o, BmobException e) {
                            progress.finishProgress();
                            if ( e == null) {
                                Invites invites = chooseItem.invites;
                                invites.setHasAccepted(hasAccepted);
                                invites.update();
                                setAdapter();
                                Toast.makeText(getActivity(),
                                        "已" + reply, Toast.LENGTH_SHORT).show();
                                if (hasAccepted) initSetJoin();
                            } else Toast.makeText(getActivity(),
                                    "回复失败" + e.getMessage() , Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).startProgress("正在发送消息，请稍等");
        }
    }
}
