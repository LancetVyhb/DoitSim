package com.DoIt.View.HomePage;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.DoIt.Daos;
import com.DoIt.View.ChooseSubject;
import com.DoIt.View.Home;
import com.DoIt.View.OtherSubject;
import com.DoIt.View.SearchSubject;
import com.DoIt.Adapters.ChooseSubjectAdapter;
import com.DoIt.GreenDaos.Dao.Subjects;
import com.DoIt.GreenDaos.Dao.Tabs;
import com.DoIt.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

public class SubjectList extends Fragment {
    private static final String[] GROUP_ITEM = {
            "重命名标签",
            "删除标签",
    };
    private static final String[] CHILD_ITEM = {
            "设置标签",
            "删除",
    };
    private static final String[] ADD_ITEM = {
            "添加联系人",
            "移除联系人",
            "添加标签",
    };
    private ChooseSubjectAdapter adapter;
    private ExpandableListView expandableListView;
    private EditText editText;
    private Context context;
    private int selectedGroup,selectedChild,option;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate
                (R.layout.fragment_subject_list, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("SubjectList");
        setAdapter();
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("SubjectList");
    }
    /**
     *初始化页面
     */
    private void initView(View view){
        context = getContext();
        SearchView search = view.findViewById(R.id.search);
        expandableListView = view.findViewById(R.id.expandListView);
        adapter = new ChooseSubjectAdapter(true);
        //搜索事件
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                //当搜索栏为空时，搜索列表恢复为展示列表
                if (newText.equals("")) setAdapter();
                else setSearchAdapter(newText);
                return false;
            }
        });
        //子项点击事件
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener(){
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Intent intent = new Intent(getContext(), OtherSubject.class);
                intent.putExtra("subjectObjectId",
                        adapter.getChildItem(groupPosition, childPosition).getObjectId());
                intent.putExtra("subjectUserName",
                        adapter.getChildItem(groupPosition, childPosition).getUserName());
                startActivity(intent);
                return false;
            }
        });
        //长按点击事件
        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent,View view,int position,long id) {
                MobclickAgent.onEvent(getContext(),"manageSubjectList");
                long packedPosition = expandableListView.getExpandableListPosition(position);
                int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
                int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);
                //判断是父类还是子类
                if (childPosition != -1) {
                    selectedGroup = groupPosition;
                    selectedChild = childPosition;
                    initChildDialog();
                } else {
                    selectedGroup = groupPosition;
                    initGroupDialog();
                }
                return true;
            }
        });
        //添加联系人
        FloatingActionButton add = view.findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initAddDialog();
            }
        });
        setAdapter();
    }
    /**
     *标签项长按弹出的弹窗
     */
    private void initGroupDialog(){
        AlertDialog.Builder groupDialog = new AlertDialog.Builder(context,R.style.MyDialogTheme);
        groupDialog.setTitle(adapter.getGroupItem(selectedGroup));
        groupDialog.setItems(GROUP_ITEM, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0://重命名分组
                        option = 0;
                        initSetTabDialog();
                        break;
                    case 1://删除分组
                        //“联系人”是默认标签，无法删除
                        deleteTab();
                        break;
                    default:
                        break;
                }
            }
        }).show();
    }
    /**
     *子项长按点击事件
     */
    private void initChildDialog(){
        AlertDialog.Builder childDialog = new AlertDialog.Builder(context,R.style.MyDialogTheme);
        childDialog.setTitle(adapter.getChildItem(selectedGroup,selectedChild).getUserName());
        childDialog.setItems(CHILD_ITEM, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0://设置分组
                        option = 1;
                        initSetTabDialog();
                        break;
                    case 1://删除
                        Subjects subjects = adapter.getChildItem(selectedGroup, selectedChild);
                        Daos.getInt(getActivity()).setSubjectShow(subjects, false);
                        setAdapter();
                        break;
                    default:
                        break;
                }
            }
        }).show();
    }
    /**
     * 添加按钮弹出菜单
     */
    private void initAddDialog(){
        AlertDialog.Builder addDialog = new AlertDialog.Builder(context,R.style.MyDialogTheme);
        addDialog.setItems(ADD_ITEM, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent;
                switch (which) {
                    case 0:
                        intent = new Intent(getActivity(), SearchSubject.class);
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(getActivity(), ChooseSubject.class);
                        intent.setAction("deleteSubject");
                        startActivityForResult(intent, Home.HOME_REQUEST);
                        break;
                    case 2:
                        option = 2;
                        initSetTabDialog();
                        break;
                    default:
                        break;
                }
            }
        }).show();
    }
    /**
     * “设置标签”弹窗
     */
    private void initSetTabDialog(){
        editText = new EditText(getActivity());
        AlertDialog.Builder setTabDialog =
                new AlertDialog.Builder(context,R.style.MyDialogTheme);
        setTabDialog.setTitle("标签命名");
        setTabDialog.setView(editText);
        setTabDialog.setPositiveButton("确定",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (option) {
                    case 0://为父类设置分组
                        Daos.getInt(getActivity()).updateTabToDao(adapter.getChildList(selectedGroup),
                                adapter.getGroupItem(selectedGroup), editText.getText().toString());
                        setAdapter();
                        break;
                    case 1://为子类设置分组
                        Daos.getInt(getActivity()).updateSubjectsTab(adapter.getChildItem(selectedGroup,
                                selectedChild).getId(), editText.getText().toString());
                        setAdapter();
                        break;
                    case 2:
                        chooseSubject(editText.getText().toString());
                }
            }
        });
        setTabDialog.show();
    }
    /**
     * 获取所有的标签作为父类
     */
    private List<String> getTab() {
        List<String> list = new ArrayList<>();
        List<Tabs> tabList = Daos.getInt(getActivity()).getAllTabWithoutSelf();
        for (int i = 0; i < tabList.size(); i++)
            list.add(tabList.get(i).getName());
        return list;
    }
    /**
     * 创建标签后进入联系人选取界面选取联系人来设置标签
     * @param name 标签名称
     */
    private void chooseSubject(String name) {
        if (!name.equals("") && !name.equals("联系人") && name.length() < 15) {
            Intent intent = new Intent(getContext(), ChooseSubject.class);
            long[] id = null;
            if (Daos.getInt(getActivity()).checkTabExits(editText.getText().toString()) != null) {
                List<Subjects> list = Daos.getInt(getActivity()).getSubjectListByTab(name);
                id = new long[list.size()];
                for (int i = 0; i < list.size(); i++)
                    id[i] = list.get(i).getId();
            }
            intent.setAction("setTab");
            intent.putExtra("tab", name);
            intent.putExtra("subjectIdList", id);
            startActivityForResult(intent, Home.HOME_REQUEST);
        } else Toast.makeText(getContext(), "请输入有效标签", Toast.LENGTH_SHORT).show();
    }
    /**
     * 删除某一标签，标签下的所有联系人自动转移到默认标签“联系人”
     */
    private void deleteTab() {
        if (adapter.getGroupItem(selectedGroup).equals("联系人"))
            Toast.makeText(getActivity(), "无法删除默认标签", Toast.LENGTH_SHORT).show();
        else {
            Daos.getInt(getActivity()).deleteTab(adapter.getChildList(selectedGroup),
                    adapter.getGroupItem(selectedGroup));
            setAdapter();
        }
    }
    /**
     *加载展示列表
     */
    public void setAdapter() {
        int size = 0;
        //获取标签
        List<String> group = getTab();
        adapter.setGroupList(group);
        List<List<Subjects>> childList = new ArrayList<>();
        //根据标签分别加载subjects
        for (int i = 0; i < group.size(); i++) {
            List<Subjects> list = Daos.getInt(getActivity()).getSubjectListByTab(group.get(i));
            size = size + list.size();
            childList.add(list);
        }
        adapter.setItemList(childList);
        expandableListView.setAdapter(adapter);
        //展开父类
        for (int i = 0; i < adapter.getGroupList().size(); i++)
            expandableListView.expandGroup(i);
    }
    /**
     * 根据搜索关键字加载搜索列表
     * @param query 搜索关键字
     */
    private void setSearchAdapter(String query) {
        List<String> searchGroup = new ArrayList<>();
        List<List<Subjects>> searchChildList = new ArrayList<>();
        //只加载一个父类“搜索结果”
        searchGroup.add("搜索结果");
        //获取搜索结果
        searchChildList.add(Daos.getInt(getActivity()).getSubjectListByQuery(query));
        adapter.setGroupList(searchGroup);
        adapter.setItemList(searchChildList);
        expandableListView.setAdapter(adapter);
        expandableListView.expandGroup(0);
    }
}
