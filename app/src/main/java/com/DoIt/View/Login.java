package com.DoIt.View;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.DoIt.CloudAsyncs.CloudAsyncsListener;
import com.DoIt.CloudAsyncs.GetSubjectByCloud;
import com.DoIt.DoItApplication;
import com.DoIt.Progress;
import com.DoIt.UserUtil;
import com.DoIt.R;
import com.umeng.analytics.MobclickAgent;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;

public class Login extends AppCompatActivity {
    private EditText userName,passWord;
    private LogInListener<BmobUser> logInListener;
    private Progress progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
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
     *初始化页面
     */
    private void initView(){
        userName = findViewById(R.id.name);
        passWord = findViewById(R.id.passWord);
        Button login = findViewById(R.id.login);
        Button signUp = findViewById(R.id.signUp);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到注册界面
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
                finish();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = new Progress(Login.this);
                progress.setThread(new Runnable() {
                    @Override
                    public void run() {
                        UserUtil.login(
                                Login.this,
                                userName.getText().toString(),
                                passWord.getText().toString(),
                                progress,
                                logInListener);
                    }
                }).startProgress("正在登陆，请稍等");
            }
        });
        logInListener = new LogInListener<BmobUser>() {
            @Override
            public void done(BmobUser user, BmobException e) {
                if (e == null) {
                    Toast.makeText(Login.this, "登陆成功", Toast.LENGTH_SHORT).show();
                    //初始化数据库
                    ((DoItApplication) getApplication()).setDatabase(user.getObjectId());
                    //若是初次登陆，数据库里还没有当前用户的subject
                    initGetSubjectByCloud(user.getObjectId());
                } else {
                    progress.finishProgress();
                    Toast.makeText(Login.this,
                            "登陆失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }
    /**
     * 初次登陆后获取用户数据
     * @param userObjectId _user表的objectId
     */
    private void initGetSubjectByCloud(String userObjectId){
        GetSubjectByCloud cloud = new GetSubjectByCloud(this, userObjectId);
        cloud.setListener(new CloudAsyncsListener() {
            @Override
            public void onSuccess(long[] id) {
                jumpToHome();
            }
            @Override
            public void onFailed(Exception e) {
                progress.finishProgress();
            }
        });
        cloud.convertData();
    }
    /**
     * 跳转到主页
     */
    private void jumpToHome() {
        progress.finishProgress();
        //跳转到主页
        Intent intent = new Intent(Login.this, Home.class);
        startActivity(intent);
        finish();
    }
}
