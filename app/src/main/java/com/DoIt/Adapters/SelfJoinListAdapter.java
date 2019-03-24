package com.DoIt.Adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.DoIt.GreenDaos.Dao.Joins;
import com.DoIt.GreenDaos.Dao.Projects;
import com.DoIt.GreenDaos.Dao.Subjects;
import com.DoIt.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import q.rorbin.badgeview.QBadgeView;

public class SelfJoinListAdapter extends RecyclerView.Adapter{
    private static final String[] STATUS = {
            "计划",
            "执行",
            "完成",
            "警告"
    };
    private static final String[] COLORS = {
            "#FFFFFFFF",
            "#FFFEF0B5",
            "#FFBEFFAD",
            "#FFFFADAD"
    };
    private List<Joins> joinsList;
    private OnItemClickListener onItemClickListener;
    private SimpleDateFormat formatter;

    public SelfJoinListAdapter(){
        joinsList = new ArrayList<>();
        formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.self_join_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Joins joins = joinsList.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.setView(joins);
    }

    @Override
    public int getItemCount() {
        return joinsList.size();
    }

    public void setList(List<Joins> list) {
        joinsList.clear();
        joinsList = list;
        notifyDataSetChanged();
    }

    public List<Joins> getJoinsList(){
        return joinsList;
    }

    public Joins getItem(int position){
        return joinsList.get(position);
    }

    public void removeItem(Joins joins) {
        joinsList.remove(joins);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(View v, Joins joins);
        void onDeal(View v, Joins joins);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name, date, title, status, number;
        private ImageView head;
        private ImageButton deal;
        private QBadgeView badgeView;

        private ViewHolder(View itemView) {
            super(itemView);
            deal = itemView.findViewById(R.id.deal);
            name = itemView.findViewById(R.id.name);
            date = itemView.findViewById(R.id.time);
            title = itemView.findViewById(R.id.message);
            status = itemView.findViewById(R.id.status);
            number = itemView.findViewById(R.id.number);
            head = itemView.findViewById(R.id.head);
            badgeView = new QBadgeView(itemView.getContext());
            badgeView.bindTarget(head)
                    .setBadgeGravity(Gravity.TOP | Gravity.END)
                    .setGravityOffset(0, 0, true);
            head.setImageResource(R.drawable.head);
            deal.setImageResource(R.drawable.ic_sync_black_24dp);
            deal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onDeal(v, joinsList.get(getAdapterPosition()));
                    }
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onClick(v, joinsList.get(getAdapterPosition()));
                }
            });
        }

        @SuppressLint("SetTextI18n")
        private void setView(Joins joins) {
            Projects projects = joins.getProjects();
            Subjects sender = projects.getSender();
            name.setText(sender.getUserName());
            date.setText(formatter.format(joins.getCreatedAt()));
            title.setText(projects.getTitle());
            status.setText(STATUS[joins.getStatus()]);
            number.setText(Integer.toString(projects.getNumber()) + "人参与");
            if (sender.getHeadImage() != null)
                Glide.with(itemView).load(sender.getHeadImage()).into(head);
            itemView.setBackgroundColor(Color.parseColor(COLORS[joins.getStatus()]));
            badgeView.setBadgeNumber(joins.getNewItem());
        }
    }
}
