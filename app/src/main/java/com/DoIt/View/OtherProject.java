package com.DoIt.View;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.DoIt.Bmobs;
import com.DoIt.Daos;
import com.DoIt.Progress;
import com.DoIt.Adapters.OtherProjectAdapter;
import com.DoIt.GreenDaos.Dao.Projects;
import com.DoIt.JavaBean.Project;
import com.DoIt.JavaBean.ProjectItem;
import com.DoIt.R;

import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;

public class OtherProject extends AppCompatActivity {
    private Button join;
    private Progress progress;
    private OtherProjectAdapter adapter;
    private Projects projects;
    private Project project;
    private JSONObject power;
    private String projectObjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_project);
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
    /**
     * 初始化页面
     */
    private void initView() {
        projectObjectId = getIntent().getStringExtra("projectObjectId");
        projects = Daos.getInt(OtherProject.this).checkProjectsExist(projectObjectId);

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(OtherProject.this));
        recycler.addItemDecoration(new DividerItemDecoration
                (OtherProject.this, DividerItemDecoration.VERTICAL));
        recycler.setFocusable(false);

        adapter = new OtherProjectAdapter(OtherProject.this);
        adapter.setOnButtonClickListener(new OtherProjectAdapter.OnButtonClickListener() {
            @Override
            public void getJoiner(int position) {
                //点击参与者事件
                if (Daos.getInt(OtherProject.this)
                        .checkSubjectsExist(adapter.getJoiner(position).getObjectId()) == null)
                    Daos.getInt(OtherProject.this)
                            .setSubjectToDao(adapter.getJoiner(position), "联系人", false);
                Intent intent = new Intent(OtherProject.this, OtherSubject.class);
                intent.putExtra("subjectObjectId", adapter.getJoiner(position).getObjectId());
                startActivity(intent);
            }
        });
        recycler.setAdapter(adapter);

        join = findViewById(R.id.join);
        join.setVisibility(View.VISIBLE);
        //“加入”按钮点击事件
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherProject.this, SetJoin.class);
                intent.putExtra("projectObjectId", project.getObjectId());
                startActivity(intent);
                finish();
            }
        });
        progress = new Progress(OtherProject.this);
        progress.setThread(new Runnable() {
            @Override
            public void run() {
                getProject();
            }
        });
        progress.startProgress("正在获取数据，请稍等");
    }
    /**
     * 当主题被删时初始化确认窗口
     */
    private void initShowStatusDialog(){
        AlertDialog.Builder status = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        status.setMessage("该任务已解散，点击“确定”关闭页面");
        status.setCancelable(false);
        status.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (projects != null) Daos.getInt(OtherProject.this).deleteProject(project.getObjectId());
                finish();
            }
        });
        status.show();
    }
    /**
     * 任务不开放浏览时弹出弹窗通知并结束本页面
     */
    private void powerConfirmDialog(){
        AlertDialog.Builder confirm =
                new AlertDialog.Builder(OtherProject.this,R.style.MyDialogTheme);
        confirm.setMessage("本任务不开放浏览");
        confirm.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).show();
    }
    /**
     * 获取本任务的首项
     */
    private void getProject(){
        BmobQuery<Project> query = new BmobQuery<>();
        query.include("sender");
        query.getObject(projectObjectId, new QueryListener<Project>() {
            @Override
            public void done(Project project, BmobException e) {
                if (e == null) {
                    OtherProject.this.project = project;
                    if (projects != null) Daos.getInt(OtherProject.this).updateProjectToDao(project);
                    setView();
                } else {
                    join.setVisibility(View.GONE);
                    progress.finishProgress();
                    if (e.getErrorCode() == 101) initShowStatusDialog();
                    Toast.makeText(OtherProject.this,
                            "获取任务信息错误" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /**
     * 获取任务数据后加载数据到页面
     */
    private void setView(){
        try {
            power = new JSONObject(project.getStruct());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(project.getTitle());
        //如果该project没有开放参加或人数已满100，外人不能自己加入
        if (!power.optBoolean("isFreeJoin")
                || project.getNumber() == 100
                || project.getObjectId() == null)
            join.setVisibility(View.GONE);
        //如果该任务不开放浏览，则退出本页面，否则加载数据
        if(power.optBoolean("isFreeOpen")) getTarget();
        else {
            progress.finishProgress();
            powerConfirmDialog();
        }
    }
    /**
     * 获取本任务的议程
     */
    private void getTarget(){
        Bmobs.getTargetByProject(project.getObjectId(), new Bmobs.Result<ProjectItem>() {
            @Override
            public void onData(ProjectItem projectItem, BmobException e) {
                progress.finishProgress();
                if(e==null) adapter.setTarget(projectItem);
                else Toast.makeText(OtherProject.this,
                        "获取数据失败：" +e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}
