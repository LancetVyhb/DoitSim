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

import com.DoIt.GetLocations;
import com.DoIt.JavaBean.Project;
import com.DoIt.R;
import com.tencent.map.geolocation.TencentLocation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NearProjectListAdapter extends RecyclerView.Adapter {
    private List<Project> list;
    private TencentLocation where;
    private SimpleDateFormat formatter,parser;
    private OnItemClickListener onItemClickListener;

    @SuppressLint("SimpleDateFormat")
    public NearProjectListAdapter(){
        list = new ArrayList<>();
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.near_project_list_item, parent, false));
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

    public void setWhere(TencentLocation where){
        this.where = where;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onItemClick(View v, Project project);
    }

    @SuppressLint("SetTextI18n")
    private class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name, title, number, date, distances, places;
        private ImageView head;

        private ViewHolder(View itemView) {
            super(itemView);
            head = itemView.findViewById(R.id.head);
            name = itemView.findViewById(R.id.name);
            title = itemView.findViewById(R.id.message);
            number = itemView.findViewById(R.id.number);
            date = itemView.findViewById(R.id.time);
            distances = itemView.findViewById(R.id.distance);
            places = itemView.findViewById(R.id.place);
            head.setImageResource(R.drawable.head);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(v, list.get(getAdapterPosition()));
                }
            });
        }

        public void setView(Project project) {
            Date createdAt = null;
            try {
                createdAt = parser.parse(project.getCreatedAt());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            date.setText(formatter.format(createdAt));
            name.setText(project.getSender().getUserName());
            title.setText(project.getTitle());
            number.setText(project.getNumber() + "人参与");
            String headImage = project.getSender().getHeadImage();
            if (headImage != null) Glide.with(itemView).load(headImage).into(head);
            //获取地理信息
            GetLocations.Distance distance = GetLocations.getDistance(project.getPlace(), where);
            distances.setText(distance.distance + distance.unit);
            if (project.getAddress() == null) places.setVisibility(View.GONE);
            else {
                places.setVisibility(View.VISIBLE);
                String address = project.getAddress().replace(where.getProvince() + where.getCity(), "");
                places.setText(address);
            }
        }
    }
}
