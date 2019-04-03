package com.DoIt.Adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.DoIt.GreenDaos.Dao.Subjects;
import com.DoIt.R;
import com.bumptech.glide.Glide;

import java.util.Iterator;
import java.util.List;

public class JoinerListAdapter extends RecyclerView.Adapter {
    private List<Subjects> joinerList;
    private OnItemClickListener listener;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.joiner_list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        holder.setView(joinerList.get(position));
    }

    @Override
    public int getItemCount() {
        return joinerList.size();
    }

    public void setJoinerList(List<Subjects> joinerList){
        this.joinerList = joinerList;
        notifyDataSetChanged();
    }

    public void removeItem(Subjects subjects){
        Iterator<Subjects> iterator = joinerList.iterator();
        while (iterator.hasNext()){
            Subjects subjects1 = iterator.next();
            if (subjects1 == subjects) iterator.remove();
        }
        notifyDataSetChanged();
    }

    public void setListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public interface OnItemClickListener{
        void onItemClick(View v, Subjects subjects);
    }

    private class ViewHolder extends RecyclerView.ViewHolder{
        private TextView name;
        private ImageView head;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            head = itemView.findViewById(R.id.head);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, joinerList.get(getAdapterPosition()));
                }
            });
        }
        private void setView(Subjects subjects){
            name.setText(subjects.getUserName());
            if (subjects.getHeadImage() == null) head.setImageResource(R.drawable.head);
            else Glide.with(itemView).load(subjects.getHeadImage()).into(head);
        }
    }
}
