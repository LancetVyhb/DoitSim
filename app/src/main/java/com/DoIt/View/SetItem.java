package com.DoIt.View;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.DoIt.DaoToJson;
import com.DoIt.Daos;
import com.DoIt.GreenDaos.Dao.Projects;
import com.DoIt.Progress;
import com.DoIt.Adapters.ContentAdapter;
import com.DoIt.CloudAsyncs.CloudAsyncsListener;
import com.DoIt.CloudAsyncs.DeleteProjectItemByCloud;
import com.DoIt.CloudAsyncs.SetProjectItemByCloud;
import com.DoIt.CloudAsyncs.UpdateProjectItemByCloud;
import com.DoIt.GreenDaos.Dao.Joins;
import com.DoIt.GreenDaos.Dao.ProjectItems;
import com.DoIt.Medias.PictureUtil;
import com.DoIt.R;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.io.File;
import java.util.Objects;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UploadFileListener;

import static com.DoIt.Adapters.ContentAdapter.IMAGE;
import static com.DoIt.Adapters.ContentAdapter.PROJECT;
import static com.DoIt.Adapters.ContentAdapter.TEXT;
import static com.DoIt.View.ChooseProject.CHOOSE_PROJECT_RESULT;

public class SetItem extends AppCompatActivity {
    private static final String[] ADD_ITEM = {
            "文本",
            "图片",
            "子任务"
    };
    public static final int SET_ITEM = 509;
    private int option = 0;
    private Intent intent;
    private ProjectItems projectItems;
    private Joins self;
    private ImageView agree, reject;
    private File file;
    private BmobFile bmobFile;
    private Progress fileProgress;
    private ContentAdapter adapter;
    private int selectedPosition;
    private boolean isAdd;

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
        //删除按钮，只有projectItem是底项的时候才会显示并被使用
        if (Objects.equals(intent.getAction(), "update") && projectItems.getType() == 2) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.set_item_actionbar_menu, menu);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete)
            initDeleteProjectItemCloudAsc();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //创建图像文件做准备
        if (requestCode == SET_ITEM && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            file = PictureUtil.getPictureFile(SetItem.this, selectedImage);
            if (file != null) {
                fileProgress = new Progress(SetItem.this);
                fileProgress.setThread(new Runnable() {
                    @Override
                    public void run() {
                        uploadFile();
                    }
                }).startProgress("正在上传图片，请稍等");
            } else Toast.makeText(this, "文件不能为空", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == SET_ITEM && resultCode == CHOOSE_PROJECT_RESULT && data != null) {
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
    private void initView() {
        intent = getIntent();
        //当操作方式为set时projectItems是将要添加的projectItem的parent，update时projectItems就是要更新的数据本身
        projectItems = Daos.getInt(this).getProjectItems
                (intent.getLongExtra("projectItemsId", -1));
        self = Daos.getInt(this).getJoins
                (intent.getLongExtra("joinsId", -1));
        adapter = new ContentAdapter(true,this);

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.addItemDecoration(new DividerItemDecoration
                (this, DividerItemDecoration.VERTICAL));
        recycler.setFocusable(false);
        recycler.setAdapter(adapter);

        ImageView send = findViewById(R.id.send);
        FloatingActionButton addItem = findViewById(R.id.addItem);
        LinearLayout options = findViewById(R.id.option);
        agree = findViewById(R.id.agree);
        reject = findViewById(R.id.reject);
        //update时需要加载projectItem原本的数据，包括option和content
        if (Objects.equals(intent.getAction(), "update")) {
            if (projectItems.getContent() != null)
                adapter.setList(projectItems.getContent(), this);
            else adapter.setList("", this);
            if (projectItems.getType() != 0) {
                options.setVisibility(View.VISIBLE);
                option = projectItems.getOption();
                if (option == 1) {
                    agree.setImageResource(R.drawable.option_agree);
                    reject.setImageResource(R.drawable.reject);
                }
                if (option == 2) {
                    agree.setImageResource(R.drawable.agree);
                    reject.setImageResource(R.drawable.option_reject);
                }
                if (option == 0 || option == 3){
                    agree.setImageResource(R.drawable.agree);
                    reject.setImageResource(R.drawable.reject);
                }
            } else options.setVisibility(View.GONE);
        } else {
            options.setVisibility(View.VISIBLE);
            adapter.setList("", this);
            option = 0;
            agree.setImageResource(R.drawable.agree);
            reject.setImageResource(R.drawable.reject);
        }
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
        //“发表”按钮点击事件
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAdd = true;
                if (adapter.getItemCount() == 0) selectedPosition = 0;
                else selectedPosition = adapter.getItemCount();
                initAddItemDialog();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.getList().length() < 1000) {
                    MobclickAgent.onEvent(SetItem.this, "setOrUpdateItem");
                    if (option == 3) option = 0;//“未回应”在用户回应后变为“已回应”
                    else MobclickAgent.onEvent(SetItem.this, "updateOption");
                    if (Objects.equals(intent.getAction(), "set"))
                        initSetProjectItemCloudAsc();
                    if (Objects.equals(intent.getAction(), "update"))
                        initUpdateProjectItemCloudAsc();
                } else Toast.makeText(SetItem.this,
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
    }
    /**
     * 初始化编辑弹窗
     */
    private void initAddItemDialog() {
        AlertDialog.Builder addItemDialog =
                new AlertDialog.Builder(SetItem.this, R.style.MyDialogTheme);
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
            startActivityForResult(intent, SET_ITEM);
        } else Toast.makeText(SetItem.this,
                "应用没有获得权限，无法操作", Toast.LENGTH_SHORT).show();
    }
    /**
     * 打开任务选择页面
     */
    private void getProject(){
        Intent intent = new Intent(SetItem.this, ChooseProject.class);
        intent.setAction("setContent");
        startActivityForResult(intent, SET_ITEM);
    }
    /**
     * 上传图片
     */
    private void uploadFile() {
        bmobFile = new BmobFile(file);
        bmobFile.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                fileProgress.finishProgress();
                if (e == null) {
                    if (isAdd) adapter.addItem(selectedPosition, bmobFile.getUrl(), IMAGE);
                    else adapter.rebuildItem(selectedPosition, bmobFile.getUrl());
                    Toast.makeText(SetItem.this,
                            "图片上传成功", Toast.LENGTH_SHORT).show();
                } else Toast.makeText(SetItem.this,
                        "图片上传失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * 初始化添加projectItem云函数
     */
    private void initSetProjectItemCloudAsc() {
        SetProjectItemByCloud cloud = new SetProjectItemByCloud(
                SetItem.this,
                adapter.getList(),
                option,
                self,
                projectItems
        );
        cloud.setListener(new CloudAsyncsListener() {
            @Override
            public void onSuccess(long id[]) {
                Intent intent = new Intent(SetItem.this, JoinedProject.class);
                setResult(SET_ITEM, intent);
                finish();
            }
            @Override
            public void onFailed(Exception e) { }
        });
        cloud.convertData();
    }
    /**
     * 初始化更新projectItem云函数
     */
    private void initUpdateProjectItemCloudAsc() {
        UpdateProjectItemByCloud cloud = new UpdateProjectItemByCloud(
                SetItem.this,
                projectItems.getProjects().getObjectId(),
                adapter.getList(),
                option,
                projectItems
        );
        cloud.setListener(new CloudAsyncsListener() {
            @Override
            public void onSuccess(long id[]) {
                Intent intent = new Intent(SetItem.this, JoinedProject.class);
                setResult(SET_ITEM, intent);
                finish();
            }
            @Override
            public void onFailed(Exception e) { }
        });
        cloud.convertData();
    }
    /**
     * 初始化删除projectItem云函数
     */
    private void initDeleteProjectItemCloudAsc() {
        DeleteProjectItemByCloud cloud = new DeleteProjectItemByCloud(
                SetItem.this,
                projectItems
        );
        cloud.setListener(new CloudAsyncsListener() {
            @Override
            public void onSuccess(long[] id) {
                Toast.makeText(SetItem.this, "已删除", Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override
            public void onFailed(Exception e) { }
        });
        cloud.convertData();
    }
}
