package com.DoIt.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.DoIt.GetLocations;
import com.DoIt.JavaBean.Project;
import com.DoIt.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NearProjectListAdapter extends RecyclerView.Adapter {
    private List<Project> list;
    private Location where;
    private Address self;
    private SimpleDateFormat formatter,paser;
    private OnItemClickListener onItemClickListener;

    @SuppressLint("SimpleDateFormat")
    public NearProjectListAdapter(){
        list = new ArrayList<>();
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        paser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

    public void setWhere(Location where, Context context){
        this.where = where;
        this.self = GetLocations.getAddress(context, where.getLatitude(),where.getLongitude());
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
            //获取地理信息
            GetLocations.Distance distance = GetLocations.getDistance(project.getPlace(), where);
            Address address = GetLocations.getAddress(itemView.getContext(),
                    project.getPlace().getLatitude(), project.getPlace().getLongitude());
            distances.setText(Double.toString(distance.distance) + distance.unit);
            if (!(address != null && self != null)) {
                places.setVisibility(View.GONE);
            } else {
                places.setVisibility(View.VISIBLE);
                //屏蔽掉省市信息
                String place = address.getAddressLine(0)
                        .replace(self.getAdminArea() + self.getLocality(), "");
                places.setText(place);
            }
        }
    }
}
