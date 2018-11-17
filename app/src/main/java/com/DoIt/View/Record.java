package com.DoIt.View;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.DoIt.Daos;
import com.DoIt.Progress;
import com.DoIt.Adapters.SelfJoinListAdapter;
import com.DoIt.CloudAsyncs.CloudAsyncsListener;
import com.DoIt.CloudAsyncs.DeleteProjectByCloud;
import com.DoIt.CloudAsyncs.DeleteSelfJoinByCloud;
import com.DoIt.GreenDaos.Dao.Joins;
import com.DoIt.JavaBean.Join;
import com.DoIt.R;
import com.umeng.analytics.MobclickAgent;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class Record extends AppCompatActivity {
    public static final String[] PRIVACY = {
            "所有人可见" ,
            "仅自己可见"
    };
    private static final String[] OPTION_ITEMS = {
            "提上日程表",
            "隐私设置",
            "删除"
    };
    private int privacy;
    private Progress progress;
    private SelfJoinListAdapter adapter;
    private Joins joins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        adapter.setList(Daos.getInt(Record.this).getRecordJoinList());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
    /**
     * 初始化页面
     */
    private void initView(){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();
        RecyclerView recycler = findViewById(R.id.recycler);
        SearchView search = findViewById(R.id.search);
        recycler.setLayoutManager(new LinearLayoutManager(Record.this));
        recycler.addItemDecoration(new DividerItemDecoration
                (Record.this,DividerItemDecoration.VERTICAL));
        recycler.setFocusable(false);

        adapter = new SelfJoinListAdapter();
        recycler.setAdapter(adapter);
        adapter.setOnItemClickListener(new SelfJoinListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, Joins joins) {
                Intent intent = new Intent(Record.this,RecordJoin.class);
                intent.putExtra("joinId", joins.getId());
                startActivity(intent);
            }
            @Override
            public void onDeal(View v, Joins joins) {
                MobclickAgent.onEvent(Record.this,"manageJoinList");
                Record.this.joins = joins;
                setDialog();
            }
        });
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText != null)
                    adapter.setList(Daos.getInt(Record.this).getRecordJoinListByQuery(newText));
                else adapter.setList(Daos.getInt(Record.this).getRecordJoinList());
                return false;
            }
        });
        search.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) finish();
            }
        });
    }
    /**
     * 初始化任务管理弹窗
     */
    private void setDialog() {
        AlertDialog.Builder optionDialog =
                new AlertDialog.Builder(Record.this,R.style.MyDialogTheme);
        optionDialog.setItems(OPTION_ITEMS, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        addToJoinList();//提上日程表
                        break;
                    case 1:
                        initPrivacyDialog();//隐私设置
                        break;
                    case 2:
                        initDeleteDialog();//删除任务
                        break;
                    default:
                        break;
                }
            }
        }).show();
    }
    /**
     * 将选中的任务提上日程表
     */
    private void addToJoinList() {
        Daos.getInt(this).updateJoinsImportance(joins.getId(),0);
        Toast.makeText(Record.this, "已将该任务提上日程表", Toast.LENGTH_SHORT).show();
    }
    /**
     * 初始化任务隐私设置弹窗
     */
    private void initPrivacyDialog(){
        AlertDialog.Builder privacyDialog =
                new AlertDialog.Builder(Record.this, R.style.MyDialogTheme);
        privacyDialog.setTitle("隐私设置");
        privacyDialog.setSingleChoiceItems(PRIVACY, joins.getPrivacy(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        privacy = which;
                    }
                });
        privacyDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progress = new Progress(Record.this);
                progress.setThread(new Runnable() {
                    @Override
                    public void run() {
                        updateJoinPrivacy();
                    }
                }).startProgress("正在上传信息，请稍等");
            }
        }).show();
    }
    /**
     * 删除任务
     */
    private void initDeleteDialog(){
        AlertDialog.Builder delete =
                new AlertDialog.Builder(Record.this,R.style.MyDialogTheme);
        delete.setMessage("你确定要把该任务删除吗？");
        delete.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (joins.getRole() == 0) initDeleteProject();
                else initDeleteSelfJoin();
            }
        });
        delete.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        delete.show();
    }
    /**
     * 更新任务隐私
     */
    private void updateJoinPrivacy() {
        Join join = new Join();
        join.setObjectId(joins.getObjectId());
        join.setPrivacy(privacy);
        joins.setPrivacy(privacy);
        join.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                progress.finishProgress();
                if (e == null) {
                    joins.update();
                    adapter.setList(Daos.getInt(Record.this).getRecordJoinList());
                    Toast.makeText(Record.this,
                            "隐私设置更新成功", Toast.LENGTH_SHORT).show();
                } else Toast.makeText(Record.this,
                        "隐私设置更新失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * 初始化用于退出任务的云函数
     */
    private void initDeleteSelfJoin() {
        DeleteSelfJoinByCloud cloud =
                new DeleteSelfJoinByCloud(this, joins);
        cloud.setListener(new CloudAsyncsListener() {
            @Override
            public void onSuccess(long[] id) {
                Toast.makeText(Record.this,"任务已删除",Toast.LENGTH_SHORT).show();
                Daos.getInt(Record.this).deleteSelfJoin(joins);
                adapter.removeItem(joins);
            }
            @Override
            public void onFailed(Exception e) { }
        });
        cloud.convertData();
    }
    /**
     * 初始化用于解散任务的云函数
     */
    private void initDeleteProject() {
        DeleteProjectByCloud cloud = new DeleteProjectByCloud(this, joins.getProjects());
        cloud.setListener(new CloudAsyncsListener() {
            @Override
            public void onSuccess(long[] id) {
                Toast.makeText(Record.this,"任务已删除",Toast.LENGTH_SHORT).show();
                Daos.getInt(Record.this).deleteSelfJoin(joins);
                adapter.removeItem(joins);
            }
            @Override
            public void onFailed(Exception e) { }
        });
        cloud.convertData();
    }
}
