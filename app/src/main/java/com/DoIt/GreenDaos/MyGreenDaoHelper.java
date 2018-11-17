package com.DoIt.GreenDaos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.DoIt.GreenDaos.Dao.DaoMaster;
import com.DoIt.GreenDaos.Dao.JoinsDao;
import com.wenld.greendaoupgradehelper.DBMigrationHelper;

public class MyGreenDaoHelper extends DaoMaster.OpenHelper{
    public MyGreenDaoHelper(Context context, String name) {
        super(context, name);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //把需要管理的数据库表DAO作为最后一个参数传入到方法中
        if (oldVersion < newVersion) {
            try {
                DBMigrationHelper migratorHelper = new DBMigrationHelper();
                migratorHelper.onUpgrade(db, JoinsDao.class);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    }
}
