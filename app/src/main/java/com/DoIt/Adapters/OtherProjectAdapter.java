package com.DoIt.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.DoIt.Bmobs;
import com.DoIt.Items.OtherProjectAdapterItem;
import com.DoIt.Progress;
import com.DoIt.JavaBean.ProjectItem;
import com.DoIt.JavaBean.Subject;
import com.DoIt.R;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;

public class OtherProjectAdapter extends RecyclerView.Adapter{
    private static final int[] OPTION = {
            R.color.replied,
            R.drawable.agree,
            R.drawable.reject,
            R.color.unReply,
    };
    private static final int[] COLOR = {
            R.color.replied,
            R.color.agree,
            R.color.reject,
            R.color.unReply,
    };
    private OnButtonClickListener onButtonClickListener;
    private List<OtherProjectAdapterItem> itemList;
    private Progress progress;
    private Activity activity;

    @SuppressLint("SimpleDateFormat")
    public OtherProjectAdapter(Activity activity) {
        this.activity = activity;
        itemList = new ArrayList<>();
        progress = new Progress(activity);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.project_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        OtherProjectAdapterItem item = itemList.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.setView(item);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public Subject getJoiner(int position){
        return itemList.get(position).projectItem.getSender().getJoiner();
    }

    public void setTarget(ProjectItem projectItem){
        OtherProjectAdapterItem item = new OtherProjectAdapterItem(projectItem);
        itemList.add(item);
        notifyDataSetChanged();
    }

    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }

    /**
     * 父类展开子类
     * @param position 父类所在的位置
     */
    private void open(int position){
        Bmobs.getProjectItemByParent(itemList.get(position).projectItem.getObjectId(),
                        new Bmobs.Result<List<ProjectItem>>() {
            private int position;
            @Override
            public void onData(List<ProjectItem> list, BmobException e) {
                progress.finishProgress();
                if (e == null) {
                    //加载子类数据
                    List<OtherProjectAdapterItem> items = new ArrayList<>();
                    for (int i = 0; i < list.size(); i++) {
                        OtherProjectAdapterItem item = new OtherProjectAdapterItem(list.get(i));
                        items.add(item);
                    }
                    //修改父类状态
                    itemList.get(position).isOpen = true;
                    itemList.get(position).childrenSize = list.size();
                    itemList.addAll(position + 1, items);
                    notifyDataSetChanged();
                } else Toast.makeText(activity,"获取数据错误：" +
                        e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            private Bmobs.Result<List<ProjectItem>> setPosition(int position){
                this.position = position;
                return this;
            }
        }.setPosition(position));
    }
    /**
     * 显示议程
     * @param position 议程所在的位置
     */
    private void show(int position) {
        Bmobs.updateProjectItem(itemList.get(position).projectItem.getObjectId(),
                new Bmobs.Result<ProjectItem>() {
            private int position;
            @Override
            public void onData(ProjectItem projectItem, BmobException e) {
                progress.finishProgress();
                if (e == null){
                    itemList.get(position).projectItem = projectItem;
                    itemList.get(position).isHide = false;
                    notifyDataSetChanged();
                } else Toast.makeText(activity,"获取数据错误：" +
                        e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            private Bmobs.Result<ProjectItem> setPosition(int position){
                this.position = position;
                return this;
            }
        }.setPosition(position));
    }
    /**
     * 父类收起子类
     * @param position 父类所在的位置
     */
    private void collapse(int position) {
        itemList.get(position).isOpen = false;
        for (int i = 1; i <= itemList.get(position).childrenSize; i++) {
            //如果子类也展开了子类，则递归收起子类的子类
            if (itemList.get(position + i).isOpen) collapse(position + i);
            else itemList.remove(position + i);
        }
        notifyDataSetChanged();
    }

    public interface OnButtonClickListener{
        void getJoiner(int position);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name, date;
        private ImageView head, option, open, hide, reply, edit;
        private RecyclerView recycler;
        private ContentAdapter adapter;
        private ConstraintLayout status;
        private ViewHolder(View itemView) {
            super(itemView);
            recycler = itemView.findViewById(R.id.recycler);
            status = itemView.findViewById(R.id.status);
            name = itemView.findViewById(R.id.name);
            option = itemView.findViewById(R.id.option);
            date = itemView.findViewById(R.id.time);
            open = itemView.findViewById(R.id.open);
            status = itemView.findViewById(R.id.status);
            reply = itemView.findViewById(R.id.reply);
            edit = itemView.findViewById(R.id.edit);
            hide = itemView.findViewById(R.id.hide);
            head = itemView.findViewById(R.id.head);
            head.setImageResource(R.drawable.head);
            open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemList.get(getAdapterPosition()).isOpen) collapse(getAdapterPosition());
                    else {
                        progress.setThread(new Runnable() {
                            @Override
                            public void run() {
                                open(getAdapterPosition());
                            }
                        }).startProgress("正在获取数据，请稍等");
                    }
                }
            });
            hide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemList.get(getAdapterPosition()).isHide) {
                        progress.setThread(new Runnable() {
                            @Override
                            public void run() {
                                show(getAdapterPosition());
                            }
                        });
                        progress.startProgress("正在获取数据，请稍等");
                    } else {
                        itemList.get(getAdapterPosition()).isHide = true;
                        notifyDataSetChanged();
                    }
                }
            });
            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onButtonClickListener.getJoiner(getAdapterPosition());
                }
            });
            head.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onButtonClickListener.getJoiner(getAdapterPosition());
                }
            });
            edit.setVisibility(View.GONE);
            reply.setVisibility(View.GONE);
            status.setVisibility(View.GONE);
            recycler.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            adapter = new ContentAdapter(false, activity);
            recycler.setAdapter(adapter);
        }
        /**
         * 加载数据
         * @param item 所要加载的数据
         */
        private void setView(OtherProjectAdapterItem item) {
            recycler.setVisibility(View.VISIBLE);
            //加载数据
            name.setText(item.projectItem.getSender().getJoiner().getUserName());
            if (item.projectItem.getContent().equals("[]") || item.isHide)
                recycler.setVisibility(View.GONE);
            else adapter.setList(item.projectItem.getContent(), itemView.getContext());
            date.setText(item.projectItem.getUpdatedAt());
            String headImage = item.projectItem.getSender().getJoiner().getHeadImage();
            if (headImage != null)
                Glide.with(itemView).load(headImage).into(head);

            //底项没有子类，不能展开
            if (item.projectItem.getType() == 2) {
                open.setVisibility(View.GONE);
            }
            //顶项没有option
            if (item.projectItem.getType() == 0) option.setVisibility(View.GONE);
            else option.setVisibility(View.VISIBLE);
            if (item.isOpen) open.setImageResource(R.drawable.close);
            else open.setImageResource(R.drawable.open);
            if (item.isHide) {
                hide.setImageResource(R.drawable.hide);
                status.setVisibility(View.GONE);
                recycler.setVisibility(View.GONE);
            } else {
                hide.setImageResource(R.drawable.show);
                status.setVisibility(View.VISIBLE);
                recycler.setVisibility(View.VISIBLE);
            }
            //根据态度设置议程颜色和图像
            Resources resources = itemView.getContext().getResources();
            if (item.projectItem.getType() != 0) {
                itemView.setBackgroundColor(resources.getColor(COLOR[item.projectItem.getOption()]));
                option.setImageResource(OPTION[item.projectItem.getOption()]);
            } else {
                itemView.setBackgroundColor(resources.getColor(R.color.target));
                option.setImageResource(R.color.target);
            }
        }
    }
}
