package com.DoIt.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.DoIt.GreenDaos.Dao.Subjects;
import com.DoIt.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ResultListAdapter extends BaseAdapter {
    private List<Subjects> resultList;
    private OnItemClickListener listener;

    public ResultListAdapter() {
        resultList = new ArrayList<>();
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setResultList(List<Subjects> resultList) {
        this.resultList = resultList;
        notifyDataSetChanged();
    }

    public void removeItem(Subjects subjects) {
        Iterator<Subjects> iterator = resultList.iterator();
        while (iterator.hasNext()) {
            Subjects subjects1 = iterator.next();
            if (subjects1 == subjects) iterator.remove();
        }
        notifyDataSetChanged();
    }


    public interface OnItemClickListener {
        void onItemClick(View v, Subjects subjects);
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Object getItem(int position) {
        return resultList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.joiner_list_item, parent, false);
            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.name);
            holder.head = convertView.findViewById(R.id.head);
            convertView.setTag(holder);
        } else holder = (ViewHolder) convertView.getTag();
        Subjects subjects = resultList.get(position);
        if (subjects != null) {
            holder.name.setText(subjects.getUserName());
            if (subjects.getHeadImage() == null) holder.head.setImageResource(R.drawable.head);
            else Glide.with(convertView).load(subjects.getHeadImage()).into(holder.head);
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            private int position;
            @Override
            public void onClick(View v) {
                listener.onItemClick(v, resultList.get(position));
            }
            private View.OnClickListener setPosition(int position) {
                this.position = position;
                return this;
            }
        }.setPosition(position));
        return convertView;
    }

    private class ViewHolder{
        private TextView name;
        private ImageView head;
    }
}
