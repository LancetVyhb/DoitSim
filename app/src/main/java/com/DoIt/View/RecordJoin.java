package com.DoIt.View;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.DoIt.Daos;
import com.DoIt.Adapters.RecordJoinAdapter;
import com.DoIt.GreenDaos.Dao.Joins;
import com.DoIt.GreenDaos.Dao.ProjectItems;
import com.DoIt.Items.ProjectAdapterItem;
import com.DoIt.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

public class RecordJoin extends AppCompatActivity {
    private static final String[] SHOW_STATUS = {
            "你已退出该任务，是否删除该任务？",
            "该任务已解散，是否删除该任务？",
    };
    private RecordJoinAdapter adapter;
    private Joins self;
    private BroadcastReceiver receiver;
    private int status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_join);
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        registerReceiver(receiver, new IntentFilter("dataPush"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        unregisterReceiver(receiver);
    }

    private void initView(){
        if (getIntent() != null)
            self = Daos.getInt(this).getJoins(getIntent().getLongExtra("joinId", -1));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(self.getProjects().getTitle());

        RecyclerView recycler = findViewById(R.id.recycler);
        adapter = new RecordJoinAdapter(this);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.addItemDecoration(new DividerItemDecoration
                (this, DividerItemDecoration.VERTICAL));
        recycler.setFocusable(false);
        recycler.setAdapter(adapter);
        adapter.setOnButtonClickListener(new RecordJoinAdapter.OnButtonClickListener() {
            @Override
            public void getJoiner(int position) {
                //人物头像、名字点击事件
                Joins sender = adapter.getItem(position).projectItems.getSender();
                //点击者是事主则弹出管理弹窗，否则跳转至被点击者的个人主页
                if (sender != self) {
                    Intent intent = new Intent
                            (RecordJoin.this, OtherSubject.class);
                    intent.putExtra("subjectObjectId", sender.getJoiner().getObjectId());
                    startActivity(intent);
                }
            }
        });
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getStringExtra("dataChannels").equals(self.getProjects().getObjectId())
                        && !intent.getStringExtra("dataType").equals("Invite")) {
                    long id = intent.getLongExtra("id", -1);
                    if (intent.getStringExtra("dataOption").equals("delete")) {

                        switch (intent.getStringExtra("dataType")) {
                            case "ProjectItem":
                                adapter.deleteProjectItem(id);
                                break;
                            case "Join":
                                adapter.deleteJoin(id);
                                Joins joins = Daos.getInt(RecordJoin.this).getJoins(id);
                                //如果join还存在，那说明这个join与当前用户相关，需要通知用户
                                if (joins != null) {
                                    status = 0;
                                    initShowStatusDialog();
                                } else adapter.notifyDataSetChanged();
                                break;
                            case "Project":
                                status = 1;
                                initShowStatusDialog();
                                break;
                        }
                    }
                }
            }
        };
        //当用户的join被废弃或project解散时通知用户
        if (self.getHasDeleted() || self.getProjects().getHasDeleted()) initShowStatusDialog();
        else getTarget();
    }
    /**
     * 任务状态变化通知弹窗
     */
    private void initShowStatusDialog() {
        if (self.getHasDeleted()) status = 0;
        if (self.getProjects().getHasDeleted()) status = 1;
        AlertDialog.Builder showStatusDialog =
                new AlertDialog.Builder(this, R.style.MyDialogTheme);
        showStatusDialog.setMessage(SHOW_STATUS[status]);
        showStatusDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Daos.getInt(RecordJoin.this).deleteSelfJoin(self);
                finish();
            }
        }).show();
    }
    /**
     * 获取首项
     */
    private void getTarget(){
        ProjectItems target = Daos.getInt(this).getTargetByProjectId(self.getProjectsId());
        List<ProjectAdapterItem> itemList = new ArrayList<>();
        ProjectAdapterItem projectAdapterItem = new ProjectAdapterItem(target);
        itemList.add(projectAdapterItem);
        adapter.setList(itemList);
    }
}
