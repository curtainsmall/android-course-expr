package com.curtainsmall.expr2.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.curtainsmall.expr2.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 日历网格适配器：7 列，每格一天。
 * <p>
 * 月视图会在月初补占位格，因此单元格统一用 {@link DayCell} 描述。
 */
public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayHolder> {

    /** 一天（或一个占位空格） */
    public static class DayCell {
        public int year;
        /** 0 基，与 Calendar 一致 */
        public int month;
        /** 小于等于 0 表示占位空格 */
        public int day;
        public int dayKey;
        public boolean hasTask;
        public boolean isToday;
        public boolean isSelected;

        public boolean isBlank() {
            return day <= 0;
        }
    }

    public interface Listener {
        void onDayClick(DayCell cell);
    }

    private final List<DayCell> cells = new ArrayList<>();
    private final Listener listener;

    public CalendarAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<DayCell> newCells) {
        cells.clear();
        if (newCells != null) {
            cells.addAll(newCells);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DayHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DayHolder holder, int position) {
        holder.bind(cells.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    static class DayHolder extends RecyclerView.ViewHolder {

        private final TextView textDay;
        private final View dot;

        DayHolder(@NonNull View itemView) {
            super(itemView);
            textDay = itemView.findViewById(R.id.tv_day);
            dot = itemView.findViewById(R.id.v_dot);
        }

        void bind(DayCell cell, Listener listener) {
            if (cell.isBlank()) {
                textDay.setText("");
                dot.setVisibility(View.INVISIBLE);
                itemView.setBackgroundResource(0);
                itemView.setOnClickListener(null);
                itemView.setClickable(false);
                return;
            }

            textDay.setText(String.valueOf(cell.day));
            dot.setVisibility(cell.hasTask ? View.VISIBLE : View.INVISIBLE);

            if (cell.isSelected) {
                itemView.setBackgroundResource(R.drawable.bg_day_selected);
            } else if (cell.isToday) {
                itemView.setBackgroundResource(R.drawable.bg_day_today);
            } else {
                itemView.setBackgroundResource(0);
            }

            itemView.setClickable(true);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDayClick(cell);
                }
            });
        }
    }
}
