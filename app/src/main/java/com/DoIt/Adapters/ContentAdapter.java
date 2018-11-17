package com.DoIt.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.DoIt.DaoToJson;
import com.DoIt.GreenDaos.Dao.Projects;
import com.DoIt.Items.ContentItem;
import com.DoIt.Medias.ImageViewer;
import com.DoIt.R;
import com.bumptech.glide.Glide;

import com.DoIt.Daos;
import com.DoIt.JumpToProjectPage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ContentAdapter extends RecyclerView.Adapter{
    public static final int TEXT = 0;
    public static final int IMAGE = 1;
    public static final int PROJECT = 2;
    private SimpleDateFormat formatter, paser;
    private Activity activity;
    private boolean isEdit;
    private List<ContentItem> list;
    private EditListener editListener;

    @SuppressLint("SimpleDateFormat")
    public ContentAdapter(boolean isEdit, Activity activity) {
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        paser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        list = new ArrayList<>();
        this.isEdit = isEdit;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TEXT:
                return new TextViewHolder(inflater.inflate
                        (R.layout.content_text_item,parent,false));
            case IMAGE:
                return new ImageViewHolder(inflater.inflate
                        (R.layout.content_image_item,parent,false));
            case PROJECT:
                return new ProjectViewHolder(inflater.inflate
                        (R.layout.content_project_item,parent,false));
        }
        return new TextViewHolder(inflater.inflate
                (R.layout.content_text_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ContentItem item = list.get(position);
        switch (item.type) {
            case TEXT:
                TextViewHolder textViewHolder = (TextViewHolder)holder;
                textViewHolder.setView(position);
                break;
            case IMAGE:
                ImageViewHolder imageViewHolder = (ImageViewHolder)holder;
                imageViewHolder.setView(position);
                break;
            case PROJECT:
                ProjectViewHolder projectViewHolder = (ProjectViewHolder)holder;
                projectViewHolder.setView(position);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).type;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setListener(EditListener editListener){
        this.editListener = editListener;
    }

    public void setList(String content, Context context){
        list.clear();
        if (!content.equals("")) {
            try {
                JSONArray contents = new JSONArray(content);
                for (int i = 0; i < contents.length(); i++) {
                    JSONObject o = contents.getJSONObject(i);
                    ContentItem item = new ContentItem();
                    item.updatedAt = o.optString("updatedAt");
                    item.type = o.optInt("type");
                    item.content = o.optString("content");
                    item.hasRebuild = false;
                    list.add(item);
                }
            } catch (JSONException e) {
                Toast.makeText(context, "加载数据出错" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        notifyDataSetChanged();
    }

    public String getList() {
        try {
            JSONArray content = new JSONArray();
            String updatedAt = paser.format(new Date());
            for (int i = 0; i < list.size(); i++) {
                ContentItem item = list.get(i);
                JSONObject o = new JSONObject();
                if (item.hasRebuild) o.put("updatedAt", updatedAt);
                else o.put("updatedAt", item.updatedAt);
                o.put("content", item.content);
                o.put("type", item.type);
                content.put(i, o);
            }
            return content.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addItem(int position, String content, int type){
        ContentItem item = new ContentItem();
        item.type = type;
        item.content = content;
        item.hasRebuild = true;
        list.add(position ,item);
        notifyDataSetChanged();
    }

    public void rebuildItem(int position, String content){
        list.get(position).content = content;
        list.get(position).hasRebuild = true;
        notifyDataSetChanged();
    }

    public interface EditListener{
        void onAddItem(View v, int position);
        void onImageRebuild(View v, int position);
        void onProjectRebuild(View v, int position);
    }

    private class ContentViewHolder extends RecyclerView.ViewHolder {
        private TextView time, add, delete;
        private ContentViewHolder(View itemView) {
            super(itemView);
            delete = itemView.findViewById(R.id.delete);
            add = itemView.findViewById(R.id.add);
            time = itemView.findViewById(R.id.time);
            delete.setVisibility(View.VISIBLE);
            add.setVisibility(View.VISIBLE);
            time.setVisibility(View.GONE);
            if (!isEdit) {
                delete.setVisibility(View.GONE);
                add.setVisibility(View.GONE);
            }
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    list.remove(getAdapterPosition());
                    notifyDataSetChanged();
                }
            });
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (editListener != null)
                        editListener.onAddItem(v, getAdapterPosition());
                }
            });
        }
        @SuppressLint("SetTextI18n")
        private void setView(int position) {
            ContentItem item = list.get(position);
            if (!isEdit) {
                if (position != list.size() - 1) {
                    if (!item.updatedAt.equals(list.get(position + 1).updatedAt)) {
                        time.setVisibility(View.VISIBLE);
                        time.setText(item.updatedAt + " 更新");
                    }
                } else if (list.size() > 1){
                    time.setVisibility(View.VISIBLE);
                    time.setText(item.updatedAt + " 更新");
                }
            }
        }
    }

    private class TextViewHolder extends ContentViewHolder {
        private TextView content;
        private EditText edit;
        private TextViewHolder(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.content);
            edit = itemView.findViewById(R.id.edit);
            edit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }
                @Override
                public void afterTextChanged(Editable s) {
                    list.get(getAdapterPosition()).content = edit.getText().toString();
                    list.get(getAdapterPosition()).hasRebuild = true;
                }
            });
        }
        private void setView(int position){
            super.setView(position);
            ContentItem item = list.get(position);
            if (isEdit) {
                edit.setVisibility(View.VISIBLE);
                edit.setText(item.content);
                edit.requestFocus();
                content.setVisibility(View.GONE);
            } else {
                content.setVisibility(View.VISIBLE);
                edit.setVisibility(View.GONE);
                content.setText(item.content);
            }
        }
    }

    private class ImageViewHolder extends ContentViewHolder {
        private ImageView image;
        private ImageViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            image.setAdjustViewBounds(true);
            image.setMaxWidth(itemView.getContext().getResources().getDisplayMetrics().widthPixels/2);
            image.setMaxHeight(itemView.getContext().getResources().getDisplayMetrics().heightPixels/5);
        }
        private void setView(int position) {
            super.setView(position);
            ContentItem item = list.get(position);
            Glide.with(itemView.getContext()).load(item.content).into(image);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isEdit) editListener.onImageRebuild(v, getAdapterPosition());
                    else {
                        Intent intent = new Intent(activity, ImageViewer.class);
                        intent.putExtra("image", list.get(getAdapterPosition()).content);
                        activity.startActivity(intent);
                    }
                }
            });
        }
    }

    private class ProjectViewHolder extends ContentViewHolder {
        private TextView name, title, number, date;
        private ImageView head;
        private Projects projects;

        private ProjectViewHolder(View itemView) {
            super(itemView);
            head = itemView.findViewById(R.id.head);
            name = itemView.findViewById(R.id.name);
            title = itemView.findViewById(R.id.message);
            number = itemView.findViewById(R.id.number);
            date = itemView.findViewById(R.id.date);
            head.setImageResource(R.drawable.head);
        }

        @SuppressLint("SetTextI18n")
        private void setView(int position) {
            super.setView(position);
            try {
                JSONObject project = new JSONObject(list.get(position).content);
                String objectId = project.optString("objectId");
                Projects projects;
                if (Daos.getInt(activity).checkProjectIsListening(objectId)) {
                    projects = Daos.getInt(activity).checkProjectsExist(objectId);
                    if (isEdit) list.get(getAdapterPosition()).content
                            = DaoToJson.projectsToJson(projects, false).toString();
                } else {
                    if (Daos.getInt(activity).checkProjectsExist(project.optString("objectId")) == null)
                        Daos.getInt(activity).setProjectToDao(project);
                    projects = Daos.getInt(activity).checkProjectsExist(objectId);
                }
                this.projects = projects;
                name.setText(projects.getSender().getUserName());
                title.setText(projects.getTitle());
                date.setText(formatter.format(projects.getUpdatedAt()));
                number.setText(projects.getNumber() + "人参与");
                if (projects.getSender().getHeadImage() != null)
                    Glide.with(itemView).load(projects.getSender().getHeadImage()).into(head);
            } catch (Exception e) {
                Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isEdit) editListener.onProjectRebuild(v, getAdapterPosition());
                    else JumpToProjectPage.jumpByProjects(activity, projects);
                }
            });
        }
    }
}
