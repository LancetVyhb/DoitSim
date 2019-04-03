package com.DoIt.View.HomePage;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.DoIt.Bmobs;
import com.DoIt.Daos;
import com.DoIt.Progress;
import com.DoIt.View.FirstPage;
import com.DoIt.View.ManageAccount;
import com.DoIt.View.Record;
import com.DoIt.GreenDaos.Dao.Subjects;
import com.DoIt.JavaBean.AppVersion;
import com.DoIt.R;
import com.umeng.analytics.MobclickAgent;

import java.util.Objects;

import cn.bmob.v3.exception.BmobException;

public class SelfPage extends Fragment implements View.OnClickListener{
    private static final String ABOUT = "感谢您使用“搞事”。“搞事”是一款任务管理应用。通过它，您不仅" +
            "可以管理自己的任务列表，而且还可以查看别人正在进行的任务，发送任务给其他人或者接收其他人发" +
            "送过来的任务，（这是不同于一般的任务管理应用的地方）。由于本应用是由一个二流大学肄业生待业" +
            "期间边学边做开发而成的个人作品，集成了第三方后台服务，且没有经过详细的测试，所以请勿对本应" +
            "用抱有太大的期待。如果您对本应用有什么意见和建议，或者是招聘意向，请发送邮件至" +
            "923512461@qq.com。最后再次感谢您的使用。\n                                ————By柳叶刀";
    private Subjects self;
    private TextView userName, phoneNumber;
    private ImageView head;
    private Progress progress;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_self_page,container,false);
        initView(view);
        return view;
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("SelfPage");
        setData();
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("SelfPage");
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.account://账户管理
                intent = new Intent(getContext(), ManageAccount.class);
                startActivity(intent);
                break;
            case R.id.record://履历
                intent = new Intent(getContext(), Record.class);
                startActivity(intent);
                break;
            case R.id.update://检查版本更新
                checkAppUpdate();
                break;
            case R.id.about://关于
                initAboutDialog();
                break;
            default:
                break;
        }
    }
    /**
     * 初始化页面
     */
    @SuppressLint("SetTextI18n")
    private void initView(View view){
        ConstraintLayout account,record,update,about;

        account = view.findViewById(R.id.account);
        record = view.findViewById(R.id.record);
        update = view.findViewById(R.id.update);
        about = view.findViewById(R.id.about);

        head = view.findViewById(R.id.head);
        userName = view.findViewById(R.id.name);
        phoneNumber = view.findViewById(R.id.phoneNumber);
        //获取当前用户信息
        self = Daos.getInt(getActivity()).getSelf();
        head.setImageResource(R.drawable.head);

        account.setOnClickListener(this);
        record.setOnClickListener(this);
        update.setOnClickListener(this);
        about.setOnClickListener(this);
    }
    /**
     * 初始化“关于”弹窗
     */
    private void initAboutDialog(){
        AlertDialog.Builder about = new AlertDialog.Builder(getContext(),R.style.MyDialogTheme);
        about.setTitle("关于“搞事”");
        about.setMessage(ABOUT);
        about.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        about.show();
    }
    /**
     * 当检测到当前版本为最新时弹出本窗口
     */
    private void initUpdateDialog(boolean hasNew){
        AlertDialog.Builder update = new AlertDialog.Builder(getContext(),R.style.MyDialogTheme);
        if (!hasNew) update.setMessage("当前已是最新版本");
        else update.setMessage("检测到更新，请前往应用市场进行更新");
        update.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        update.show();
    }
    /**
     * 展示账户信息
     */
    @SuppressLint("SetTextI18n")
    private void setData() {
        if (self.getHeadImage() != null)
            Glide.with(Objects.requireNonNull(getView())).load(self.getHeadImage()).into(head);
        userName.setText("用户名：" + self.getUserName());
        phoneNumber.setText("手机号码：" + self.getPhoneNumber());
    }
    /**
     * 检测版本更新
     */
    private void checkAppUpdate() {
        progress = new Progress(getActivity());
        progress.setThread(new Runnable() {
            @Override
            public void run() {
                Bmobs.checkAppUpdate(new Bmobs.Result<AppVersion>() {
                    @Override
                    public void onData(AppVersion appVersion, BmobException e) {
                        progress.finishProgress();
                        if (e == null) {
                            initUpdateDialog(appVersion.getVersion_i() > FirstPage.VERSION_I);
                        } else Toast.makeText(getContext(), "无法检测版本", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).startProgress("正在检查更新，请稍等");
    }
}
