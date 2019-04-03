package com.DoIt.View;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.DoIt.Bmobs;
import com.DoIt.Daos;
import com.DoIt.DoItApplication;
import com.DoIt.Progress;
import com.DoIt.UserUtil;
import com.DoIt.CloudAsyncs.CloudAsyncsListener;
import com.DoIt.CloudAsyncs.SetSubjectByCloud;
import com.DoIt.JavaBean.Subject;
import com.DoIt.R;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class SignUp extends AppCompatActivity {
    private EditText userName, passWord, passWord2, phoneNumber;
    private Progress progress;
    private SaveListener<BmobUser> signUpListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_sign_up);
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
        userName = findViewById(R.id.name);
        passWord = findViewById(R.id.passWord);
        passWord2 = findViewById(R.id.passWord2);
        phoneNumber = findViewById(R.id.phoneNumber);
        Button login = findViewById(R.id.login);
        Button signUp = findViewById(R.id.signUp);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到登陆界面
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
        //调用云函数注册
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = new Progress(SignUp.this);
                progress.setThread(new Runnable() {
                    @Override
                    public void run() {
                        UserUtil.register(
                                SignUp.this,
                                progress,
                                userName.getText().toString(),
                                passWord.getText().toString(),
                                passWord2.getText().toString(),
                                phoneNumber.getText().toString(),
                                signUpListener
                        );
                    }
                }).startProgress("正在注册，请稍等");
            }
        });
        signUpListener = new SaveListener<BmobUser>() {
            @Override
            public void done(BmobUser user, BmobException e) {
                if (e == null) {
                    //初始化数据库
                    ((DoItApplication) getApplication()).setDatabase(user.getObjectId());
                    initSetSubjectByCloud(user);
                } else {
                    progress.finishProgress();
                    Toast.makeText(SignUp.this,
                            "注册失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }
    /**
     * 初始化实体创建云函数
     */
    private void initSetSubjectByCloud(BmobUser user){
        JSONObject subject = new JSONObject();
        try {
            subject.put("userName", userName.getText().toString());
            subject.put("phoneNumber", phoneNumber.getText().toString());
            subject.put("type", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SetSubjectByCloud cloud = new SetSubjectByCloud(
                this,
                user.getObjectId(),
                "我",
                subject
        );
        cloud.setListener(new CloudAsyncsListener() {
            @Override
            public void onSuccess(long[] id) {
                Toast.makeText(SignUp.this, "注册成功", Toast.LENGTH_SHORT).show();
                //跳转到主页面
                if (checkCallingOrSelfPermission(Manifest.permission.READ_CONTACTS)
                        == PackageManager.PERMISSION_GRANTED)
                    getContacts();
                else jumpToHome();
            }
            @Override
            public void onFailed(Exception e) {
                progress.finishProgress();
            }
        });
        cloud.convertData();
    }
    /**
     * 导入手机通讯录联系人
     */
    private void getContacts() {
        ContentResolver reContentResolverol = getContentResolver();
        // 查询就是输入URI等参数,其中URI是必须的,其他是可选的,如果系统能找到URI对应的ContentProvider将返回一个Cursor对象.
        Cursor cursor = reContentResolverol.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null);
        List<String> numbers = new ArrayList<>();
        if (cursor != null) {
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                numbers.add(cursor.getString(cursor.getColumnIndex
                        (ContactsContract.CommonDataKinds.Phone.NUMBER)));
            }
            cursor.close();
            Bmobs.getSubjectListByPhoneNumber(numbers, new Bmobs.Result<List<Subject>>() {
                @Override
                public void onData(List<Subject> subjectList, BmobException e) {
                    if (e == null)
                        for (Subject subject : subjectList)
                            if (!subject.getPhoneNumber().equals(phoneNumber.getText().toString()))
                                Daos.getInt(SignUp.this).setSubjectToDao
                                        (subject, "手机通讯录导入", true);
                    jumpToHome();
                }
            });
        }
    }
    /**
     * 跳转到主页
     */
    private void jumpToHome() {
        progress.finishProgress();
        Intent intent = new Intent(SignUp.this, Home.class);
        startActivity(intent);
        finish();
    }
}
