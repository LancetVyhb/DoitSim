package com.DoIt.Adapters;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.DoIt.GreenDaos.Dao.Joins;
import com.DoIt.GreenDaos.Dao.Projects;
import com.DoIt.Items.ChooseProjectItem;
import com.DoIt.R;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ChooseProjectAdapter extends RecyclerView.Adapter{
    private OnChooseListener onChooseListener;
    private SimpleDateFormat formatter;
    private List<ChooseProjectItem> list;
    public boolean onBind;

    @SuppressLint("SimpleDateFormat")
    public ChooseProjectAdapter(){
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        list = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.choose_project_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder,int position) {
        onBind = true;
        ChooseProjectItem item = list.get(position);
        ViewHolder viewHolder = (ViewHolder)holder;
        viewHolder.setView(item);
        viewHolder.isChose.setTag(position);
        viewHolder.isChose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    int position = (int) buttonView.getTag();
                    onChooseListener.onChoose(list.get(position).projects, position);
                    list.get(position).isChose = true;
                }
            }
        });
        onBind = false;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    /**
     * 搜索在上一个列表被选中的projects在新的列表中的位置
     * @param projects 被选中的projects
     */
    public int checkPosition(Projects projects){
        int position = -1;
        for (int i=0;i<list.size();i++) if(list.get(i).projects == projects) position = i;
        return position;
    }
    /**
     * 修改列表选项的选择状态
     * @param position 要修改的列表选项的位置
     * @param isChecked 要修改的状态
     */
    public void setChecked(int position,boolean isChecked) {
        list.get(position).isChose = isChecked;
        notifyDataSetChanged();
    }
    /**
     * 获取数据并展示
     * @param items 数据
     */
    public void setList(List<Joins> items){
        list.clear();
        if (items != null) {
            for (Joins joins : items) {
                ChooseProjectItem item = new ChooseProjectItem();
                item.projects = joins.getProjects();
                item.isChose = false;
                list.add(item);
            }
            notifyDataSetChanged();
        }
    }

    public void setOnChooseListener(OnChooseListener onChooseListener){
        this.onChooseListener = onChooseListener;
    }

    public interface OnChooseListener{
        void onChoose(Projects choseProjects,int position);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name,title,number,date;
        private ImageView head;
        private RadioButton isChose;
        private ViewHolder(View itemView) {
            super(itemView);
            head = itemView.findViewById(R.id.head);
            name = itemView.findViewById(R.id.name);
            title = itemView.findViewById(R.id.message);
            number = itemView.findViewById(R.id.number);
            date = itemView.findViewById(R.id.time);
            isChose = itemView.findViewById(R.id.isChose);
            head.setImageResource(R.drawable.head);
        }
        @SuppressLint("SetTextI18n")
        public void setView(ChooseProjectItem item){
            date.setText(formatter.format(item.projects.getCreatedAt()));
            name.setText(item.projects.getSender().getUserName());
            title.setText(item.projects.getTitle());
            number.setText(Integer.toString(item.projects.getNumber()) + "人参与");
            String headImage = item.projects.getSender().getHeadImage();
            if(headImage!=null)
                Glide.with(itemView).load(headImage).into(head);
            isChose.setChecked(item.isChose);
        }
    }
}
