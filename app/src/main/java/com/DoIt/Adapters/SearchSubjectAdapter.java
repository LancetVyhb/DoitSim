package com.DoIt.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.DoIt.Items.SearchSubjectItem;
import com.bumptech.glide.Glide;
import com.DoIt.JavaBean.Subject;
import com.DoIt.R;

import java.util.ArrayList;
import java.util.List;

public class SearchSubjectAdapter extends RecyclerView.Adapter{
    private List<SearchSubjectItem> list;
    private OnClickListener onClickListener;

    public SearchSubjectAdapter(){
        list = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.choose_subject_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.setView(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setList(List<SearchSubjectItem> list){
        this.list = list;
        notifyDataSetChanged();
    }

    public void addList(List<SearchSubjectItem> list){
        this.list.addAll(this.list.size(), list);
        notifyDataSetChanged();
    }

    public void setOnClickListener(OnClickListener onItemClickListener){
        this.onClickListener = onItemClickListener;
    }

    public interface OnClickListener {
        void onClick(View v, Subject subject);
        void onCheck(View v, Subject subject, boolean isChecked);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private ImageView head;
        private CheckBox isChose;
        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            head = itemView.findViewById(R.id.head);
            isChose = itemView.findViewById(R.id.isChose);
            head.setImageResource(R.drawable.head);
            isChose.setText("收录");
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onClick(v, list.get(getAdapterPosition()).subject);
                }
            });
            isChose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    onClickListener.onCheck(buttonView, list.get(getAdapterPosition()).subject, isChecked);
                }
            });
        }
        public void setView(SearchSubjectItem item) {
            name.setText(item.subject.getUserName());
            isChose.setChecked(item.isChose);
            if(item.subject.getHeadImage()!=null)
                Glide.with(itemView).load(item.subject.getHeadImage()).into(head);
        }
    }
}

