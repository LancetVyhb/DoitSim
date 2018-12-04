package com.DoIt.View;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.DoIt.Bmobs;
import com.DoIt.Daos;
import com.DoIt.GreenDaos.Dao.Projects;
import com.DoIt.JavaBean.AppVersion;
import com.DoIt.R;
import com.DoIt.View.HomePage.NearByProjectList;
import com.DoIt.View.HomePage.SelfPage;
import com.DoIt.View.HomePage.SelfJoinList;
import com.DoIt.View.HomePage.SubjectList;
import com.umeng.analytics.MobclickAgent;

import java.util.Objects;

import cn.bmob.v3.exception.BmobException;
import q.rorbin.badgeview.QBadgeView;

import static com.DoIt.View.ChooseProject.CHOOSE_PROJECT_RESULT;
import static com.DoIt.View.ChooseSubject.CHOOSE_SUBJECT_RESULT;
import static com.DoIt.View.FirstPage.VERSION_I;

public class Home extends AppCompatActivity {
    public static final int HOME_REQUEST = 443;
    private SelfJoinList selfJoinList;
    private SubjectList subjectList;
    private NearByProjectList nearByProjectList;
    private SelfPage selfPage;
    private QBadgeView joinsMessage;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        //设置红点
        registerReceiver(receiver, new IntentFilter("dataPush"));
        joinsMessage.setBadgeNumber(Daos.getInt(this).getJoinNewItem());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        unregisterReceiver(receiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            //添加标签
            if (Objects.equals(data.getAction(), "setTab") && resultCode == CHOOSE_SUBJECT_RESULT ) {
                Daos.getInt(this).updateSubjectsListTab
                        (data.getLongArrayExtra("results"), data.getStringExtra("tab"));
                Toast.makeText(this, "已添加标签", Toast.LENGTH_SHORT).show();
            }
            //移除联系人
            if (Objects.equals(data.getAction(), "deleteSubject") && resultCode == CHOOSE_SUBJECT_RESULT ) {
                Daos.getInt(this).setSubjectShow(data.getLongArrayExtra("results"), false);
                Toast.makeText(this, "已移除联系人", Toast.LENGTH_SHORT).show();
            }
            //为任务设置地点
            if (Objects.equals(data.getAction(), "setPlace") && resultCode == CHOOSE_PROJECT_RESULT ) {
                Projects choseProjects = Daos.getInt(this).
                        getProjects(data.getLongExtra("id", -1));
                nearByProjectList.setPlace(choseProjects);
            }
        }
    }
    /**
     *初始化页面
     */
    private void initView(){
        checkAppUpdate();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();
        FragmentManager fm = getSupportFragmentManager();
        ViewPager viewPager = findViewById(R.id.viewPagers);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        selfJoinList = new SelfJoinList();
        subjectList = new SubjectList();
        nearByProjectList = new NearByProjectList();
        selfPage = new SelfPage();
        FragmentPagerAdapter adapter=new FragmentPagerAdapter(fm) {
            @Override
            public Fragment getItem(int position) {
                switch (position){
                    case 0:
                        return selfJoinList;
                    case 1:
                        return subjectList;
                    case 2:
                        return nearByProjectList;
                    case 3:
                        return selfPage;
                }
                return selfJoinList;
            }
            @Override
            public int getCount() {
                return 4;
            }
        };
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(4);
        tabLayout.setupWithViewPager(viewPager);
        TabLayout.Tab tab1, tab2, tab3, tab4;
        joinsMessage = new QBadgeView(this);
        if ((tab1 = tabLayout.getTabAt(0)) != null){
            tab1.setText("搞事");
            tab1.setIcon(R.drawable.project);
            joinsMessage.bindTarget(tab1.view);
        }
        if ((tab2 = tabLayout.getTabAt(1)) != null){
            tab2.setText("通讯录");
            tab2.setIcon(R.drawable.subjects);
        }
        if ((tab3 = tabLayout.getTabAt(2)) != null){
            tab3.setText("附近");
            tab3.setIcon(R.drawable.map);
        }
        if ((tab4 = tabLayout.getTabAt(3)) != null){
            tab4.setText("我的");
            tab4.setIcon(R.drawable.self);
        }
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                joinsMessage.setBadgeNumber(Daos.getInt(Home.this).getJoinNewItem());
            }
        };
    }
    /**
     * 当检测到强制更新时弹出本窗口
     */
    private void initUpdateDialog() {
        AlertDialog.Builder update = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        update.setCancelable(false);
        update.setMessage("检测到强制更新，请前往应用市场进行更新");
        update.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).show();
    }
    /**
     * 检测版本更新
     */
    private void checkAppUpdate() {
        Bmobs.checkAppUpdate(new Bmobs.Result<AppVersion>() {
            @Override
            public void onData(AppVersion appVersion, BmobException e) {
                if (e == null)
                    if (appVersion.isIsforce() && VERSION_I < appVersion.getVersion_i())
                        initUpdateDialog();
            }
        });
    }
}
