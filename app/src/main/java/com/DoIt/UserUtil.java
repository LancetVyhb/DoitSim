package com.DoIt;

import android.app.Activity;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.SaveListener;

public class UserUtil {
    /**
     *判断密码格式是否正确
     * @param passWord 密码
     */
    private static boolean checkPassWord(String passWord){
        Pattern p = Pattern.compile("^[a-z0-9_-]{6,18}$");
        Matcher m = p.matcher(passWord);
        return m.find();
    }
    /**
     *判断用户名格式是否正确
     * @param userName 用户名
     */
    public static boolean checkName(String userName){
        Pattern p = Pattern.compile("^[0-9a-zA-Z\u4e00-\u9fa5_]{2,16}$");
        Matcher m = p.matcher(userName);
        return m.find();
    }
    /**
     *判断手机号码格式是否正确
     * @param phoneNumber 手机号码
     */
    public static boolean checkPhoneNumber(String phoneNumber){
        Pattern p = Pattern.compile
                ("^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\d{8}$");
        Matcher m = p.matcher(phoneNumber);
        return m.find();
    }
    /**
     *注册信息检查
     * @param activity 调用该方法的上下文
     * @param userName 用户名
     * @param passWord 密码
     * @param passWord2 确认密码
     * @param phoneNumber 手机号码
     * @param signUpListener 注册回调
     */
    public static void register(
            Activity activity,
            Progress progress,
            String userName,
            String passWord,
            String passWord2,
            String phoneNumber,
            SaveListener<BmobUser> signUpListener
    ) {
        if (passWord.equals("") || passWord2.equals("") || userName.equals("") || phoneNumber.equals("")) {
            progress.finishProgress();
            Toast.makeText(activity, "请输入注册信息", Toast.LENGTH_SHORT).show();
        } else {
            if (!checkPassWord(passWord)) {
                progress.finishProgress();
                Toast.makeText(activity, "密码格式错误", Toast.LENGTH_SHORT).show();
            } else {
                if (!passWord.equals(passWord2)) {
                    progress.finishProgress();
                    Toast.makeText(activity, "确认密码错误", Toast.LENGTH_SHORT).show();
                } else {
                    if (!checkName(userName)) {
                        progress.finishProgress();
                        Toast.makeText(activity, "用户名格式错误", Toast.LENGTH_SHORT).show();
                    } else {
                        if (!checkPhoneNumber(phoneNumber)) {
                            progress.finishProgress();
                            Toast.makeText(activity, "手机号码错误", Toast.LENGTH_SHORT).show();
                        } else signUp(userName, passWord, phoneNumber, signUpListener);
                    }
                }
            }
        }
    }
    /**
     *登陆
     * @param activity 调用该方法的上下文
     * @param userName 用户名或手机号码
     * @param passWord 密码
     * @param loginListener 登陆回调
     */
    public static void login(
            Activity activity,
            String userName,
            String passWord,
            Progress progress,
            LogInListener<BmobUser> loginListener
    ) {
        if (!userName.equals("") && !passWord.equals(""))
            BmobUser.loginByAccount(userName, passWord, loginListener);
        else {
            progress.finishProgress();
            Toast.makeText(activity, "请输入必要信息",Toast.LENGTH_SHORT).show();
        }
    }
    /**
     *注册
     * @param userName 用户名
     * @param passWord 密码
     * @param phoneNumber 手机号码
     * @param signUpListener 注册回调
     */
    public static void signUp(String userName,
                              String passWord,
                              String phoneNumber,
                              SaveListener<BmobUser> signUpListener){
        BmobUser user = new BmobUser();
        user.setUsername(userName);
        user.setPassword(passWord);
        user.setMobilePhoneNumber(phoneNumber);
        user.signUp(signUpListener);
    }
}
