package com.DoIt.View;

import android.content.Intent;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.DoIt.Adapters.JoinerListAdapter;
import com.DoIt.Daos;
import com.DoIt.Adapters.ChooseSubjectAdapter;
import com.DoIt.GreenDaos.Dao.Subjects;
import com.DoIt.GreenDaos.Dao.Tabs;
import com.DoIt.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChooseSubject extends AppCompatActivity {
    public static final int CHOOSE_SUBJECT_RESULT = 505;
    private ExpandableListView expandableListView;
    private ChooseSubjectAdapter showAdapter, searchAdapter;//一个展示用，一个搜索用
    private JoinerListAdapter joinerListAdapter;
    private List<Subjects> resultList, joinerList;//结果集合和排除集合
    private boolean isSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_subject);
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
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();

        joinerListAdapter = new JoinerListAdapter();
        showAdapter = new ChooseSubjectAdapter(false);
        searchAdapter = new ChooseSubjectAdapter(false);
        expandableListView = findViewById(R.id.expandListView);
        ChooseSubjectAdapter.OnResultChangeListener listener =
                new ChooseSubjectAdapter.OnResultChangeListener() {
            @Override
            public void onResultChange(List<Subjects> resultList) {
                ChooseSubject.this.resultList = resultList;
                joinerListAdapter.setJoinerList(resultList);
            }
        };
        showAdapter.setListener(listener);
        searchAdapter.setListener(listener);

        RecyclerView joiners = findViewById(R.id.resultList);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        joiners.setLayoutManager(manager);
        joiners.setFocusable(false);
        joiners.setAdapter(joinerListAdapter);
        joinerListAdapter.setListener(new JoinerListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Subjects subjects) {
                resultList.remove(subjects);
                if (isSearch) {
                    searchAdapter.setResultList(resultList);
                    searchAdapter.checkChose();
                } else {
                    showAdapter.setResultList(resultList);
                    showAdapter.checkChose();
                }
            }
        });
        SearchView search = findViewById(R.id.search);
        Button ok = findViewById(R.id.ok);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                //当搜索栏为空时，搜索列表恢复为展示列表
                if (newText.equals("")) setShowAdapter();
                else setSearchAdapter(newText);
                return false;
            }
        });
        search.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) finish();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSearch) resultList = searchAdapter.getResultList();
                else resultList = showAdapter.getResultList();
                if ((joinerList.size() + resultList.size()) <= 50
                        || Objects.equals(getIntent().getAction(), "setTab")
                        || Objects.equals(getIntent().getAction(), "deleteSubject")) {
                    //返回选择结果
                    long[] results = new long[resultList.size()];
                    for (int i = 0; i < resultList.size(); i++)
                        if (resultList.get(i) != null)
                            results[i] = resultList.get(i).getId();
                    Intent intent = getIntent();
                    intent.putExtra("results", results);
                    setResult(CHOOSE_SUBJECT_RESULT, intent);
                    finish();
                } else Toast.makeText(ChooseSubject.this,
                        "已满员，请重新选择",Toast.LENGTH_SHORT).show();
            }
        });
        setResultList();
        setJoinerList();
        setShowAdapter();
    }
    /**
     * 获取结果集合
     */
    private void setResultList() {
        Intent intent = getIntent();
        resultList = new ArrayList<>();
        long[] id;
        if ((id = intent.getLongArrayExtra("subjectIdList")) != null)
            for (long anId : id) resultList.add(Daos.getInt(this).getSubjects(anId));
        showAdapter.setResultList(resultList);
        searchAdapter.setResultList(resultList);
        joinerListAdapter.setJoinerList(resultList);
    }
    /**
     * 获取排除集合
     */
    private void setJoinerList() {
        Intent intent = getIntent();
        joinerList = new ArrayList<>();
        long[] id;
        if ((id = intent.getLongArrayExtra("joinerIdList")) != null)
            for (long anId : id) joinerList.add(Daos.getInt(this).getSubjects(anId));
        showAdapter.setJoinerList(joinerList);
        searchAdapter.setJoinerList(joinerList);
    }
    /**
     * 加载所有的标签
     */
    private List<String> setTab() {
        List<String> list = new ArrayList<>();
        List<Tabs> tabList = Daos.getInt(this).getAllTabWithoutSelf();
        for (int i = 0; i < tabList.size(); i++)
            list.add(tabList.get(i).getName());
        return list;
    }
    /**
     * 加载展示列表
     */
    private void setShowAdapter() {
        //获取标签
        List<String> group = setTab();
        showAdapter.setGroupList(group);
        List<List<Subjects>> childList = new ArrayList<>();
        //根据标签分别加载subjects
        for (int i = 0; i < group.size(); i++) {
            List<Subjects> list = Daos.getInt(this).getSubjectListByTab(group.get(i));
            childList.add(list);
        }
        showAdapter.setItemList(childList);
        //根据结果集合检查列表项是否为已选择
        showAdapter.setResultList(resultList);
        showAdapter.checkChose();
        expandableListView.setAdapter(showAdapter);
        //展开父类
        for (int i = 0; i < showAdapter.getGroupList().size(); i++)
            expandableListView.expandGroup(i);
        isSearch = false;
    }
    /**
     * 加载搜索列表
     */
    private void setSearchAdapter(String query) {
        List<String> searchGroup = new ArrayList<>();
        List<List<Subjects>> searchChildList = new ArrayList<>();
        //只加载一个父类“搜索结果”
        searchGroup.add("搜索结果");
        //获取搜索结果
        searchChildList.add(Daos.getInt(this).getSubjectListByQuery(query));
        searchAdapter.setGroupList(searchGroup);
        searchAdapter.setItemList(searchChildList);
        //根据结果集合检查列表项是否为已选择
        searchAdapter.setResultList(resultList);
        searchAdapter.checkChose();
        expandableListView.setAdapter(searchAdapter);
        expandableListView.expandGroup(0);
        isSearch = true;
    }
}
