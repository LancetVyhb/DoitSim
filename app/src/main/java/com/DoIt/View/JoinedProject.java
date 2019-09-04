package com.DoIt.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.DoIt.CloudAsyncs.SetJoinListByCloud;
import com.DoIt.Daos;
import com.DoIt.GreenDaos.Dao.Subjects;
import com.DoIt.Progress;
import com.DoIt.Adapters.ProjectAdapter;
import com.DoIt.CloudAsyncs.CloudAsyncsListener;
import com.DoIt.CloudAsyncs.DeleteJoinByCloud;
import com.DoIt.CloudAsyncs.DeleteProjectByCloud;
import com.DoIt.CloudAsyncs.DeleteSelfJoinByCloud;
import com.DoIt.CloudAsyncs.UpdateJoinByCloud;
import com.DoIt.CloudAsyncs.UpdateProjectByCloud;
import com.DoIt.GreenDaos.Dao.Joins;
import com.DoIt.GreenDaos.Dao.ProjectItems;
import com.DoIt.GreenDaos.Dao.Projects;
import com.DoIt.Items.ProjectAdapterItem;
import com.DoIt.JavaBean.Project;
import com.DoIt.R;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationManager;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

import static com.DoIt.View.ChooseSubject.CHOOSE_SUBJECT_RESULT;

public class JoinedProject extends AppCompatActivity {
    private static final String[] OPTIONS_FOR_MANAGER = {
            "访问ta的主页",
            "降为执行人",
            "开除"
    };
    private static final String[] OPTIONS_FOR_JOINER = {
            "访问ta的主页",
            "升为管理者",
            "开除"
    };
    private static final String[] OPTIONS_FOR_PROJECT = {
            "修改标题",
            "权限设置",
            "位置设置",
    };
    private static final String[] POWER = {
            "是否开放参加",
            "是否开放审核",
            "是否开放浏览"
    };
    private static final String[] SHOW_STATUS = {
            "你已退出该任务，是否删除该任务？",
            "该任务已解散，是否删除该任务？",
    };
    private static final String[] SET_PLACE = {
            "设置新位置",
            "清空位置",
    };
    private boolean[] SET_POWER;
    private final int JOINED_PROJECT_REQUEST = 563;
    private int joinOptions,status;
    private ActionBar actionBar;
    private ProjectAdapter adapter;
    private Joins self,other;
    private Projects projects;
    private JSONObject power;
    private EditText editText;
    private Progress progress;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joined_project);
        initView();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        if (Daos.getInt(this).getJoins(self.getId()) != null)
            Daos.getInt(this).setJoinNewItem(self.getId(), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if(self.getRole() != 0) inflater.inflate(R.menu.joined_project_actionbar_menu,menu);
        else inflater.inflate(R.menu.joined_project_actionbar_menu_for_sender,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.option://“设置”按钮，只有事主可以操作
                initProjectOptionDialog();
                break;
            case R.id.invite://“拉人”按钮，如果不开放参加，普通参与者不可操作
                inviteOther();
                break;
            case R.id.delete://“退出”按钮，只有事主无法操作
                initOutConfirmDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //当选定想要邀请的人后把数据传回本页面，调用云函数进行操作
        if (requestCode == JOINED_PROJECT_REQUEST && data != null) {
            if (resultCode == CHOOSE_SUBJECT_RESULT) {
                long[] id = data.getLongArrayExtra("results");
                if (id.length != 0) {
                    List<Subjects> list = new ArrayList<>();
                    for (long anId : id) list.add(Daos.getInt(this).getSubjects(anId));
                    initSetJoinList(list);
                }
            }
        }
    }
    /**
     * 初始化页面
     */
    private void initView() {
        if (getIntent() != null) {
            //获取迁移页面传来的必要信息
            self = Daos.getInt(this).getJoins(getIntent().getLongExtra("joinId", -1));
            //打开某一任务后新消息通知归零
            Daos.getInt(this).setJoinNewItem(self.getId(), 0);
            Daos.getInt(this).addJoinClickTime(self);
            projects = self.getProjects();
            try {
                power = new JSONObject(projects.getStruct());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(self.getProjects().getTitle());

        adapter = new ProjectAdapter(self, this);
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.addItemDecoration(new DividerItemDecoration
                (this, DividerItemDecoration.VERTICAL));
        recycler.setFocusable(false);
        recycler.setAdapter(adapter);

        adapter.setOnButtonClickListener(new ProjectAdapter.OnButtonClickListener() {
            @Override
            public void update(int position) {
                //“编辑”按钮点击事件
                Intent intent = new Intent(JoinedProject.this, SetItem.class);
                intent.putExtra("joinsId", self.getId());
                intent.putExtra("projectItemsId",
                        adapter.getItem(position).projectItems.getId());
                intent.setAction("update");
                startActivityForResult(intent, JOINED_PROJECT_REQUEST);
            }
            @Override
            public void reply(int position) {
                //“回应”按钮点击事件
                Intent intent = new Intent(JoinedProject.this, SetItem.class);
                intent.putExtra("joinsId", self.getId());
                intent.putExtra("projectItemsId",
                        adapter.getItem(position).projectItems.getId());
                intent.setAction("set");
                startActivityForResult(intent, JOINED_PROJECT_REQUEST);
            }
            @Override
            public void getJoiner(int position) {
                //人物头像、名字点击事件
                Joins sender = adapter.getItem(position).projectItems.getSender();
                //点击者是事主则弹出管理弹窗，否则跳转至被点击者的个人主页
                if (self.getRole() == 0 && sender != self) {
                    MobclickAgent.onEvent(JoinedProject.this,"manageJoiner");
                    initJoinOptionDialog(sender);
                    //点击自己没有反应
                } else if (sender != self) {
                    Intent intent = new Intent
                            (JoinedProject.this, OtherSubject.class);
                    intent.putExtra("subjectObjectId", sender.getJoiner().getObjectId());
                    startActivity(intent);
                }
            }
        });
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getStringExtra("dataChannels").equals(projects.getObjectId())) {
                    long id = intent.getLongExtra("id", -1);
                    if (!intent.getStringExtra("dataOption").equals("delete")) {
                        //收到添加或更新的广播
                        switch (intent.getStringExtra("dataType")) {
                            case "ProjectItem":
                                if (intent.getStringExtra("dataOption").equals("set")) {
                                    long[] idList = intent.getLongArrayExtra("idList");
                                    for (long anId : idList)
                                        adapter.setNewProjectItem(Daos.getInt(JoinedProject.this)
                                                .getProjectItems(anId));
                                } else adapter.updateProjectItem
                                        (Daos.getInt(JoinedProject.this).getProjectItems(id));
                                break;
                            case "Join":
                                adapter.notifyDataSetChanged();
                                break;
                            case "Project":
                                adapter.updatePower();
                                projects = Daos.getInt(JoinedProject.this).getProjects(id);
                                actionBar.setTitle(projects.getTitle());
                                break;
                        }
                    } else {
                        switch (intent.getStringExtra("dataType")) {
                            case "ProjectItem":
                                adapter.deleteProjectItem(id);
                                break;
                            case "Join":
                                adapter.deleteJoin(id);
                                Joins joins = Daos.getInt(JoinedProject.this).getJoins(id);
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
        registerReceiver(receiver, new IntentFilter("dataPush"));
        //当用户的join被废弃或project解散时通知用户
        if (self.getHasDeleted() || projects.getHasDeleted()) initShowStatusDialog();
        else getTarget();
    }
    /**
     * 参与者管理弹窗
     * @param sender 被点击者的join
     */
    private void initJoinOptionDialog(Joins sender){
        String[] items;
        other = sender;
        //根据被点击者的身份确定不同的选项
        if(other.getRole()==1) items = OPTIONS_FOR_MANAGER;
        else items = OPTIONS_FOR_JOINER;
        AlertDialog.Builder joinOption = new AlertDialog.Builder(this,R.style.MyDialogTheme);
        joinOption.setTitle(other.getJoiner().getUserName());
        joinOption.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0://访问他的主页
                        Intent intent = new Intent
                                (JoinedProject.this,OtherSubject.class);
                        intent.putExtra("subjectObjectId", other.getJoiner().getObjectId());
                        startActivity(intent);
                        break;
                    case 1://升/降职
                        joinOptions = 1;
                        initUpdateConfirmDialog();
                        break;
                    case 2://开除
                        joinOptions = 2;
                        initUpdateConfirmDialog();
                        break;
                    default:
                        break;
                }
            }
        }).show();
    }
    /**
     * 对参与者管理操作的确认界面
     */
    private void initUpdateConfirmDialog(){
        AlertDialog.Builder confirm = new AlertDialog.Builder(this,R.style.MyDialogTheme);
        if(other.getRole() == 1)
            confirm.setMessage("你确定要将ta"+OPTIONS_FOR_MANAGER[joinOptions]+"吗？");
        else confirm.setMessage("你确定要将ta"+OPTIONS_FOR_JOINER[joinOptions]+"吗？");
        confirm.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(joinOptions==1) initUpdateJoin();//升降职
                if(joinOptions==2) initDeleteJoin();//开除
            }
        }).show();
    }
    /**
     * “退出”选项确认弹窗
     */
    private void initOutConfirmDialog(){
        AlertDialog.Builder confirm = new AlertDialog.Builder(this,R.style.MyDialogTheme);
        if (self.getRole() != 0) confirm.setMessage("你确定要退出吗？");
        else confirm.setMessage("你确定要解散任务吗？");
        confirm.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (self.getRole() != 0) initDeleteSelfJoin();
                else initDeleteProject();
            }
        }).show();
    }
    /**
     * 任务管理弹窗
     */
    private void initProjectOptionDialog(){
        AlertDialog.Builder projectOption =
                new AlertDialog.Builder(this,R.style.MyDialogTheme);
        projectOption.setItems(OPTIONS_FOR_PROJECT,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0://修改标题
                        initTitleDialog();
                        break;
                    case 1://修改权限
                        initPowerDialog();
                        break;
                    case 2://设置任务位置
                        initPlaceDialog();
                        break;
                    default:
                        break;
                }
            }
        }).show();
    }
    /**
     * 修改标题弹窗
     */
    private void initTitleDialog(){
        editText = new EditText(this);
        AlertDialog.Builder title = new AlertDialog.Builder(this,R.style.MyDialogTheme);
        title.setView(editText);
        title.setTitle("请输入标题");
        title.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                projects.setTitle(editText.getText().toString());
                initUpdateProject();
            }
        }).show();
    }
    /**
     * 修改权限弹窗
     */
    private void initPowerDialog() {
        SET_POWER = new boolean[]{
                power.optBoolean("isFreeJoin"),//是否开放参加
                power.optBoolean("isFreeJudge"),//是否开放审核
                power.optBoolean("isFreeOpen")//是否开放查看
        };
        AlertDialog.Builder powerDialog =
                new AlertDialog.Builder(this, R.style.MyDialogTheme);
        powerDialog.setTitle("权限设置");
        powerDialog.setMultiChoiceItems(POWER, SET_POWER,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    }
                });
        powerDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (self.getRole() == 0) {
                    try {
                        power.put("isFreeJoin", SET_POWER[0]);
                        power.put("isFreeJudge", SET_POWER[1]);
                        power.put("isFreeOpen", SET_POWER[2]);
                        projects.setStruct(power.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    initUpdateProject();
                } else Toast.makeText(JoinedProject.this, "你无权这么做",
                        Toast.LENGTH_SHORT).show();
            }
        }).show();
    }
    /**
     * 任务状态设置弹窗
     */
    private void initPlaceDialog() {
        AlertDialog.Builder placeDialog = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        placeDialog.setTitle("位置设置");
        placeDialog.setItems(SET_PLACE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        setPlace();
                        break;
                    case 1:
                        deletePlace();
                        break;
                    default:
                        break;
                }
            }
        }).show();
    }
    /**
     * 任务状态变化通知弹窗
     */
    private void initShowStatusDialog() {
        if (self.getHasDeleted()) status = 0;
        if (projects.getHasDeleted()) status = 1;
        AlertDialog.Builder showStatusDialog =
                new AlertDialog.Builder(this, R.style.MyDialogTheme);
        showStatusDialog.setMessage(SHOW_STATUS[status]);
        showStatusDialog.setCancelable(false);
        showStatusDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Daos.getInt(JoinedProject.this).deleteSelfJoin(self);
                finish();
            }
        }).show();
    }
    /**
     * 获取首项
     */
    private void getTarget() {
        ProjectItems target = Daos.getInt(this).getTargetByProjectId(self.getProjectsId());
        List<ProjectAdapterItem> itemList = new ArrayList<>();
        ProjectAdapterItem projectAdapterItem = new ProjectAdapterItem(target);
        itemList.add(projectAdapterItem);
        adapter.setList(itemList);
    }
    /**
     * 获取所有的参与者
     */
    private long[] getJoinerList() {
        List<Joins> joinsList = projects.getJoinsList();
        long[] id = new long[joinsList.size()];
        for (int i = 0; i < joinsList.size(); i++)
            if (joinsList.get(i).getRole() != 3)
                id[i] = joinsList.get(i).getJoiner().getId();
        return id;
    }
    /**
     * 邀请别人加入
     */
    private void inviteOther() {
        if (self.getRole() != 2 || power.optBoolean("isFreeJoin")) {
            if (projects.getNumber() < 100) {
                Intent intent = new Intent(this, ChooseSubject.class);
                intent.setAction("inviteOther");
                intent.putExtra("joinerIdList", getJoinerList());
                startActivityForResult(intent, JOINED_PROJECT_REQUEST);
            } else Toast.makeText(this, "该任务已满员", Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, "权限不足,无法操作", Toast.LENGTH_SHORT).show();
    }
    /**
     * 设置任务位置
     */
    private void setPlace() {
        MobclickAgent.onEvent(this, "setPlace");
        progress = new Progress(this);
        progress.setThread(new Runnable() {
            @Override
            public void run() {
                TencentLocation location =
                        TencentLocationManager.getInstance(JoinedProject.this).getLastKnownLocation();
                if (location != null) {
                    Project project = new Project();
                    project.setObjectId(projects.getObjectId());
                    project.setPlace(new BmobGeoPoint(location.getLongitude(), location.getLatitude()));
                    project.setAddress(location.getAddress());
                    try {
                        power.put("isFreeJoin", true);
                        power.put("isFreeOpen", true);
                    } catch (JSONException e) {
                        progress.finishProgress();
                        Toast.makeText(JoinedProject.this,
                                "权限设置出错" + e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                    project.setStruct(power.toString());
                    project.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            progress.finishProgress();
                            if (e == null) Toast.makeText(JoinedProject.this,
                                    "已设置地点", Toast.LENGTH_SHORT).show();
                            else Toast.makeText(JoinedProject.this,
                                    "地点设置失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    progress.finishProgress();
                    Toast.makeText(JoinedProject.this, "无法获取当前地点", Toast.LENGTH_SHORT).show();
                }
            }
        });
        progress.startProgress("正在上传数据，请稍等");
    }
    /**
     * 清除任务位置
     */
    private void deletePlace() {
        MobclickAgent.onEvent(this, "setPlace");
        progress = new Progress(this);
        progress.setThread(new Runnable() {
            @Override
            public void run() {
                Project project = new Project();
                project.setObjectId(projects.getObjectId());
                project.setPlace(new BmobGeoPoint());
                project.setAddress("");
                project.update(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        progress.finishProgress();
                        if (e == null) Toast.makeText(JoinedProject.this,
                                "已清除位置", Toast.LENGTH_SHORT).show();
                        else Toast.makeText(JoinedProject.this,
                                "位置设置失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).startProgress("正在上传数据，请稍等");
    }
    /**
     * 初始化用于邀请他人参加任务的云函数
     */
    private void initSetJoinList(List<Subjects> list) {
        SetJoinListByCloud cloud = new SetJoinListByCloud(
                this,
                list,
                adapter.getItem(0).projectItems
        );
        cloud.setListener(new CloudAsyncsListener() {
            @Override
            public void onSuccess(long[] id) { }
            @Override
            public void onFailed(Exception e) { }
        });
        cloud.setCloudAsc();
    }
    /**
     * 初始化用于更新任务的云函数
     */
    private void initUpdateProject() {
        UpdateProjectByCloud cloud = new UpdateProjectByCloud(this, projects);
        cloud.setListener(new CloudAsyncsListener() {
            @Override
            public void onSuccess(long[] id) { }
            @Override
            public void onFailed(Exception e) { }
        });
        cloud.setUpdateProject();
    }
    /**
     * 初始化用于更新参与者的云函数
     */
    private void initUpdateJoin() {
        UpdateJoinByCloud cloud = new UpdateJoinByCloud(this, other, projects.getObjectId());
        cloud.setListener(new CloudAsyncsListener() {
            @Override
            public void onSuccess(long[] id) { }
            @Override
            public void onFailed(Exception e) { }
        });
        cloud.setCloudAsc();
    }
    /**
     * 初始化用于解散任务的云函数
     */
    private void initDeleteProject() {
        DeleteProjectByCloud cloud = new DeleteProjectByCloud(this, self.getProjects());
        cloud.setListener(new CloudAsyncsListener() {
            @Override
            public void onSuccess(long[] id) {
                status = 1;
                initShowStatusDialog();
            }
            @Override
            public void onFailed(Exception e) { }
        });
        cloud.convertData();
    }
    /**
     * 初始化用于开除参与者的云函数
     */
    private void initDeleteJoin() {
        DeleteJoinByCloud cloud = new DeleteJoinByCloud(this, other);
        cloud.setListener(new CloudAsyncsListener() {
            @Override
            public void onSuccess(long[] id) {
                Toast.makeText(JoinedProject.this,
                        "已开除", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onFailed(Exception e) { }
        });
        cloud.convertData();
    }
    /**
     * 初始化用于退出任务的云函数
     */
    private void initDeleteSelfJoin() {
        DeleteSelfJoinByCloud cloud = new DeleteSelfJoinByCloud(this, self);
        cloud.setListener(new CloudAsyncsListener() {
            @Override
            public void onSuccess(long[] id) {
                status = 0;
                initShowStatusDialog();
            }
            @Override
            public void onFailed(Exception e) { }
        });
        cloud.convertData();
    }
}
