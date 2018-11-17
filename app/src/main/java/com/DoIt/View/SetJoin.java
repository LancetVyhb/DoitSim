package com.DoIt.View;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.DoIt.DaoToJson;
import com.DoIt.Daos;
import com.DoIt.Progress;
import com.DoIt.Adapters.ContentAdapter;
import com.DoIt.CloudAsyncs.CloudAsyncsListener;
import com.DoIt.CloudAsyncs.SetJoinByCloud;
import com.DoIt.GreenDaos.Dao.Projects;
import com.DoIt.Medias.PictureUtil;
import com.DoIt.R;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.io.File;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UploadFileListener;

import static com.DoIt.Adapters.ContentAdapter.IMAGE;
import static com.DoIt.Adapters.ContentAdapter.PROJECT;
import static com.DoIt.Adapters.ContentAdapter.TEXT;
import static com.DoIt.View.ChooseProject.CHOOSE_PROJECT_RESULT;

public class SetJoin extends AppCompatActivity {
    private static final String[] PRIVACY = {
            "所有人可见" ,
            "仅自己可见"
    };
    private static final String[] ADD_ITEM = {
            "文本",
            "图片",
            "子任务"
    };
    public static final int SET_JOIN_REQUEST = 508;
    private ImageView agree, reject;
    private int option = 0, privacy = 0;
    private File file;
    private BmobFile bmobFile;
    private Progress fileProgress;
    private ContentAdapter adapter;
    private int selectedPosition;
    private boolean isAdd;
    private String projectObjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_join);
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
        inflater.inflate(R.menu.set_join_actionbar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.setPrivacy://隐私设置
                initPrivacyDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //创建图像文件做准备
        if (requestCode == SET_JOIN_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            file = PictureUtil.getPictureFile(this, selectedImage);
            if (file != null) {
                fileProgress = new Progress(this);
                fileProgress.setThread(new Runnable() {
                    @Override
                    public void run() {
                        uploadFile();
                    }
                });
                fileProgress.startProgress("正在上传图片，请稍等");
            } else Toast.makeText(this, "文件不能为空", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == SET_JOIN_REQUEST && resultCode == CHOOSE_PROJECT_RESULT && data != null) {
            Projects projects = Daos.getInt(this).getProjects(data.getLongExtra("id", -1));
            JSONObject project = DaoToJson.projectsToJson(projects, false);
            if (project != null) {
                if (isAdd) adapter.addItem(selectedPosition, project.toString(), PROJECT);
                else adapter.rebuildItem(selectedPosition, project.toString());
            }
        }
    }
    /**
     * 初始化页面
     */
    @SuppressLint("SetTextI18n")
    private void initView(){
        projectObjectId = getIntent().getStringExtra("projectObjectId");
        adapter = new ContentAdapter(true, this);

        RecyclerView recycler= findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.addItemDecoration(new DividerItemDecoration
                (this, DividerItemDecoration.VERTICAL));
        recycler.setFocusable(false);
        recycler.setAdapter(adapter);

        agree = findViewById(R.id.agree);
        reject = findViewById(R.id.reject);
        agree.setImageResource(R.drawable.agree);
        reject.setImageResource(R.drawable.reject);
        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (option == 1) {
                    agree.setImageResource(R.drawable.agree);
                    option = 0;
                } else {
                    reject.setImageResource(R.drawable.reject);
                    agree.setImageResource(R.drawable.option_agree);
                    option = 1;
                }
            }
        });
        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (option == 2) {
                    reject.setImageResource(R.drawable.reject);
                    option = 0;
                } else {
                    reject.setImageResource(R.drawable.option_reject);
                    agree.setImageResource(R.drawable.agree);
                    option = 2;
                }
            }
        });
        FloatingActionButton addItem = findViewById(R.id.addItem);
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAdd = true;
                if (adapter.getItemCount() == 0) selectedPosition = 0;
                else selectedPosition = adapter.getItemCount();
                initAddItemDialog();
            }
        });
        ImageView send = findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(SetJoin.this,"setJoin");
                if (adapter.getList().length() < 1000) { initSetJoin();
                } else Toast.makeText(SetJoin.this,
                        "输入内容超出字数限制", Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setListener(new ContentAdapter.EditListener() {
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
        adapter.setList("", this);
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
     * 初始化编辑弹窗
     */
    private void initAddItemDialog() {
        AlertDialog.Builder addItemDialog =
                new AlertDialog.Builder(this, R.style.MyDialogTheme);
        addItemDialog.setItems(ADD_ITEM, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        adapter.addItem(selectedPosition, "", TEXT);
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
        });
        addItemDialog.show();
    }
    /**
     * 打开系统相册
     */
    private void getPicture(){
        if (checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, SET_JOIN_REQUEST);
        } else Toast.makeText(this,
                "应用没有获得权限，无法操作", Toast.LENGTH_SHORT).show();
    }
    /**
     * 打开任务选择页面
     */
    private void getProject(){
        Intent intent = new Intent(this, ChooseProject.class);
        intent.setAction("setContent");
        startActivityForResult(intent, SET_JOIN_REQUEST);
    }
    /**
     * 上传图片
     */
    private void uploadFile() {
        bmobFile = new BmobFile(file);
        bmobFile.uploadblock(new UploadFileListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void done(BmobException e) {
                fileProgress.finishProgress();
                if (e == null) {
                    if (isAdd) adapter.addItem(selectedPosition, bmobFile.getUrl(), IMAGE);
                    else adapter.rebuildItem(selectedPosition, bmobFile.getUrl());
                    Toast.makeText(SetJoin.this,
                            "图片上传成功", Toast.LENGTH_SHORT).show();
                } else Toast.makeText(SetJoin.this,
                        "图片上传失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * 初始化添加join的云函数
     */
    private void initSetJoin() {
        SetJoinByCloud cloud = new SetJoinByCloud(
                SetJoin.this,
                projectObjectId,
                adapter.getList(),
                option,
                privacy
        );
        cloud.setListener(new CloudAsyncsListener() {
            @Override
            public void onSuccess(long[] id) {
                Toast.makeText(SetJoin.this, "已加入任务", Toast.LENGTH_SHORT).show();
                //join添加成功后跳转到相应的project页面
                Intent intent = new Intent(SetJoin.this, JoinedProject.class);
                intent.putExtra("joinId", id[0]);
                startActivity(intent);
                finish();
            }
            @Override
            public void onFailed(Exception e) { }
        });
        cloud.setCloudAsc();
    }
}
