package com.DoIt.View;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.DoIt.Daos;
import com.DoIt.Progress;
import com.DoIt.UserUtil;
import com.DoIt.GreenDaos.Dao.Subjects;
import com.DoIt.JavaBean.Subject;
import com.DoIt.Medias.PictureUtil;
import com.DoIt.R;
import com.umeng.analytics.MobclickAgent;

import java.io.File;

import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobInstallationManager;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.InstallationListener;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

public class ManageAccount extends AppCompatActivity {
    public static final int HEAD_IMAGE_SELSCT=104;
    public static final int HEAD_IMAGE_CROP=105;
    private Progress progress;
    private EditText editText;
    private TextView userName, phoneNumber;
    private ImageView head;
    private File file;
    private BmobFile bmobFile;
    private Subject self;
    private BmobUser user;
    private int option;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account);
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
        //创建图像文件做准备
        file = new File(ManageAccount.this.getExternalFilesDir
                (Environment.DIRECTORY_PICTURES), self.getObjectId() + "_head.jpeg");
        //打开系统自带的图片剪切功能将用户选择的图像剪切为头像
        if (requestCode == HEAD_IMAGE_SELSCT && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            startActivityForResult(PictureUtil.photoClip(file, selectedImage), HEAD_IMAGE_CROP);
        }
        //将系统传回来的剪切好的头像文件上传
        if (requestCode == HEAD_IMAGE_CROP && resultCode == Activity.RESULT_OK && data != null) {
            progress = new Progress(ManageAccount.this);
            progress.setThread(new Runnable() {
                @Override
                public void run() {
                    uploadHeadImage();
                }
            }).startProgress("正在上传数据，请稍等");
        }
    }
    /**
     * 初始化页面
     */
    private void initView() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setTitle("我的账户");
        LinearLayout out, headOption, userNameOption, phoneNumberOption;
        out = findViewById(R.id.delete);
        headOption = findViewById(R.id.headOption);
        userNameOption = findViewById(R.id.userNameOption);
        phoneNumberOption = findViewById(R.id.phoneNumberOption);
        userName = findViewById(R.id.name);
        phoneNumber = findViewById(R.id.phoneNumber);
        head = findViewById(R.id.head);
        head.setImageResource(R.drawable.head);
        //获取当前用户的数据
        self = new Subject();
        user = new BmobUser();
        Subjects selfs = Daos.getInt(this).getSelf();
        userName.setText(selfs.getUserName());
        phoneNumber.setText(selfs.getPhoneNumber());
        if (selfs.getHeadImage() != null)
            Glide.with(ManageAccount.this).load(selfs.getHeadImage()).into(head);
        self.setObjectId(Daos.getInt(this).getSelf().getObjectId());
        //“注销”
        out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = new Progress(ManageAccount.this);
                progress.setThread(new Runnable() {
                    @Override
                    public void run() {
                        unsubscribeChannels();
                    }
                }).startProgress("正在注销，请稍等");
            }
        });
        //修改头像
        headOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断是否拥有相应权限
                if (checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    option = 0;
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, HEAD_IMAGE_SELSCT);
                } else Toast.makeText(ManageAccount.this,
                        "应用没有获得权限，无法操作", Toast.LENGTH_SHORT).show();
            }
        });
        //修改用户名
        userNameOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = new Progress(ManageAccount.this);
                progress.setThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUser();
                    }
                });
                option = 1;
                setDialog();
            }
        });
        //修改手机号吗
        phoneNumberOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = new Progress(ManageAccount.this);
                progress.setThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUser();
                    }
                });
                option = 2;
                setDialog();
            }
        });
    }
    /**
     * 弹出输入修改数据用的弹窗
     */
    private void setDialog() {
        editText = new EditText(ManageAccount.this);
        AlertDialog.Builder setText =
                new AlertDialog.Builder(ManageAccount.this, R.style.MyDialogTheme);
        setText.setView(editText);
        setText.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //判断输入是否为空
                if (!editText.getText().toString().equals("")) {
                    switch (option) {
                        case 1://修改用户名，先检查输入的用户名格式是否正确
                            if (UserUtil.checkName(editText.getText().toString())) {
                                self.setUserName(editText.getText().toString());
                                user.setUsername(editText.getText().toString());
                                progress.startProgress("正在上传数据，请稍等");
                            } else Toast.makeText(ManageAccount.this,
                                    "格式不正确", Toast.LENGTH_SHORT).show();
                            break;
                        case 2://修改手机号码，先检查输入的手机号码格式是否正确
                            if (UserUtil.checkPhoneNumber(editText.getText().toString())) {
                                self.setPhoneNumber(editText.getText().toString());
                                user.setMobilePhoneNumber(editText.getText().toString());
                                progress.startProgress("正在上传数据，请稍等");
                            } else Toast.makeText(ManageAccount.this,
                                    "格式不正确", Toast.LENGTH_SHORT).show();
                            break;
                    }
                } else Toast.makeText(ManageAccount.this, "请输入信息",
                        Toast.LENGTH_SHORT).show();
            }
        }).show();
    }
    /**
     * 上传头像
     */
    private void uploadHeadImage() {
        bmobFile = new BmobFile(file);
        bmobFile.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    Toast.makeText(ManageAccount.this,
                            "图片上传成功", Toast.LENGTH_SHORT).show();
                    Glide.with(ManageAccount.this).load(bmobFile.getUrl()).into(head);
                    self.setHeadImage(bmobFile.getUrl());
                    updateSubject();
                } else {
                    progress.finishProgress();
                    Toast.makeText(ManageAccount.this,
                            "图片上传失败" + e.getErrorCode(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /**
     * 更新用户信息
     */
    private void updateUser() {
        user.update(BmobUser.getCurrentUser(BmobUser.class).getObjectId(), new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) updateSubject();
                else {
                    progress.finishProgress();
                    Toast.makeText(ManageAccount.this,
                            "账户更新失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /**
     * 更新用户信息
     */
    private void updateSubject() {
        self.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                progress.finishProgress();
                if (e == null) {
                    Daos.getInt(ManageAccount.this).updateSubjectToDao(self);
                    userName.setText(Daos.getInt(ManageAccount.this).getSelf().getUserName());
                    phoneNumber.setText(Daos.getInt(ManageAccount.this).getSelf().getPhoneNumber());
                    Toast.makeText(ManageAccount.this,
                            "账户更新成功", Toast.LENGTH_SHORT).show();
                } else Toast.makeText(ManageAccount.this,
                        "账户更新失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * 注销账户后退订频道
     */
    private void unsubscribeChannels(){
        BmobInstallationManager.getInstance().unsubscribe(Daos.getInt(this).getAllChannels() ,
                new InstallationListener<BmobInstallation>() {
            @Override
            public void done(BmobInstallation bmobInstallation, BmobException e) {
                progress.finishProgress();
                if (e == null) {
                    Toast.makeText(ManageAccount.this, "已成功注销", Toast.LENGTH_SHORT).show();
                    BmobUser.logOut();
                    Intent intent = new Intent(ManageAccount.this, FirstPage.class);
                    intent.setAction("ManageAccount");
                    startActivity(intent);
                    finish();
                } else Toast.makeText(ManageAccount.this,
                            "注销失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
