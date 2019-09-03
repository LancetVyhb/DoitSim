package com.DoIt.View.HomePage;

import android.content.DialogInterface;
import android.content.Intent;
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

import com.DoIt.Adapters.NearProjectListAdapter;
import com.DoIt.Bmobs;
import com.DoIt.GreenDaos.Dao.Projects;
import com.DoIt.JavaBean.Project;
import com.DoIt.JumpToProjectPage;
import com.DoIt.Progress;
import com.DoIt.R;
import com.DoIt.View.ChooseProject;
import com.DoIt.View.SetProject;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationManager;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class NearByProjectList extends Fragment {
    private static final String[] ADD_OPTION = {
            "已有任务",
            "新任务"
    };
    private static final int NEAR_PROJECT_LIST_REQUEST = 999;
    private RefreshLayout refreshLayout;
    private FloatingActionButton add;
    private NearProjectListAdapter adapter;
    private Progress upload;
    private TencentLocation location;
    private int skip;
    private boolean isRefresh;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate
                (R.layout.fragment_near_by_project_list, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("NearByProjectList");
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("NearByProjectList");
    }

    private void initView(View view) {
        RecyclerView recycler = view.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.addItemDecoration(new DividerItemDecoration
                (Objects.requireNonNull(getContext()), DividerItemDecoration.VERTICAL));
        recycler.setFocusable(false);

        refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                isRefresh = true;
                getLocation();
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if ((skip % 20) == 0) loadMoreDate();
                else {
                    refreshLayout.finishLoadMore();
                    Toast.makeText(getContext(), "下面没有了", Toast.LENGTH_SHORT).show();
                }
            }
        });
        adapter = new NearProjectListAdapter();
        adapter.setOnItemClickListener(new NearProjectListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Project project) {
                JumpToProjectPage.jumpByProject(getActivity(), project);
            }
        });
        recycler.setAdapter(adapter);

        add = view.findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initDialog();
            }
        });
    }

    /**
     * 初始化发送任务弹窗
     */
    private void initDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder
                (Objects.requireNonNull(getContext()), R.style.MyDialogTheme);
        builder.setTitle("请选择方式");
        builder.setItems(ADD_OPTION, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent;
                switch (which) {
                    case 0://已有任务
                        intent = new Intent(getContext(), ChooseProject.class);
                        intent.setAction("setPlace");
                        startActivityForResult(intent, NEAR_PROJECT_LIST_REQUEST);
                        break;
                    case 1://新任务
                        intent = new Intent(getContext(), SetProject.class);
                        intent.setAction("setPlace");
                        startActivityForResult(intent, NEAR_PROJECT_LIST_REQUEST);
                        break;
                }
            }
        }).show();
    }

    /**
     * 设置当前用户所在地点，由HOME页面调用
     */
    public void setLocation(TencentLocation location) {
        this.location = location;
        refreshLayout.autoRefresh();
    }

    /**
     * 获取当前用户所在地点
     */
    private void getLocation() {
        if (location == null)
            location = TencentLocationManager.getInstance(getContext()).getLastKnownLocation();
        if (location != null) {
            add.setVisibility(View.VISIBLE);
            adapter.setWhere(location);
            setAdapter();
        } else add.setVisibility(View.INVISIBLE);
    }

    /**
     * 初次获取附近的任务列表
     */
    private void setAdapter() {
        skip = 0;//分页用的数字，代表列表已经加载的数据，在查询的时候要跳过
        Bmobs.getProjectListByGeoPoint(location, skip, new Bmobs.Result<List<Project>>() {
            @Override
            public void onData(List<Project> projects, BmobException e) {
                if (isRefresh) refreshLayout.finishRefresh();
                if (e == null) {
                    if (projects != null) {
                        adapter.setList(projects);
                        //下次加载要跳过的数据量
                        skip = projects.size();
                    }
                } else Toast.makeText(getContext(), "获取任务列表失败" +
                        e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 下拉加载获取附近更多的任务列表
     */
    private void loadMoreDate() {
        Bmobs.getProjectListByGeoPoint(location, skip, new Bmobs.Result<List<Project>>() {
            @Override
            public void onData(List<Project> projects, BmobException e) {
                if (e == null) {
                    //下次加载要跳过的数据量
                    skip = skip + projects.size();
                    //设置加载状态，true表示正在加载，false表示没有在加载
                    refreshLayout.finishLoadMore();
                    adapter.addList(projects);
                    Toast.makeText(getContext(), "已加载更多", Toast.LENGTH_SHORT).show();
                } else Toast.makeText(getContext(), "加载数据出错" +
                        e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 设置任务地点
     */
    public void setPlace(final Projects choseProjects) {
        MobclickAgent.onEvent(getContext(), "setPlace");
        upload = new Progress(getActivity());
        upload.setThread(new Runnable() {
            @Override
            public void run() {
                Project project = new Project();
                project.setObjectId(choseProjects.getObjectId());
                project.setPlace(new BmobGeoPoint(location.getLongitude(), location.getLatitude()));
                project.setAddress(location.getAddress());
                JSONObject power;
                try {
                    power = new JSONObject(choseProjects.getStruct());
                    power.put("isFreeJoin", true);
                    power.put("isFreeOpen", true);
                    project.setStruct(power.toString());
                    project.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            upload.finishProgress();
                            if (e == null) {
                                Toast.makeText(getContext(), "已发布任务", Toast.LENGTH_SHORT).show();
                                refreshLayout.autoRefresh();
                            } else Toast.makeText(getContext(),
                                    "地点设置失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (JSONException e) {
                    upload.finishProgress();
                    Toast.makeText(getContext(), "权限设置出错" +
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }).startProgress("正在上传数据，请稍等");
    }
}