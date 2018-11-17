package com.DoIt.View;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.DoIt.Bmobs;
import com.DoIt.Daos;
import com.DoIt.DoItApplication;
import com.DoIt.GreenDaos.Dao.Subjects;
import com.DoIt.Progress;
import com.DoIt.UserUtil;
import com.DoIt.JavaBean.Join;
import com.DoIt.JavaBean.ProjectItem;
import com.DoIt.JavaBean.Subject;
import com.DoIt.R;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobInstallationManager;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.InstallationListener;
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
                Intent intent = new Intent(Login.this,SignUp.class);
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
                    Subjects self;
                    if ((self = Daos.getInt(Login.this).getSelf()) == null)
                        getSelfSubject(user);
                    else getJoinList(self.getObjectId());
                } else {
                    progress.finishProgress();
                    Toast.makeText(Login.this,
                            "登陆失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }
    /**
     *初次登陆后获取subject
     * @param user _user表
     */
    private void getSelfSubject(BmobUser user){
        Bmobs.getSubjectByUserObjectId(user, new Bmobs.Result<List<Subject>>() {
            @Override
            public void onData(List<Subject> list, BmobException e) {
                if (e == null) {
                    //存储当前用户的subject
                    Daos.getInt(Login.this).setSubjectToDao(list.get(0), "我", true);
                    getJoinList(list.get(0).getObjectId());
                } else {
                    progress.finishProgress();
                    Toast.makeText(Login.this,
                            "获取身份失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /**
     *初次登陆后获取任务列表
     * @param subjectObjectId Subject的objectId
     */
    private void getJoinList(String subjectObjectId){
        Bmobs.getSelfJoinList(subjectObjectId, new Bmobs.Result<List<Join>>() {
            @Override
            public void onData(List<Join> joinList, BmobException e) {
                if (e == null){
                    Daos.getInt(Login.this).setOrUpdateJoinListToDao(joinList);
                    if (joinList.size() == 0) subscribeChannels();
                    else getProjectItemList(joinList);
                } else {
                    progress.finishProgress();
                    Toast.makeText(Login.this,
                            "获取任务列表失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /**
     *初次登陆后获取任务列表中的任务内容
     * @param joinList 任务列表
     */
    private void getProjectItemList(List<Join> joinList){
        Bmobs.getProjectItemListByJoinList(joinList, new Bmobs.Result<List<ProjectItem>>() {
            @Override
            public void onData(List<ProjectItem> itemList, BmobException e) {
                if (e == null) {
                    Daos.getInt(Login.this).divideProjectItemList(itemList);
                    subscribeChannels();
                } else {
                    progress.finishProgress();
                    Toast.makeText(Login.this,
                            "获取任务列表内容失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /**
     * 登陆账户后订阅频道
     */
    private void subscribeChannels() {
        BmobInstallationManager.getInstance().subscribe(Daos.getInt(this).getAllChannels(),
                new InstallationListener<BmobInstallation>() {
            @Override
            public void done(BmobInstallation bmobInstallation, BmobException e) {
                if (e == null) jumpToHome();
                else {
                    progress.finishProgress();
                    Toast.makeText(Login.this,
                            "订阅频道失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
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
