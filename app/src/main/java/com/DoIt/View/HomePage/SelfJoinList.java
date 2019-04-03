package com.DoIt.View.HomePage;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.DoIt.Daos;
import com.DoIt.Progress;
import com.DoIt.View.JoinedProject;
import com.DoIt.View.SetProject;
import com.DoIt.Adapters.SelfJoinListAdapter;
import com.DoIt.GreenDaos.Dao.Joins;
import com.DoIt.JavaBean.Join;
import com.DoIt.R;
import com.umeng.analytics.MobclickAgent;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class SelfJoinList extends Fragment {
    private static final String[] OPTION_ITEMS = {
            "置顶",
            "归档",
            "隐私设置"
    };
    public static final String[] PRIVACY = {
            "所有人可见" ,
            "仅自己可见"
    };
    private int privacy;
    private SelfJoinListAdapter adapter;
    private Progress progress;
    private Activity activity;
    private BroadcastReceiver receiver;
    private Joins joins;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate
                (R.layout.fragment_self_join_list, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("SelfJoinList");
        if (getContext() != null)
            getContext().registerReceiver(receiver, new IntentFilter("dataPush"));
        adapter.setList(Daos.getInt(getActivity()).getNowJoinList());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("SelfJoinList");
        if (getContext() != null)
            getContext().unregisterReceiver(receiver);
    }
    /**
     *初始化页面
     *@param view view
     */
    private void initView(View view) {
        RecyclerView recycler = view.findViewById(R.id.recycler);
        FloatingActionButton add = view.findViewById(R.id.add);
        adapter = new SelfJoinListAdapter();
        if ((activity = getActivity()) != null) {
            recycler.setLayoutManager(new LinearLayoutManager(activity));
            recycler.addItemDecoration(new DividerItemDecoration
                    (activity, DividerItemDecoration.VERTICAL));
            recycler.setFocusable(false);
            recycler.setAdapter(adapter);
            progress = new Progress(getActivity());
        }
        adapter.setOnItemClickListener(new SelfJoinListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, Joins joins) {
                Intent intent = new Intent(getContext(), JoinedProject.class);
                intent.putExtra("joinId", joins.getId());
                startActivity(intent);
            }
            @Override
            public void onDeal(View v, Joins joins) {
                SelfJoinList.this.joins = joins;
                setDialog(activity);
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SetProject.class);
                startActivity(intent);
            }
        });
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                adapter.setList(Daos.getInt(getActivity()).getNowJoinList());
            }
        };
    }
    /**
     *弹窗设置
     *@param context context
     */
    private void setDialog(Context context) {
        MobclickAgent.onEvent(getContext(),"manageJoinList");
        AlertDialog.Builder optionDialog = new AlertDialog.Builder(context,R.style.MyDialogTheme);
        optionDialog.setItems(OPTION_ITEMS, new DialogInterface.OnClickListener() {
            private Context context;
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        setJoinToTop();//置顶
                        break;
                    case 1:
                        removeJoins();//归档
                        break;
                    case 2:
                        initPrivacyDialog(context);//隐私设置
                        break;
                    default:
                        break;
                }
            }
            private DialogInterface.OnClickListener setContext(Context context){
                this.context = context;
                return this;
            }
        }.setContext(context)).show();
    }
    /**
     *通过设置join点击数来置顶任务
     */
    private void setJoinToTop() {
        if (adapter.getJoinsList().size() > 1) {
            joins.setClickTime(adapter.getItem(0).getClickTime() + 1);
            joins.update();
            adapter.setList(Daos.getInt(getActivity()).getNowJoinList());
        }
    }
    /**
     *归档任务
     */
    private void removeJoins(){
        Daos.getInt(getActivity()).updateJoinsImportance(joins.getId(),1);
        adapter.removeItem(joins);
    }
    /**
     *弹出隐私设置弹窗
     */
    private void initPrivacyDialog(Context context){
        AlertDialog.Builder privacyDialog =
                new AlertDialog.Builder(context, R.style.MyDialogTheme);
        privacyDialog.setTitle("隐私设置");
        privacyDialog.setSingleChoiceItems(PRIVACY, joins.getPrivacy(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        privacy = which;
                    }
                });
        privacyDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progress.setThread(new Runnable() {
                    @Override
                    public void run() {
                        //更新隐私标记
                        updateJoinPrivacy();
                    }
                }).startProgress("正在上传信息，请稍等");
            }
        }).show();
    }
    /**
     * 更新join的隐私标记privacy
     */
    private void updateJoinPrivacy() {
        Join join = new Join();
        join.setObjectId(joins.getObjectId());
        join.setPrivacy(privacy);
        joins.setPrivacy(privacy);
        join.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                progress.finishProgress();
                if (e == null) {
                    //更新列表内容
                    joins.update();
                    adapter.setList(Daos.getInt(getActivity()).getNowJoinList());
                    Toast.makeText(getContext(),
                            "隐私设置更新成功", Toast.LENGTH_SHORT).show();
                } else Toast.makeText(getContext(),
                        "隐私设置更新失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
