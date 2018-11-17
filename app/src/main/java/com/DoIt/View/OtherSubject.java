package com.DoIt.View;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.DoIt.Bmobs;
import com.DoIt.CloudAsyncs.CloudAsyncsListener;
import com.DoIt.CloudAsyncs.SetJoinListByCloud;
import com.DoIt.Daos;
import com.DoIt.JumpToProjectPage;
import com.DoIt.Progress;
import com.DoIt.Adapters.OtherJoinListAdapter;
import com.DoIt.GreenDaos.Dao.Projects;
import com.DoIt.GreenDaos.Dao.Subjects;
import com.DoIt.JavaBean.Join;
import com.DoIt.JavaBean.Subject;
import com.DoIt.R;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;

import static com.DoIt.View.ChooseProject.CHOOSE_PROJECT_RESULT;

public class OtherSubject extends AppCompatActivity {
    public static final int OTHER_SUBJECT_REQUEST = 489;
    private static final String[] OPTION_UNSHOW = {
            "添加到通讯录",
            "发送任务",
    };
    private static final String[] OPTION_SHOW = {
            "添加标签",
            "发送任务",
    };
    private static final String[] INVITE_OPTION = {
            "已有任务",
            "新任务",
    };
    private RefreshLayout refreshLayout;
    private OtherJoinListAdapter adapter;
    private Progress progress;
    private EditText editText;
    private Subjects subjects;
    private Subject subject;
    private String subjectObjectId;
    private boolean isRefresh;
    private int skip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_subject);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OTHER_SUBJECT_REQUEST && resultCode == CHOOSE_PROJECT_RESULT && data != null) {
            Projects choseProjects = Daos.getInt(this).getProjects(data.getLongExtra("id", -1));
            initSetJoinList(choseProjects);
        }
    }
    /**
     * 初始化页面
     */
    private void initView() {
        subjectObjectId = getIntent().getStringExtra("subjectObjectId");
        subjects = Daos.getInt(this).checkSubjectsExist(subjectObjectId);

        RecyclerView recyclerView = findViewById(R.id.recycler);
        FloatingActionButton add = findViewById(R.id.add);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration
                (this, DividerItemDecoration.VERTICAL));

        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                isRefresh = true;
                setAdapter();
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if ((skip % 20) == 0) loadMoreDate();
                else {
                    refreshLayout.finishLoadMore();
                    Toast.makeText(OtherSubject.this, "下面没有了", Toast.LENGTH_SHORT).show();
                }
            }
        });

        adapter = new OtherJoinListAdapter();
        recyclerView.setAdapter(adapter);
        //列表点击事件
        adapter.setOnItemClickListener(new OtherJoinListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Join join) {
                JumpToProjectPage.jumpByProject(OtherSubject.this, join.getProject());
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initAddDialog();
            }
        });
        progress = new Progress(this);
        progress.setThread(new Runnable() {
            @Override
            public void run() {
                isRefresh = false;
                getSubject();
            }
        }).startProgress("正在获取数据，请稍等");
    }
    /**
     * 初始化操作弹窗
     */
    private void initAddDialog() {
        String[] OPTION;
        if (subjects.getIsShow()) OPTION = OPTION_SHOW;
        else OPTION = OPTION_UNSHOW;
        AlertDialog.Builder option = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        option.setItems(OPTION, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        if (subjects.getIsShow()) initSetTextDialog();
                        else {
                            Daos.getInt(OtherSubject.this).setSubjectShow(subjects, true);
                            Toast.makeText(OtherSubject.this,
                                    "已添加到通讯录", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1:
                        initInviteDialog();
                        break;
                    default:
                        break;
                }
            }
        }).show();
    }
    /**
     * 初始化标签设置弹窗
     */
    private void initSetTextDialog() {
        editText = new EditText(this);
        AlertDialog.Builder setTabDialog = new AlertDialog.Builder
                (this, R.style.MyDialogTheme);
        setTabDialog.setView(editText);
        setTabDialog.setTitle("请输入标签");
        setTabDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Daos.getInt(OtherSubject.this).updateSubjectsTab(subjects.getId(), editText.getText().toString());
                Toast.makeText(OtherSubject.this, "已设置标签:" +
                        editText.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        }).show();
    }
    /**
     * 初始化发送任务弹窗
     */
    private void initInviteDialog() {
        AlertDialog.Builder invite = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        invite.setItems(INVITE_OPTION, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent;
                switch (which) {
                    case 0://已有任务
                        intent = new Intent(OtherSubject.this, ChooseProject.class);
                        intent.setAction("inviteOther");
                        intent.putExtra("subjectObjectId", subject.getObjectId());
                        startActivityForResult(intent, OTHER_SUBJECT_REQUEST);
                        break;
                    case 1://新任务
                        intent = new Intent(OtherSubject.this, SetProject.class);
                        intent.setAction("inviteOther");
                        intent.putExtra("subjectObjectId", subject.getObjectId());
                        startActivity(intent);
                        break;
                }
            }
        }).show();
    }
    /**
     * 获取任务信息
     */
    private void getSubject() {
        BmobQuery<Subject> query = new BmobQuery<>();
        query.getObject(subjectObjectId, new QueryListener<Subject>() {
            @Override
            public void done(Subject subject, BmobException e) {
                if (e == null) {
                    OtherSubject.this.subject = subject;
                    if (subjects != null) Daos.getInt(OtherSubject.this).updateSubjectToDao(subject);
                    else Daos.getInt(OtherSubject.this).setSubjectToDao(subject, "联系人", false);
                    ActionBar actionBar = getSupportActionBar();
                    if (actionBar != null) actionBar.setTitle(subject.getUserName());
                    setAdapter();
                } else {
                    progress.finishProgress();
                    Toast.makeText(OtherSubject.this,
                            "获取人物信息错误" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /**
     * 初次获取对方的任务列表
     */
    private void setAdapter() {
        skip = 0;
        Bmobs.getJoinListBySubjectObjectId(subject.getObjectId(), skip, new Bmobs.Result<List<Join>>() {
            @Override
            public void onData(List<Join> joins, BmobException e) {
                if (!isRefresh) progress.finishProgress();
                else refreshLayout.finishRefresh();
                if (e == null) {
                    if (joins != null) {
                        adapter.setList(removePrivate(joins));
                        //下次加载要跳过的数据量
                        skip = joins.size();
                    }
                } else Toast.makeText(OtherSubject.this,
                        "获取任务列表失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * 下拉加载获取对方更多的任务列表
     */
    private void loadMoreDate() {
        Bmobs.getJoinListBySubjectObjectId(subject.getObjectId(), skip, new Bmobs.Result<List<Join>>() {
            @Override
            public void onData(List<Join> joins, BmobException e) {
                if (e == null) {
                    //下次加载要跳过的数据量
                    skip = skip + joins.size();
                    //设置加载状态，true表示正在加载，false表示没有在加载
                    adapter.addList(removePrivate(joins));
                    refreshLayout.finishLoadMore();
                    Toast.makeText(OtherSubject.this,
                            "已加载更多", Toast.LENGTH_SHORT).show();
                } else Toast.makeText(OtherSubject.this,
                        "加载数据出错" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * 获取数据后排除隐私设置为“仅自己可见”的数据
     * @param list 获取到的原始数据
     */
    private List<Join> removePrivate(List<Join> list) {
        Iterator<Join> iterator = list.iterator();
        while (iterator.hasNext()) {
            Join join = iterator.next();
            if (join.getPrivacy() == 1) iterator.remove();
        }
        return list;
    }
    /**
     * 初始化用于邀请他人参加任务的云函数
     */
    private void initSetJoinList(Projects projects) {
        List<Subjects> list = new ArrayList<>();
        list.add(subjects);
        SetJoinListByCloud cloud = new SetJoinListByCloud(
                this,
                list,
                Daos.getInt(this).getTargetByProjectId(projects.getId())
        );
        cloud.setListener(new CloudAsyncsListener() {
            @Override
            public void onSuccess(long[] id) {
                Toast.makeText(OtherSubject.this, "已发送任务", Toast.LENGTH_SHORT).show();
                refreshLayout.autoRefresh();
            }
            @Override
            public void onFailed(Exception e) { }
        });
        cloud.setCloudAsc();
    }
}
