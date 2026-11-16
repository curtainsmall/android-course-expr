package com.curtainsmall.expr2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.curtainsmall.expr2.R;
import com.curtainsmall.expr2.TaskEditActivity;
import com.curtainsmall.expr2.data.TaskDbHelper;
import com.curtainsmall.expr2.data.UserPrefs;
import com.curtainsmall.expr2.model.Task;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 任务列表页：支持“按时间顺序”和“按类别分组”两种展示方式。
 */
public class TasksFragment extends Fragment implements TaskAdapter.Listener {

    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> reload());

    private RecyclerView recyclerView;
    private TextView textEmpty;
    private TextView textSummary;
    private TaskAdapter adapter;
    private TaskDbHelper db;
    private UserPrefs prefs;

    private int order = TaskDbHelper.ORDER_BY_TIME;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = TaskDbHelper.get(requireContext());
        prefs = new UserPrefs(requireContext());

        recyclerView = view.findViewById(R.id.rv_tasks);
        textEmpty = view.findViewById(R.id.tv_tasks_empty);
        textSummary = view.findViewById(R.id.tv_tasks_summary);

        adapter = new TaskAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        MaterialButtonToggleGroup toggleSort = view.findViewById(R.id.toggle_sort);
        toggleSort.check(R.id.btn_sort_time);
        toggleSort.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            order = checkedId == R.id.btn_sort_category
                    ? TaskDbHelper.ORDER_BY_CATEGORY
                    : TaskDbHelper.ORDER_BY_TIME;
            reload();
        });

        view.findViewById(R.id.fab_add).setOnClickListener(v -> openEditor(0L));
    }

    @Override
    public void onResume() {
        super.onResume();
        reload();
    }

    private void openEditor(long taskId) {
        Intent intent = new Intent(requireContext(), TaskEditActivity.class);
        if (taskId > 0) {
            intent.putExtra(TaskEditActivity.EXTRA_TASK_ID, taskId);
        }
        editLauncher.launch(intent);
    }

    private void reload() {
        if (adapter == null || db == null || prefs == null) {
            return;
        }

        List<Task> tasks = db.query(prefs.username(), order);
        List<Object> rows = new ArrayList<>();

        if (order == TaskDbHelper.ORDER_BY_CATEGORY) {
            Map<String, Integer> counts = new LinkedHashMap<>();
            for (Task task : tasks) {
                String category = categoryOf(task);
                Integer old = counts.get(category);
                counts.put(category, old == null ? 1 : old + 1);
            }
            Set<String> written = new HashSet<>();
            for (Task task : tasks) {
                String category = categoryOf(task);
                if (written.add(category)) {
                    rows.add(getString(R.string.tasks_category_header_format,
                            category, counts.get(category)));
                }
                rows.add(task);
            }
        } else {
            rows.addAll(tasks);
        }

        adapter.submit(rows);
        textEmpty.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);

        int pending = 0;
        for (Task task : tasks) {
            if (!task.isDone()) {
                pending++;
            }
        }
        textSummary.setText(getString(R.string.tasks_count_format, tasks.size(), pending));
    }

    private String categoryOf(Task task) {
        return TextUtils.isEmpty(task.category)
                ? getString(R.string.category_default)
                : task.category;
    }

    @Override
    public void onOpen(Task task) {
        openEditor(task.id);
    }

    @Override
    public void onToggleDone(Task task, boolean done) {
        db.updateStatus(task.id, done ? Task.STATUS_DONE : Task.STATUS_TODO);
        reload();
    }
}
