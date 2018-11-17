package com.DoIt.Adapters;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.DoIt.JavaBean.Project;
import com.DoIt.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProjectListAdapter extends RecyclerView.Adapter {
    private List<Project> list;
    private SimpleDateFormat formatter,paser;
    private OnClickListener onClickListener;
    private OnLongClickListener onLongClickListener;

    @SuppressLint("SimpleDateFormat")
    public ProjectListAdapter(){
        list = new ArrayList<>();
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        paser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.other_join_list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Project project = list.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.setView(project);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setList(List<Project> list){
        this.list = list;
        notifyDataSetChanged();
    }

    public void addList(List<Project> list){
        this.list.addAll(this.list.size(),list);
        notifyDataSetChanged();
    }

    public void setOnClickListener(OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(OnLongClickListener onLongClickListener){
        this.onLongClickListener = onLongClickListener;
    }

    public interface OnClickListener{
        void onItemClick(View v, Project project);
    }

    public interface OnLongClickListener{
        void onLongClickListener(View v, Project project);
    }

    private class ViewHolder extends RecyclerView.ViewHolder{
        private TextView name,title,number,date;
        private ImageView head;
        private ViewHolder(View itemView) {
            super(itemView);
            head = itemView.findViewById(R.id.head);
            name = itemView.findViewById(R.id.name);
            title = itemView.findViewById(R.id.message);
            number = itemView.findViewById(R.id.number);
            date = itemView.findViewById(R.id.time);
            head.setImageResource(R.drawable.head);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onItemClick(v, list.get(getAdapterPosition()));
                }
            });
            if (onLongClickListener != null)
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        onLongClickListener.onLongClickListener(v, list.get(getAdapterPosition()));
                        return false;
                    }
                });
        }
        @SuppressLint("SetTextI18n")
        public void setView(Project project) {
            Date createdAt = null;
            try {
                createdAt = paser.parse(project.getCreatedAt());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            date.setText(formatter.format(createdAt));
            name.setText(project.getSender().getUserName());
            title.setText(project.getTitle());
            number.setText(Integer.toString(project.getNumber()) + "人参与");
            String headImage = project.getSender().getHeadImage();
            if (headImage != null) Glide.with(itemView).load(headImage).into(head);
        }
    }
}
