package com.DoIt.View;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.DoIt.Bmobs;
import com.DoIt.Daos;
import com.DoIt.Items.SearchSubjectItem;
import com.DoIt.UserUtil;
import com.DoIt.Adapters.SearchSubjectAdapter;
import com.DoIt.GreenDaos.Dao.Subjects;
import com.DoIt.JavaBean.Subject;
import com.DoIt.R;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;

public class SearchSubject extends AppCompatActivity {
    private RefreshLayout refreshLayout;
    private SearchSubjectAdapter adapter;
    private Subjects self;
    private int skip;
    private boolean isRefresh;
    private String queryText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_subject);
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
    private void initView(){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();

        queryText = "";
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.addItemDecoration(new DividerItemDecoration
                (this,DividerItemDecoration.VERTICAL));
        recycler.setFocusable(false);
        SearchView search = findViewById(R.id.search);
        adapter = new SearchSubjectAdapter();
        self = Daos.getInt(this).getSelf();
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                MobclickAgent.onEvent(SearchSubject.this,"searchSubject");
                if(!newText.equals("")) {
                    isRefresh = false;
                    queryText = newText;
                    setSearchAdapter();
                }
                return false;
            }
        });
        search.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) finish();
            }
        });

        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                isRefresh = true;
                if (!queryText.equals("")) setSearchAdapter();
                else refreshLayout.finishRefresh();
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if (!queryText.equals("")) {
                    if ((skip % 20) == 0) loadMoreDate();
                    else {
                        refreshLayout.finishLoadMore();
                        Toast.makeText(SearchSubject.this, "下面没有了", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        adapter.setOnClickListener(new SearchSubjectAdapter.OnClickListener() {
            @Override
            public void onClick(View v, Subject subject) {
                Intent intent = new Intent(SearchSubject.this, OtherSubject.class);
                intent.putExtra("subjectObjectId", subject.getObjectId());
                startActivity(intent);
            }
            @Override
            public void onCheck(View v, Subject subject, boolean isChecked){
                Subjects subjects = Daos.getInt(SearchSubject.this).checkSubjectsExist(subject.getObjectId());
                String s = "";
                if (isChecked) {
                    if (subjects == null)
                        Daos.getInt(SearchSubject.this).setSubjectToDao(subject, "联系人", true);
                    else Daos.getInt(SearchSubject.this).setSubjectShow(subjects, true);
                    s = "已收录联系人：" + subject.getUserName();
                }
                if (!isChecked && subjects != null) {
                    Daos.getInt(SearchSubject.this).setSubjectShow(subjects, false);
                    s = "已取消收录联系人：" + subject.getUserName();
                }
                if (!s.equals(""))
                    Toast.makeText(SearchSubject.this, s, Toast.LENGTH_SHORT).show();
            }
        });
        recycler.setAdapter(adapter);
    }
    /**
     * 根据搜索关键字加载搜索列表
     */
    private void setSearchAdapter() {
        skip = 0;
        Bmobs.searchSubject(queryText, !UserUtil.checkPhoneNumber(queryText), self, skip,
                new Bmobs.Result<List<Subject>>() {
            @Override
            public void onData(List<Subject> list, BmobException e) {
                if (isRefresh) refreshLayout.finishRefresh();
                if (e == null) {
                    if (list != null) {
                        skip = list.size();
                        adapter.setList(checkChoose(list));
                    }
                } else Toast.makeText(SearchSubject.this, e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * 下拉加载获取对方更多的任务列表
     */
    private void loadMoreDate() {
        Bmobs.searchSubject(queryText, !UserUtil.checkPhoneNumber(queryText), self, skip,
                new Bmobs.Result<List<Subject>>() {
            @Override
            public void onData(List<Subject> subjectList, BmobException e) {
                if (e == null) {
                    //下次加载要跳过的数据量
                    skip = skip + subjectList.size();
                    //设置加载状态，true表示正在加载，false表示没有在加载
                    refreshLayout.finishLoadMore();
                    adapter.addList(checkChoose(subjectList));
                    Toast.makeText(SearchSubject.this,
                            "已加载更多", Toast.LENGTH_SHORT).show();
                } else Toast.makeText(SearchSubject.this,
                        "加载数据出错" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * 将查询得来的联系人检验是否已存储、收录并传入适配器
     * @param list 数据
     */
    private List<SearchSubjectItem> checkChoose(List<Subject> list){
        List<SearchSubjectItem> items = new ArrayList<>();
        for (Subject subject : list){
            SearchSubjectItem item = new SearchSubjectItem(subject);
            Subjects subjects  = Daos.getInt(this).checkSubjectsExist(subject.getObjectId());
            item.isChose = (subjects != null && subjects.getIsShow());
            items.add(item);
        }
        return items;
    }
}
