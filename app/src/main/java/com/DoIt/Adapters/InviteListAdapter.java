package com.DoIt.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.DoIt.GreenDaos.Dao.Invite;
import com.DoIt.GreenDaos.Dao.Subjects;
import com.DoIt.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class InviteListAdapter extends RecyclerView.Adapter {
    private List<Invite> list;
    private OnClickListener listener;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.invite_list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder holder = (ViewHolder)viewHolder;
        holder.setView(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setList(List<Invite> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void setListener(OnClickListener listener) {
        this.listener = listener;
    }

    public interface OnClickListener{
        void getSender(View v, Subjects sender);
        void reply(View v, Invite invite, boolean isAccepted);
        void cancel(View v, Invite invite);
        void delete(View v, Invite invite);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name, message, accept, reject;
        private ImageView head;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            message = itemView.findViewById(R.id.message);
            accept = itemView.findViewById(R.id.accept);
            reject = itemView.findViewById(R.id.reject);
            head = itemView.findViewById(R.id.head);
            head.setImageResource(R.drawable.head);
            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Invite invite = list.get(getAdapterPosition());
                    if (invite.getIsSender())
                        listener.getSender(v, list.get(getAdapterPosition()).getReceiver());
                    else listener.getSender(v, list.get(getAdapterPosition()).getSender());
                }
            });
            head.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Invite invite = list.get(getAdapterPosition());
                    if (invite.getIsSender())
                        listener.getSender(v, list.get(getAdapterPosition()).getReceiver());
                    else listener.getSender(v, list.get(getAdapterPosition()).getSender());
                }
            });
        }
        private void setView(Invite invite){
            name.setText(invite.getSender().getUserName());
            message.setText(invite.getMessage());
            String headImage = invite.getSender().getHeadImage();
            if (headImage != null) Glide.with(itemView).load(headImage).into(head);
            if (invite.getHasAccepted() == null) {
                if (invite.getIsSender()) {
                    accept.setText("已发送");
                    accept.setOnClickListener(null);
                    reject.setText("撤销");
                    reject.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.cancel(v, list.get(getAdapterPosition()));
                        }
                    });
                } else {
                    accept.setText("接受");
                    reject.setText("拒绝");
                    accept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.reply(v, list.get(getAdapterPosition()), true);
                        }
                    });
                    reject.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.reply(v, list.get(getAdapterPosition()), false);
                        }
                    });
                }
            } else {
                if (invite.getHasAccepted()) reject.setText("已接受");
                else reject.setText("已拒绝");
                accept.setText("删除");
                accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.delete(v, list.get(getAdapterPosition()));
                    }
                });
                reject.setOnClickListener(null);
            }
        }
    }
}
