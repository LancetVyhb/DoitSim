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

import com.bumptech.glide.Glide;
import com.DoIt.Items.ProjectAdapterItem;
import com.DoIt.GreenDaos.Dao.Joins;
import com.DoIt.GreenDaos.Dao.ProjectItems;
import com.DoIt.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter{
    private static final String[] ROLE = {
            "事主",
            "管理员",
            ""
    };
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
    private static final String[][] OPTION_STATE = {
            {"已回应", "已同意", "已拒绝", "未回应",},
            {"已审核", "已通过", "已否决", "未审核",},
    };
    private List<ProjectAdapterItem> itemList;
    private OnButtonClickListener onButtonClickListener;
    private SimpleDateFormat formatter;
    private Activity activity;
    private Joins self;
    private JSONObject power;
    @SuppressLint("SimpleDateFormat")
    public ProjectAdapter(Joins self, Activity activity){
        itemList = new ArrayList<>();
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.self = self;
        this.activity = activity;
        try {
            this.power = new JSONObject(self.getProjects().getStruct());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.project_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ProjectAdapterItem item = itemList.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.setView(item);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
    /**
     * 获取数据并加载
     * @param list 数据
     */
    public void setList(List<ProjectAdapterItem> list){
        this.itemList = list;
        notifyDataSetChanged();
    }
    /**
     * 更新权限
     */
    public void updatePower(){
        try {
            power = new JSONObject(self.getProjects().getStruct());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public List<ProjectAdapterItem> getList(){
        return itemList;
    }


    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }

    public ProjectAdapterItem getItem(int position){
        return itemList.get(position);
    }
    /**
     * 获取父类
     * @param id 子类id
     */
    private ProjectAdapterItem getParent(long id) {
        ProjectAdapterItem parent = new ProjectAdapterItem();
        for (ProjectAdapterItem item : itemList)
            if (item.projectItems.getId() == id)
                parent = item;
        return parent;
    }
    /**
     * 添加新数据
     * @param projectItems 新数据
     */
    public void setNewProjectItem(ProjectItems projectItems) {
        for (int i = 0; i < itemList.size(); i++) {
            //父子匹配
            if (itemList.get(i).projectItems.getObjectId().
                    equals(projectItems.getParent().getObjectId())) {
                //判断父类是否已打开，时则直接添加，否则打开父类
                if (itemList.get(i).isOpen) {
                    ProjectAdapterItem item = new ProjectAdapterItem(projectItems);
                    itemList.add(i + 1, item);
                    itemList.get(i).childrenSize++;
                } else open(i);
                break;
            }
        }
        notifyDataSetChanged();
    }
    /**
     * 更新新数据
     * @param projectItems 新数据
     */
    public void updateProjectItem(ProjectItems projectItems){
        for (int i=0;i<itemList.size();i++) {
            if (itemList.get(i).projectItems.getId().equals(projectItems.getId())) {
                itemList.get(i).projectItems = projectItems;
            }
        }
        notifyDataSetChanged();
    }
    /**
     * 删除数据
     * @param id 被删除的数据的id
     */
    public void deleteProjectItem(long id){
        Iterator<ProjectAdapterItem> iterator = itemList.iterator();
        while (iterator.hasNext()){
            ProjectAdapterItem item= iterator.next();
            if(item.projectItems.getId() == id) {
                //把被删除了的数据移出列表
                iterator.remove();
                //更新父类的children的Size
                getParent(item.projectItems.getParentId()).childrenSize--;
                //如果被删除的数据还有子类，则子类也要删除
                if (item.childrenSize != 0 && item.isOpen) {
                    for (int i = 0; i < item.childrenSize; i++) {
                        iterator.next();
                        iterator.remove();
                    }
                }
            }
        }
        notifyDataSetChanged();
    }
    /**
     * 删除某一个参与者的join及其projectItem
     * @param id 被删除的数据的id
     */
    public void deleteJoin(long id){
        Iterator<ProjectAdapterItem> iterator = itemList.iterator();
        while (iterator.hasNext()) {
            ProjectAdapterItem item= iterator.next();
            if(item.projectItems.getSender().getId()==id){
                iterator.remove();
                //如果被删除的数据还有子类，则子类也要删除
                if (item.childrenSize != 0 && item.isOpen) {
                    for (int i = 0; i < item.childrenSize; i++) {
                        iterator.next();
                        iterator.remove();
                    }
                }
            }
        }
        //更新父类的children的Size
        itemList.get(0).childrenSize--;
        notifyDataSetChanged();
    }
    /**
     * 打开某一父类的子类
     * @param position 父类的位置
     */
    private void open(int position) {
        //获取该父类的子类
        List<ProjectItems> children = itemList.get(position).projectItems.getChildren();
        //加载子类
        List<ProjectAdapterItem> list = new ArrayList<>();
        for (int i = 0; i < children.size(); i++) {
            ProjectAdapterItem projectAdapterItem = new ProjectAdapterItem(children.get(i));
            if (projectAdapterItem.projectItems.getIsSelf()) list.add(0, projectAdapterItem);
            else list.add(projectAdapterItem);
        }
        //更新该父类
        itemList.get(position).isOpen = true;
        itemList.get(position).childrenSize = children.size();
        itemList.addAll(position + 1, list);
        notifyDataSetChanged();
    }
    /**
     * 关闭某一父类的子类
     * @param position 父类的位置
     */
    private void collapse(int position) {
        itemList.get(position).isOpen = false;
        for (int i = 0; i < itemList.get(position).childrenSize; i++) {
            //如果子类也有展开该子类的子类，则递归该子类
            if (itemList.get(position + 1).isOpen) collapse(position + 1);
            itemList.remove(position + 1);
        }
        notifyDataSetChanged();
    }


    public interface OnButtonClickListener{
        void update(int position);
        void reply(int position);
        void getJoiner(int position);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name, date, replied, agree, reject;
        private ImageView head, option, open, reply, edit, hide;
        private ConstraintLayout status;
        private RecyclerView recycler;
        private ContentAdapter adapter;
        private ViewHolder(View itemView) {
            super(itemView);
            status = itemView.findViewById(R.id.status);
            recycler = itemView.findViewById(R.id.recycler);
            name = itemView.findViewById(R.id.name);
            option = itemView.findViewById(R.id.option);
            date = itemView.findViewById(R.id.time);
            open = itemView.findViewById(R.id.open);
            reply = itemView.findViewById(R.id.reply);
            edit = itemView.findViewById(R.id.edit);
            hide = itemView.findViewById(R.id.hide);
            head = itemView.findViewById(R.id.head);
            replied = itemView.findViewById(R.id.replied);
            agree = itemView.findViewById(R.id.agree);
            reject = itemView.findViewById(R.id.reject);
            head.setImageResource(R.drawable.head);
            open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (itemList.get(position).isOpen) collapse(position);
                    else open(position);
                }
            });
            hide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isHide = itemList.get(getAdapterPosition()).isHide;
                    itemList.get(getAdapterPosition()).isHide = !isHide;
                    notifyDataSetChanged();
                }
            });
            head.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onButtonClickListener.getJoiner(getAdapterPosition());
                }
            });
            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onButtonClickListener.getJoiner(getAdapterPosition());
                }
            });
            reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onButtonClickListener.reply(getAdapterPosition());
                }
            });
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onButtonClickListener.update(getAdapterPosition());
                }
            });
            recycler.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            adapter = new ContentAdapter(false, activity);
            recycler.setFocusable(false);
            recycler.setAdapter(adapter);
        }

        @SuppressLint({"SetTextI18n", "ResourceAsColor"})
        private void setView(ProjectAdapterItem item) {
            recycler.setVisibility(View.VISIBLE);
            status.setVisibility(View.VISIBLE);
            itemView.setVisibility(View.VISIBLE);
            reply.setVisibility(View.VISIBLE);
            open.setVisibility(View.VISIBLE);
            edit.setVisibility(View.VISIBLE);
            //如果该父类已被回复，则隐藏“回复”按钮
            if (item.projectItems.getHasReplied()) reply.setVisibility(View.GONE);
            //如果该项目不是自己的，则隐藏“编辑”按钮
            if (!item.projectItems.getIsSelf()) edit.setVisibility(View.GONE);
                //执行人自己不能审核自己
            else if (item.projectItems.getType() == 1 && self.getRole() == 2)
                reply.setVisibility(View.GONE);
            if (item.projectItems.getType() == 2) {
                status.setVisibility(View.GONE);
                open.setVisibility(View.GONE);
                reply.setVisibility(View.GONE);
            } else {
                replied.setText(Integer.toString(item.projectItems.getTotal()));
                agree.setText(Integer.toString(item.projectItems.getAgree()));
                reject.setText(Integer.toString(item.projectItems.getReject()));
            }
            //当当前用户没有权限时
            if (self.getRole() == 2) {
                //不开放审核时
                if (!power.optBoolean("isFreeJudge")) reply.setVisibility(View.GONE);
                else if (item.projectItems.getType() != 0) reply.setVisibility(View.VISIBLE);
            }
            //设置open的图像
            if (item.isOpen) open.setImageResource(R.drawable.close);
            else open.setImageResource(R.drawable.open);
            if (item.isHide) {
                hide.setImageResource(R.drawable.hide);
                recycler.setVisibility(View.GONE);
            } else {
                hide.setImageResource(R.drawable.show);
                recycler.setVisibility(View.VISIBLE);
            }
            //加载数据
            name.setText(item.projectItems.getSender().getJoiner().getUserName()
                    + " " + ROLE[item.projectItems.getSender().getRole()]);//加载身份
            if (item.projectItems.getContent().equals("[]")) recycler.setVisibility(View.GONE);
            else adapter.setList(item.projectItems.getContent(), itemView.getContext());//加载内容
            if (item.projectItems.getUpdatedAt() != null)
                date.setText(formatter.format(item.projectItems.getUpdatedAt()));//加载时间
            else date.setText(formatter.format(item.projectItems.getCreatedAt()));
            if (item.projectItems.getSender().getJoiner().getHeadImage() != null)
                Glide.with(itemView).load//加载图片
                        (item.projectItems.getSender().getJoiner().getHeadImage()).into(head);
            //根据态度设置议程颜色和图像
            Resources resources = itemView.getContext().getResources();
            if (item.projectItems.getType() != 0) {
                itemView.setBackgroundColor(resources.getColor(COLOR[item.projectItems.getOption()]));
                option.setImageResource(OPTION[item.projectItems.getOption()]);
                name.setText(name.getText().toString() + " " +
                        OPTION_STATE[item.projectItems.getType() - 1][item.projectItems.getOption()]);
            } else {
                itemView.setBackgroundColor(resources.getColor(R.color.target));
                option.setImageResource(R.color.target);
            }
        }
    }
}
