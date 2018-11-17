package com.DoIt.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.DoIt.GreenDaos.Dao.Subjects;
import com.DoIt.Items.ChooseSubjectItem;
import com.DoIt.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ChooseSubjectAdapter extends BaseExpandableListAdapter {
    private List<String> groupList;
    private List<List<ChooseSubjectItem>> itemList;
    private List<Subjects> joinerList, resultList;
    private OnResultChangeListener listener;
    private boolean isShow;

    public ChooseSubjectAdapter(boolean isShow) {
        groupList = new ArrayList<>();
        itemList = new ArrayList<>();
        this.isShow = isShow;
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return itemList.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return itemList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate
                    (R.layout.choose_subject_list_parent_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.name = convertView.findViewById(R.id.name);
            viewHolder.isChose = convertView.findViewById(R.id.isChose);
            viewHolder.isChose.setTag(groupPosition);
            viewHolder.isChose.setChecked(false);
            //checkBox选择状态改变事件
            viewHolder.isChose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int groupPosition = (int) buttonView.getTag();
                    setGroupCheck(groupPosition, isChecked);
                }
            });
            if (isShow) viewHolder.isChose.setVisibility(View.GONE);
            else viewHolder.isChose.setVisibility(View.VISIBLE);
            convertView.setTag(viewHolder);
        } else viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.name.setText(groupList.get(groupPosition));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate
                    (R.layout.choose_subject_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.name = convertView.findViewById(R.id.name);
            viewHolder.head = convertView.findViewById(R.id.head);
            viewHolder.head.setImageResource(R.drawable.head);
            viewHolder.isChose = convertView.findViewById(R.id.isChose);
            viewHolder.isChose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                private int groupPosition, childPosition;
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    itemList.get(groupPosition).get(childPosition).isChose = isChecked;
                    Subjects subjects = itemList.get(groupPosition).get(childPosition).subjects;
                    //根据选择状态和是否已加入到结果集合将某个子类加入或移出结果集合
                    if (isChecked && !checkInResult(subjects)) resultList.add(subjects);
                    else if (!isChecked && checkInResult(subjects)) removeItem(subjects);
                    listener.onResultChange(resultList);
                }
                //向回调方法传递位置数据
                private CompoundButton.OnCheckedChangeListener setPosition
                (int groupPosition, int childPosition) {
                    this.groupPosition = groupPosition;
                    this.childPosition = childPosition;
                    return this;
                }
            }.setPosition(groupPosition, childPosition));
            if (isShow) viewHolder.isChose.setVisibility(View.GONE);
            else viewHolder.isChose.setVisibility(View.VISIBLE);
            convertView.setTag(viewHolder);
        } else viewHolder = (ViewHolder) convertView.getTag();
        //加载数据
        viewHolder.name.setText
                (itemList.get(groupPosition).get(childPosition).subjects.getUserName());
        viewHolder.isChose.setChecked
                (itemList.get(groupPosition).get(childPosition).isChose);
        if (itemList.get(groupPosition).get(childPosition).subjects.getHeadImage() != null)
            Glide.with(parent).load(itemList.get(groupPosition).
                    get(childPosition).subjects.getHeadImage()).into(viewHolder.head);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public List<Subjects> getChildList(int groupPosition){
        List<ChooseSubjectItem> list = itemList.get(groupPosition);
        List<Subjects> subjectsList = new ArrayList<>();
        if (list != null)
            for (ChooseSubjectItem item : list)
                subjectsList.add(item.subjects);
        return subjectsList;
    }

    public Subjects getChildItem(int groupPosition, int childPosition){
        return itemList.get(groupPosition).get(childPosition).subjects;
    }

    public String getGroupItem(int groupPosition){
        return groupList.get(groupPosition);
    }

    public void setGroupList(List<String> groupList) {
        this.groupList = groupList;
        notifyDataSetChanged();
    }

    public List<String> getGroupList() {
        return groupList;
    }

    public void setJoinerList(List<Subjects> joinerList) {
        this.joinerList = joinerList;
    }

    public void setResultList(List<Subjects> resultList) {
        this.resultList = resultList;
        checkChose();
    }

    public List<Subjects> getResultList() {
        return resultList;
    }
    /**
     * 加载子类
     * @param subjectsList subject集合
     */
    public void setItemList(List<List<Subjects>> subjectsList) {
        itemList = new ArrayList<>();
        for (int i = 0; i < subjectsList.size(); i++) {
            List<ChooseSubjectItem> list = new ArrayList<>();
            for (int o = 0; o < subjectsList.get(i).size(); o++) {
                //排除掉已参加的人
                if (joinerList == null || !joinerList.contains(subjectsList.get(i).get(o))) {
                    ChooseSubjectItem chooseSubjectItem = new ChooseSubjectItem();
                    chooseSubjectItem.isChose = false;
                    chooseSubjectItem.subjects = subjectsList.get(i).get(o);
                    list.add(chooseSubjectItem);
                }
            }
            itemList.add(list);
        }
        notifyDataSetChanged();
    }
    /**
     * 当父类的选择状态被改变时，子类也跟着改变
     * @param groupPosition 父类位置
     * @param isChecked 是否被选择
     */
    private void setGroupCheck(int groupPosition, boolean isChecked) {
        for (int i = 0; i < itemList.get(groupPosition).size(); i++) {
            //当父类的选择状态被改变时，子类也跟着改变
            itemList.get(groupPosition).get(i).isChose = isChecked;
            Subjects subjects = itemList.get(groupPosition).get(i).subjects;
            //根据选择状态将子类加入或移出结果集合
            if (isChecked && !checkInResult(subjects)) resultList.add(subjects);
            else if (!isChecked) removeItem(subjects);
        }
        notifyDataSetChanged();
        listener.onResultChange(resultList);
    }
    /**
     * 根据结果列表检查各个子类是否已被选择
     */
    public void checkChose() {
        for (int i = 0; i < itemList.size(); i++)
            for (int o = 0; o < itemList.get(i).size(); o++)
                itemList.get(i).get(o).isChose = (checkInResult(itemList.get(i).get(o).subjects));
        notifyDataSetChanged();
    }
    /**
     * 检查某个子类是否已被加入到结果列表中
     * @param subjects 子类
     */
    private boolean checkInResult(Subjects subjects) {
        return resultList.contains(subjects);
    }
    /**
     * 移除某个子类
     * @param subjects 子类
     */
    public void removeItem(Subjects subjects) {
        Iterator<Subjects> iterator = resultList.iterator();
        while (iterator.hasNext()) {
            Subjects subjects1 = iterator.next();
            if (subjects == subjects1) iterator.remove();
        }
    }

    public void setListener(OnResultChangeListener listener){
        this.listener = listener;
    }

    public interface OnResultChangeListener{
        void onResultChange(List<Subjects> resultList);
    }

    public class ViewHolder {
        private TextView name;
        private ImageView head;
        private CheckBox isChose;
    }
}
