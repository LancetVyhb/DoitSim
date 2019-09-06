package com.DoIt.View;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.DoIt.DoItApplication;
import com.DoIt.Permissions.PermissionListener;
import com.DoIt.Permissions.PermissionPageUtils;
import com.DoIt.Permissions.PermissionUtil;
import com.DoIt.Progress;
import com.DoIt.R;
import com.umeng.analytics.MobclickAgent;

import java.util.List;
import java.util.Objects;

import cn.bmob.v3.BmobUser;

public class FirstPage extends AppCompatActivity {
    public static final String[] PERMISSIONS = {
            Manifest.permission.INTERNET,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.VIBRATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
    };
    public static final Integer VERSION_I = 8;
    public static final String CHANNEL = "baidu";
    private PermissionPageUtils utils;
    private Progress progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_page);
        progress = new Progress(this);
        progress.setThread(new Runnable() {
            @Override
            public void run() {
                checkPermission();
            }
        }).startProgress("正在获取数据，请稍等");
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
     * 进入主页面前的判断和准备
     */
    private void start() {
        Intent intent;
        BmobUser user = BmobUser.getCurrentUser(BmobUser.class);
        //用户未注册登录或因注销跳转而来
        if (user == null || Objects.equals(getIntent().getAction(), "ManageAccount")) {
            //跳转到登陆界面
            intent = new Intent(FirstPage.this, Login.class);
            startActivity(intent);
            finish();
        } else {
            //初始化数据库
            ((DoItApplication)getApplication()).setDatabase(user.getObjectId());
            intent = new Intent(FirstPage.this, Home.class);
            startActivity(intent);
            finish();
        }
    }
    /**
     * 安装时的权限设置与检查
     */
    private void checkPermission() {
        PermissionUtil util = new PermissionUtil(this);
        utils = new PermissionPageUtils(FirstPage.this);
        util.requestPermissions(PERMISSIONS, new PermissionListener() {
            @Override
            public void onGranted() {
                progress.finishProgress();
                start();
            }
            @Override
            public void onDenied(List<String> deniedPermission) {
                initPermissionDialog();
            }
            @Override
            public void onShouldShowRationale(List<String> deniedPermission) {
                initPermissionDialog();
            }
        });
    }
    /**
     * 当有权限被拒绝后弹出弹窗提醒用户跳转到系统权限设置页面，否则退出
     */
    private void initPermissionDialog() {
        progress.finishProgress();
        AlertDialog.Builder permission =
                new AlertDialog.Builder(FirstPage.this, R.style.MyDialogTheme);
        permission.setMessage("应用权限不足。点击“确定”前往权限设置页面，点击“取消”退出应用");
        permission.setCancelable(false);
        permission.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                start();
                utils.jumpPermissionPage();
            }
        });
        permission.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        permission.show();
    }
}
