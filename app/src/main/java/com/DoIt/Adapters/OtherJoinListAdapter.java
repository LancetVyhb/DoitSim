package com.DoIt.Adapters;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.DoIt.JavaBean.Join;
import com.DoIt.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OtherJoinListAdapter extends RecyclerView.Adapter{
    private List<Join> list;
    private OnItemClickListener onItemClickListener;
    private SimpleDateFormat formatter,paser;

    @SuppressLint("SimpleDateFormat")
    public OtherJoinListAdapter(){
        list = new ArrayList<>();
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        paser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.other_join_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        Join join = list.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.setView(join);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setList(List<Join> list){
        this.list = list;
        notifyDataSetChanged();
    }

    public void addList(List<Join> list){
        this.list.addAll(this.list.size(),list);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onItemClick(View v, Join join);
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
                    onItemClickListener.onItemClick(v, list.get(getAdapterPosition()));
                }
            });
        }
        @SuppressLint("SetTextI18n")
        public void setView(Join join) {
            Date createdAt = new Date();
            try {
                createdAt = paser.parse(join.getCreatedAt());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            date.setText(formatter.format(createdAt));
            name.setText(join.getProject().getSender().getUserName());
            title.setText(join.getProject().getTitle());
            number.setText(Integer.toString(join.getProject().getNumber()) + "人参与");
            String headImage = join.getProject().getSender().getHeadImage();
            if (headImage != null) Glide.with(itemView).load(headImage).into(head);
        }
    }
}
