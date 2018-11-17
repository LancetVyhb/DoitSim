package com.DoIt.View;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.DoIt.Adapters.JoinerListAdapter;
import com.DoIt.DaoToJson;
import com.DoIt.Daos;
import com.DoIt.GetLocations;
import com.DoIt.Progress;
import com.DoIt.Adapters.ContentAdapter;
import com.DoIt.CloudAsyncs.CloudAsyncsListener;
import com.DoIt.CloudAsyncs.SetProjectByCloud;
import com.DoIt.GreenDaos.Dao.Projects;
import com.DoIt.GreenDaos.Dao.Subjects;
import com.DoIt.JavaBean.Project;
import com.DoIt.Medias.PictureUtil;
import com.DoIt.R;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

import static com.DoIt.Adapters.ContentAdapter.IMAGE;
import static com.DoIt.Adapters.ContentAdapter.PROJECT;
import static com.DoIt.Adapters.ContentAdapter.TEXT;
import static com.DoIt.View.ChooseSubject.CHOOSE_SUBJECT_RESULT;
import static com.DoIt.View.ChooseProject.CHOOSE_PROJECT_RESULT;

public class SetProject extends AppCompatActivity {
    public static final int SET_PROJECT_REQUEST = 206;
    public static final int SET_PROJECT_RESULT = 299;
    private static final String[] PRIVACY = {
            "所有人可见" ,
            "仅自己可见"
    };
    private static final String[] POWER = {
            "是否开放参加",
            "是否开放审核",
            "是否开放浏览"
    };
    private static final String[] ADD_ITEM = {
            "文本",
            "图片",
            "子任务"
    };
    private boolean SET_POWER[] = {false, false, true};
    private Location location;
    private EditText title;
    private TextView joinerListState;
    private List<Subjects> joinerList;
    private Integer privacy = 0;
    private JSONObject power;
    private File file;
    private BmobFile bmobFile;
    private Progress progress;
    private JoinerListAdapter joinerListAdapter;
    private ContentAdapter contentAdapter;
    private int selectedPosition;
    private boolean isAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_project);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.set_project_actionbar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.setPrivacy://隐私设置
                initPrivacyDialog();
                break;
            case R.id.setPower://权限设置
                initPowerDialog();
                break;
            case R.id.setPlace://位置设置
                setLocation();
                break;
        //    case R.id.setTheme://主题设置
         //       setTheme();
         //       break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SET_PROJECT_REQUEST && data != null) {
            //邀请他人返回的结果处理
            if (resultCode == CHOOSE_SUBJECT_RESULT) {
                long[] id = data.getLongArrayExtra("results");
                joinerList.clear();
                joinerList.add(Daos.getInt(this).getSelf());
                for (long anId : id) joinerList.add(Daos.getInt(this).getSubjects(anId));
                joinerListState.setText("已有" + (joinerList.size()) + "人参与");
                joinerListAdapter.setJoinerList(joinerList);
            }
            //选取图片返回结果
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = data.getData();
                file = PictureUtil.getPictureFile(this, selectedImage);
                if (file != null) uploadFile();
                else Toast.makeText(this, "文件不能为空", Toast.LENGTH_SHORT).show();
            }
            //选取子任务返回结果
            if (resultCode == CHOOSE_PROJECT_RESULT) {
                Projects projects = Daos.getInt(this).getProjects(data.getLongExtra("id", -1));
                JSONObject project = DaoToJson.projectsToJson(projects, false);
                if (project != null) {
                    if (isAdd) contentAdapter.addItem(selectedPosition, project.toString(), PROJECT);
                    else contentAdapter.rebuildItem(selectedPosition, project.toString());
                }
            }
        }
    }
    /**
     * 初始化页面
     */
    @SuppressLint("SetTextI18n")
    private void initView() {
        power = new JSONObject();
        joinerList = new ArrayList<>();
        title = findViewById(R.id.message);
        joinerListState = findViewById(R.id.joinerListState);
        joinerListAdapter = new JoinerListAdapter();
        contentAdapter = new ContentAdapter(true, this);
        //议程
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.addItemDecoration(new DividerItemDecoration
                (this, DividerItemDecoration.VERTICAL));
        recycler.setFocusable(false);
        recycler.setAdapter(contentAdapter);
        //被邀请人
        RecyclerView joiners = findViewById(R.id.joinerList);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        joiners.setLayoutManager(manager);
        joiners.setFocusable(false);
        joiners.setAdapter(joinerListAdapter);

        ImageView invite = findViewById(R.id.invite);
        ImageView send = findViewById(R.id.send);
        FloatingActionButton addItem = findViewById(R.id.addItem);
        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (joinerList.size() <= 100) {
                    //邀请他人时把已经被邀请的的人也传输过去
                    Intent intent = new Intent(SetProject.this, ChooseSubject.class);
                    intent.setAction("setJoinList");
                    long[] self = new long[1];
                    self[0] = joinerList.get(0).getId();
                    intent.putExtra("joinerIdList", self);
                    if (joinerList.size() != 1) {
                        long[] joinerIdList = new long[joinerList.size() - 1];
                        for (int i = 1; i < joinerList.size(); i++)
                            joinerIdList[i - 1] = joinerList.get(i).getId();
                        intent.putExtra("subjectIdList", joinerIdList);
                    }
                    startActivityForResult(intent, SET_PROJECT_REQUEST);
                } else Toast.makeText(SetProject.this,
                        "该任务已满员", Toast.LENGTH_SHORT).show();
            }
        });
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAdd = true;
                if (contentAdapter.getItemCount() == 0) selectedPosition = 0;
                else selectedPosition = contentAdapter.getItemCount();
                initAddItemDialog();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断输入是否为空
                if (!contentAdapter.getList().equals("")
                        && !title.getText().toString().equals("")) {
                    //判断输入是否超出限制
                    if (title.length() < 20 && contentAdapter.getList().length() < 1000)
                        initCloudAsync();
                    else Toast.makeText(SetProject.this,
                            "输入内容超过字数限制", Toast.LENGTH_SHORT).show();
                } else Toast.makeText(SetProject.this,
                        "请输入必要信息", Toast.LENGTH_SHORT).show();
            }
        });
        joinerList.add(Daos.getInt(this).getSelf());
        //从otherSubject页面跳转过来的情况下要加那个人到被邀请的行列
        if (Objects.equals(getIntent().getAction(), "inviteOther"))
            joinerList.add(Daos.getInt(this).checkSubjectsExist
                    (getIntent().getStringExtra("subjectObjectId")));
        //从nearProjectList页面跳转而来的情况下要设置地点
        if (Objects.equals(getIntent().getAction(), "setPlace"))
            setLocation();

        joinerListState.setText("已有" + Integer.toString(joinerList.size()) + "人参加");
        try {
            power.put("isFreeJoin", SET_POWER[0]);
            power.put("isFreeJudge", SET_POWER[1]);
            power.put("isFreeOpen", SET_POWER[2]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        contentAdapter.setListener(new ContentAdapter.EditListener() {
            @Override
            public void onAddItem(View v, int position) {
                isAdd = true;
                selectedPosition = position;
                initAddItemDialog();
            }
            @Override
            public void onImageRebuild(View v, int position) {
                isAdd = false;
                selectedPosition = position;
                getPicture();
            }
            @Override
            public void onProjectRebuild(View v, int position) {
                isAdd = false;
                selectedPosition = position;
                getProject();
            }
        });
        joinerListAdapter.setListener(new JoinerListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Subjects subjects) {
                if (subjects != Daos.getInt(SetProject.this).getSelf()) {
                    joinerList.remove(subjects);
                    joinerListState.setText("已有" + Integer.toString(joinerList.size()) + "人参加");
                    joinerListAdapter.removeItem(subjects);
                    Toast.makeText(SetProject.this, "已移除"
                            + subjects.getUserName(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        joinerListAdapter.setJoinerList(joinerList);
        contentAdapter.setList("", this);
    }
    /**
     * 初始化编辑弹窗
     */
    private void initAddItemDialog() {
        AlertDialog.Builder addItemDialog =
                new AlertDialog.Builder(SetProject.this, R.style.MyDialogTheme);
        addItemDialog.setItems(ADD_ITEM, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        contentAdapter.addItem(selectedPosition, "", TEXT);
                        break;
                    case 1:
                        getPicture();
                        break;
                    case 2:
                        getProject();
                        break;
                    default:
                        break;
                }
            }
        }).show();
    }
    /**
     * 初始化隐私设置弹窗
     */
    private void initPrivacyDialog(){
        AlertDialog.Builder privacyDialog =
                new AlertDialog.Builder(this, R.style.MyDialogTheme);
        privacyDialog.setTitle("隐私设置");
        privacyDialog.setSingleChoiceItems(PRIVACY, privacy,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        privacy = which;
                    }
                });
        privacyDialog.setPositiveButton("确定",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        }).show();
    }
    /**
     * 初始化权限设置弹窗
     */
    private void initPowerDialog(){
        AlertDialog.Builder powerDialog =
                new AlertDialog.Builder(SetProject.this, R.style.MyDialogTheme);
        powerDialog.setTitle("权限设置");
        powerDialog.setMultiChoiceItems(POWER, SET_POWER,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) { }
                });
        powerDialog.setPositiveButton("确定",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    power.put("isFreeJoin",SET_POWER[0]);
                    power.put("isFreeJudge",SET_POWER[1]);
                    power.put("isFreeOpen",SET_POWER[2]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).show();
    }
    /**
     * 获取并保存当前位置
     */
    private void setLocation() {
        SET_POWER[0] = true;
        SET_POWER[2] = true;
        location = GetLocations.getLocation(this);
        Toast.makeText(SetProject.this, "已设置地点", Toast.LENGTH_SHORT).show();
    }
    /**
     * 打开系统相册
     */
    private void getPicture(){
        if (checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, SET_PROJECT_REQUEST);
        } else Toast.makeText(SetProject.this,
                "应用没有获得权限，无法操作", Toast.LENGTH_SHORT).show();
    }
    /**
     * 打开任务选择页面
     */
    private void getProject(){
        Intent intent = new Intent(SetProject.this, ChooseProject.class);
        intent.setAction("setContent");
        startActivityForResult(intent, SET_PROJECT_REQUEST);
    }
    /**
     * 上传图片
     */
    private void uploadFile() {
        progress = new Progress(this);
        progress.setThread(new Runnable() {
            @Override
            public void run() {
                bmobFile = new BmobFile(file);
                bmobFile.uploadblock(new UploadFileListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void done(BmobException e) {
                        progress.finishProgress();
                        if (e == null) {
                            if (isAdd) contentAdapter.addItem(selectedPosition, bmobFile.getUrl(), IMAGE);
                            else contentAdapter.rebuildItem(selectedPosition, bmobFile.getUrl());
                            Toast.makeText(SetProject.this,
                                    "图片上传成功", Toast.LENGTH_SHORT).show();
                        } else Toast.makeText(SetProject.this,
                                "图片上传失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).startProgress("正在上传图片，请稍等");
    }
    /**
     * 初始化任务创建云函数
     */
    private void initCloudAsync() {
        SetProjectByCloud cloud = new SetProjectByCloud(
                SetProject.this,
                joinerList,
                power,
                contentAdapter.getList(),
                title.getText().toString(),
                privacy
        );
        cloud.setListener(new CloudAsyncsListener() {
            @Override
            public void onSuccess(long id[]) {
                //任务创建成功后跳转至任务页面
                if (location != null) updateProjectPlace(id[0]);
                else jumpToJoinedProject(id[0]);
            }
            @Override
            public void onFailed(Exception e) { }
        });
        cloud.convertData();
    }
    /**
     * 设置任务位置
     */
    private void updateProjectPlace(final long id){
        MobclickAgent.onEvent(this,"setPlace");
        progress = new Progress(this);
        progress.setThread(new Runnable() {
            @Override
            public void run() {
                Project project = new Project();
                project.setObjectId(Daos.getInt(SetProject.this).getJoins(id).getProjects().getObjectId());
                project.setPlace(new BmobGeoPoint(location.getLongitude(),location.getLatitude()));
                project.update(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        progress.finishProgress();
                        jumpToJoinedProject(id);
                    }
                });
            }
        }).startProgress("正在上传数据，请稍等");
    }
    /**
     * 跳转到任务页面
     */
    private void jumpToJoinedProject(long id){
        if (Objects.equals(getIntent().getAction(), "setPlace"))
            setResult(SET_PROJECT_RESULT);
        else {
            Intent intent = new Intent(SetProject.this, JoinedProject.class);
            intent.putExtra("joinId", id);
            startActivity(intent);
        }
        finish();
    }
}
