package com.curtainsmall.expr2.ui;

import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.curtainsmall.expr2.R;
import com.curtainsmall.expr2.model.Task;
import com.curtainsmall.expr2.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务列表适配器。
 * <p>
 * rows 中既可以放 {@link Task}（任务行），也可以放 {@link String}（类别分组标题行），
 * 因此“按时间”与“按类别”两种展示方式共用同一个适配器。
 */
public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TASK = 0;
    private static final int TYPE_HEADER = 1;

    public interface Listener {
        /** 点击任务行：打开编辑页 */
        void onOpen(Task task);

        /** 勾选/取消勾选：切换完成状态 */
        void onToggleDone(Task task, boolean done);
    }

    private final List<Object> rows = new ArrayList<>();
    private final Listener listener;

    public TaskAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Object> newRows) {
        rows.clear();
        if (newRows != null) {
            rows.addAll(newRows);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position) instanceof Task ? TYPE_TASK : TYPE_HEADER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderHolder(inflater.inflate(R.layout.item_category_header, parent, false));
        }
        return new TaskHolder(inflater.inflate(R.layout.item_task, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object row = rows.get(position);
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).bind((String) row);
        } else if (holder instanceof TaskHolder) {
            ((TaskHolder) holder).bind((Task) row, listener);
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    /** 类别分组标题行 */
    static class HeaderHolder extends RecyclerView.ViewHolder {

        HeaderHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bind(String text) {
            ((TextView) itemView).setText(text);
        }
    }

    /** 任务行 */
    static class TaskHolder extends RecyclerView.ViewHolder {

        private final CheckBox checkDone;
        private final TextView textTitle;
        private final TextView textCategory;
        private final TextView textDue;
        private final TextView textStatus;

        TaskHolder(@NonNull View itemView) {
            super(itemView);
            checkDone = itemView.findViewById(R.id.cb_done);
            textTitle = itemView.findViewById(R.id.tv_title);
            textCategory = itemView.findViewById(R.id.tv_category);
            textDue = itemView.findViewById(R.id.tv_due);
            textStatus = itemView.findViewById(R.id.tv_status);
        }

        void bind(Task task, Listener listener) {
            textTitle.setText(task.title);
            textCategory.setText(TextUtils.isEmpty(task.category)
                    ? itemView.getContext().getString(R.string.category_default)
                    : task.category);
            textDue.setText(TimeUtils.formatDateTime(task.dueTime));
            textStatus.setText(statusText(task.status));
            textStatus.setTextColor(ContextCompat.getColor(
                    itemView.getContext(), statusColor(task.status)));

            int paintFlags = textTitle.getPaintFlags();
            if (task.isDone()) {
                paintFlags |= Paint.STRIKE_THRU_TEXT_FLAG;
            } else {
                paintFlags &= ~Paint.STRIKE_THRU_TEXT_FLAG;
            }
            textTitle.setPaintFlags(paintFlags);

            // 先清空监听再设置选中状态，避免复用时误触发回调
            checkDone.setOnCheckedChangeListener(null);
            checkDone.setChecked(task.isDone());
            checkDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggleDone(task, isChecked);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOpen(task);
                }
            });
        }

        private static int statusText(int status) {
            if (status == Task.STATUS_DOING) {
                return R.string.status_doing;
            }
            if (status == Task.STATUS_DONE) {
                return R.string.status_done;
            }
            return R.string.status_todo;
        }

        private static int statusColor(int status) {
            if (status == Task.STATUS_DOING) {
                return R.color.status_doing;
            }
            if (status == Task.STATUS_DONE) {
                return R.color.status_done;
            }
            return R.color.status_todo;
        }
    }
}
