package com.curtainsmall.expr2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.curtainsmall.expr2.R;
import com.curtainsmall.expr2.TaskEditActivity;
import com.curtainsmall.expr2.data.TaskDbHelper;
import com.curtainsmall.expr2.data.UserPrefs;
import com.curtainsmall.expr2.model.Task;
import com.curtainsmall.expr2.util.TimeUtils;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 日历视图：按月或按周展示任务日期，点击某一天查看当天的任务。
 * <p>
 * 周起始日固定为星期日，与表头“日 一 二 三 四 五 六”对应。
 */
public class CalendarFragment extends Fragment
        implements CalendarAdapter.Listener, TaskAdapter.Listener {

    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> refresh());

    /** 当前选中的日期（只看年月日，时分秒不参与） */
    private final Calendar selected = Calendar.getInstance();

    private boolean weekMode = false;

    private CalendarAdapter dayAdapter;
    private TaskAdapter taskAdapter;
    private TextView textMonth;
    private TextView textSelectedDay;
    private TextView textDayEmpty;
    private TaskDbHelper db;
    private UserPrefs prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = TaskDbHelper.get(requireContext());
        prefs = new UserPrefs(requireContext());

        textMonth = view.findViewById(R.id.tv_month);
        textSelectedDay = view.findViewById(R.id.tv_selected_day);
        textDayEmpty = view.findViewById(R.id.tv_day_empty);

        RecyclerView dayRecycler = view.findViewById(R.id.rv_days);
        dayAdapter = new CalendarAdapter(this);
        dayRecycler.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        dayRecycler.setAdapter(dayAdapter);

        RecyclerView dayTasksRecycler = view.findViewById(R.id.rv_day_tasks);
        taskAdapter = new TaskAdapter(this);
        dayTasksRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        dayTasksRecycler.setAdapter(taskAdapter);

        MaterialButtonToggleGroup toggleMode = view.findViewById(R.id.toggle_mode);
        toggleMode.check(R.id.btn_mode_month);
        toggleMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            weekMode = checkedId == R.id.btn_mode_week;
            refresh();
        });

        view.findViewById(R.id.btn_prev).setOnClickListener(v -> {
            if (weekMode) {
                selected.add(Calendar.DAY_OF_MONTH, -7);
            } else {
                shiftMonth(-1);
            }
            refresh();
        });

        view.findViewById(R.id.btn_next).setOnClickListener(v -> {
            if (weekMode) {
                selected.add(Calendar.DAY_OF_MONTH, 7);
            } else {
                shiftMonth(1);
            }
            refresh();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    /** 翻月时把日号限制在目标月份的最大天数内，避免 1 月 31 日 + 1 月跳到 3 月 */
    private void shiftMonth(int delta) {
        int day = selected.get(Calendar.DAY_OF_MONTH);
        selected.set(Calendar.DAY_OF_MONTH, 1);
        selected.add(Calendar.MONTH, delta);
        int max = selected.getActualMaximum(Calendar.DAY_OF_MONTH);
        selected.set(Calendar.DAY_OF_MONTH, Math.min(day, max));
    }

    private void refresh() {
        if (dayAdapter == null || db == null || prefs == null) {
            return;
        }

        String owner = prefs.username();
        List<CalendarAdapter.DayCell> cells = new ArrayList<>();
        long viewStart;
        long viewEnd;

        if (weekMode) {
            Calendar start = (Calendar) selected.clone();
            int offset = start.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
            start.add(Calendar.DAY_OF_MONTH, -offset);
            for (int i = 0; i < 7; i++) {
                Calendar day = (Calendar) start.clone();
                day.add(Calendar.DAY_OF_MONTH, i);
                cells.add(cellOf(day));
            }
            viewStart = TimeUtils.startOfDay(start.getTimeInMillis());
            Calendar after = (Calendar) start.clone();
            after.add(Calendar.DAY_OF_MONTH, 7);
            viewEnd = TimeUtils.startOfDay(after.getTimeInMillis());
            textMonth.setText(getString(R.string.calendar_week_format,
                    start.get(Calendar.YEAR),
                    start.get(Calendar.MONTH) + 1,
                    start.get(Calendar.DAY_OF_MONTH)));
        } else {
            Calendar first = (Calendar) selected.clone();
            first.set(Calendar.DAY_OF_MONTH, 1);
            int lead = first.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
            int maxDay = first.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int i = 0; i < lead; i++) {
                cells.add(blankCell());
            }
            for (int day = 1; day <= maxDay; day++) {
                Calendar current = (Calendar) first.clone();
                current.set(Calendar.DAY_OF_MONTH, day);
                cells.add(cellOf(current));
            }
            viewStart = TimeUtils.startOfDay(first.getTimeInMillis());
            Calendar after = (Calendar) first.clone();
            after.set(Calendar.DAY_OF_MONTH, maxDay);
            after.add(Calendar.DAY_OF_MONTH, 1);
            viewEnd = TimeUtils.startOfDay(after.getTimeInMillis());
            textMonth.setText(getString(R.string.calendar_month_format,
                    first.get(Calendar.YEAR),
                    first.get(Calendar.MONTH) + 1));
        }

        int todayKey = TimeUtils.dayKey(System.currentTimeMillis());
        int selectedKey = TimeUtils.dayKey(selected.getTimeInMillis());

        Set<Integer> keysWithTask = new HashSet<>();
        for (Task task : db.queryRange(owner, viewStart, viewEnd)) {
            keysWithTask.add(TimeUtils.dayKey(task.dueTime));
        }

        for (CalendarAdapter.DayCell cell : cells) {
            if (cell.isBlank()) {
                continue;
            }
            cell.hasTask = keysWithTask.contains(cell.dayKey);
            cell.isToday = cell.dayKey == todayKey;
            cell.isSelected = cell.dayKey == selectedKey;
        }
        dayAdapter.submit(cells);

        long dayStart = TimeUtils.startOfDay(selected.getTimeInMillis());
        long dayEnd = TimeUtils.nextDayStart(dayStart);
        List<Task> tasks = db.queryRange(owner, dayStart, dayEnd);

        List<Object> rows = new ArrayList<>(tasks);
        taskAdapter.submit(rows);

        textSelectedDay.setText(getString(R.string.calendar_day_format,
                TimeUtils.formatDayLabel(dayStart), tasks.size()));
        textDayEmpty.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private CalendarAdapter.DayCell cellOf(Calendar calendar) {
        CalendarAdapter.DayCell cell = new CalendarAdapter.DayCell();
        cell.year = calendar.get(Calendar.YEAR);
        cell.month = calendar.get(Calendar.MONTH);
        cell.day = calendar.get(Calendar.DAY_OF_MONTH);
        cell.dayKey = TimeUtils.dayKey(cell.year, cell.month, cell.day);
        return cell;
    }

    private CalendarAdapter.DayCell blankCell() {
        CalendarAdapter.DayCell cell = new CalendarAdapter.DayCell();
        cell.day = 0;
        return cell;
    }

    @Override
    public void onDayClick(CalendarAdapter.DayCell cell) {
        selected.set(cell.year, cell.month, cell.day);
        refresh();
    }

    @Override
    public void onOpen(Task task) {
        Intent intent = new Intent(requireContext(), TaskEditActivity.class);
        intent.putExtra(TaskEditActivity.EXTRA_TASK_ID, task.id);
        editLauncher.launch(intent);
    }

    @Override
    public void onToggleDone(Task task, boolean done) {
        db.updateStatus(task.id, done ? Task.STATUS_DONE : Task.STATUS_TODO);
        refresh();
    }
}
