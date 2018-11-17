package com.DoIt;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.DoIt.GreenDaos.Dao.DaoMaster;
import com.DoIt.GreenDaos.Dao.DaoSession;

import com.DoIt.GreenDaos.MyGreenDaoHelper;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import cn.bmob.push.BmobPush;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobInstallationManager;
import cn.bmob.v3.InstallationListener;
import cn.bmob.v3.exception.BmobException;

public class DoItApplication extends Application{
    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        Bmob.initialize(this, "588210fbd30c8268c5d46e339a3d60c3");//Bmob后台初始化
        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, null);//友盟初始化
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
        startBmobPush();
    }
    /**
     * 初次登陆或注册App后根据用户的objectId创建数据库
     * @param name 传入的_user表的objectId，用于创建数据库
     */
    public void setDatabase(String name) {
        MyGreenDaoHelper helper = new MyGreenDaoHelper(this, name);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster mDaoMaster = new DaoMaster(db);
        daoSession = mDaoMaster.newSession();
        Daos.init(this);
        Daos.getInt(DoItApplication.this).checkJoinsWorkedTime();
        Daos.getInt(DoItApplication.this).resetJoinClickTime();
    }
    /**
     * 使用推送服务时的初始化操作
     */
    public void startBmobPush(){
        BmobInstallationManager.getInstance().initialize(new InstallationListener<BmobInstallation>() {
            @Override
            public void done(BmobInstallation bmobInstallation, BmobException e) { }
        });
        BmobPush.startWork(this);
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
