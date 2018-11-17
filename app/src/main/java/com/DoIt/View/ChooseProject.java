package com.DoIt.View;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.DoIt.Daos;
import com.DoIt.Adapters.ChooseProjectAdapter;
import com.DoIt.GreenDaos.Dao.Joins;
import com.DoIt.GreenDaos.Dao.Projects;
import com.DoIt.R;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

public class ChooseProject extends AppCompatActivity {
    public static final int CHOOSE_PROJECT_RESULT = 477;
    private RecyclerView recycler;
    private ChooseProjectAdapter showAdapter,searchAdapter;
    private Projects projects;
    private int chosePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_project);
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

        chosePosition = -1;
        Button ok = findViewById(R.id.ok);
        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(ChooseProject.this));
        recycler.addItemDecoration(new DividerItemDecoration
                (ChooseProject.this,DividerItemDecoration.VERTICAL));
        recycler.setFocusable(false);

        SearchView search = findViewById(R.id.search);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.equals("")) setShowAdapter();
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
                if (projects != null) {
                    Intent intent = getIntent();
                    intent.putExtra("id", projects.getId());
                    setResult(CHOOSE_PROJECT_RESULT , intent);
                    finish();
                } else Toast.makeText(ChooseProject.this,
                        "请选择要发送的任务", Toast.LENGTH_SHORT).show();
            }
        });
        showAdapter = new ChooseProjectAdapter();
        showAdapter.setOnChooseListener(new ChooseProjectAdapter.OnChooseListener() {
            @Override
            public void onChoose(Projects choseProjects, int position) {
                //同一时间只有一个选项被选择，每当下一个选项被选择后，上一个被选择的选项取消被选择状态
                if (chosePosition != -1 && !showAdapter.onBind)
                    showAdapter.setChecked(chosePosition, false);
                chosePosition = position;
                projects = choseProjects;
            }
        });
        searchAdapter = new ChooseProjectAdapter();
        searchAdapter.setOnChooseListener(new ChooseProjectAdapter.OnChooseListener() {
            @Override
            public void onChoose(Projects choosedProjects, int position) {
                //同一时间只有一个选项被选择，每当下一个选项被选择后，上一个被选择的选项取消被选择状态
                if (chosePosition != -1 && !searchAdapter.onBind)
                    searchAdapter.setChecked(chosePosition, false);
                chosePosition = position;
                projects = choosedProjects;
            }
        });
        setShowAdapter();
    }

    private List<Joins> getList(String query){
        Intent intent = getIntent();
        if (intent.getAction() != null)
        switch (intent.getAction()){
            case "inviteOther":
                long id = Daos.getInt(this).checkSubjectsExist
                        (getIntent().getStringExtra("subjectObjectId")).getId();
                return Daos.getInt(this).getInviteOtherJoinList(query, id);
            case "setPlace":
                return Daos.getInt(this).getSetPlaceJoinList(query);
            case "setContent":
                return Daos.getInt(this).getSetContentJoinList(query);
        }
        return null;
    }
    /**
     * 获取可以邀请对方加入的任务列表
     */
    private void setShowAdapter() {
        List<Joins> list = getList("");
        showAdapter.setList(list);
        recycler.setAdapter(showAdapter);
        if (projects != null)
            if ((chosePosition = showAdapter.checkPosition(projects)) != -1)
                showAdapter.setChecked(chosePosition, true);
    }
    /**
     * 获取经过关键字查询后获得可以邀请对方加入的任务列表
     */
    private void setSearchAdapter(String query) {
        List<Joins> list = getList(query);
        searchAdapter.setList(list);
        recycler.setAdapter(searchAdapter);
        if (projects != null)
            if ((chosePosition = searchAdapter.checkPosition(projects)) != -1)
                searchAdapter.setChecked(chosePosition, true);
    }
}
