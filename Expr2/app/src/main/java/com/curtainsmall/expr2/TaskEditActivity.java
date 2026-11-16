package com.curtainsmall.expr2;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.curtainsmall.expr2.data.TaskDbHelper;
import com.curtainsmall.expr2.data.UserPrefs;
import com.curtainsmall.expr2.model.Task;
import com.curtainsmall.expr2.util.TimeUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

/**
 * 任务新建 / 编辑页。
 * <p>
 * 通过 extra 传入任务 id 表示编辑已有任务，不传则为新建。
 */
public class TaskEditActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_DEFAULT_TIME = "extra_default_time";

    private static final String STATE_DUE_TIME = "state_due_time";
    private static final String STATE_STATUS = "state_status";

    private TaskDbHelper db;
    private UserPrefs prefs;

    private long taskId = 0L;
    private long dueTime;
    private int status = Task.STATUS_TODO;

    private TextInputEditText editTitle;
    private TextInputEditText editContent;
    private TextInputEditText editCategory;
    private MaterialButton buttonDate;
    private MaterialButton buttonTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_task_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.edit_root), (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        db = TaskDbHelper.get(this);
        prefs = new UserPrefs(this);

        TextView textHeader = findViewById(R.id.tv_edit_title);
        editTitle = findViewById(R.id.et_title);
        editContent = findViewById(R.id.et_content);
        editCategory = findViewById(R.id.et_category);
        buttonDate = findViewById(R.id.btn_date);
        buttonTime = findViewById(R.id.btn_time);
        MaterialButtonToggleGroup toggleStatus = findViewById(R.id.toggle_status);
        MaterialButton buttonSave = findViewById(R.id.btn_save);
        MaterialButton buttonDelete = findViewById(R.id.btn_delete);

        taskId = getIntent().getLongExtra(EXTRA_TASK_ID, 0L);
        dueTime = getIntent().getLongExtra(EXTRA_DEFAULT_TIME, 0L);

        if (taskId > 0L) {
            Task existing = db.findById(taskId);
            if (existing == null) {
                toast(R.string.task_not_found);
                finish();
                return;
            }
            editTitle.setText(existing.title);
            editContent.setText(existing.content);
            editCategory.setText(existing.category);
            dueTime = existing.dueTime;
            status = existing.status;
        } else {
            editCategory.setText(R.string.category_default);
            if (dueTime <= 0L) {
                dueTime = System.currentTimeMillis();
            }
        }

        if (savedInstanceState != null) {
            dueTime = savedInstanceState.getLong(STATE_DUE_TIME, dueTime);
            status = savedInstanceState.getInt(STATE_STATUS, status);
        }

        textHeader.setText(taskId > 0L ? R.string.task_edit : R.string.task_new);
        buttonDelete.setVisibility(taskId > 0L ? View.VISIBLE : View.GONE);

        updateTimeButtons();
        toggleStatus.check(statusButtonId(status));
        toggleStatus.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                status = statusOf(checkedId);
            }
        });

        buttonDate.setOnClickListener(v -> pickDate());
        buttonTime.setOnClickListener(v -> pickTime());
        buttonSave.setOnClickListener(v -> save());
        buttonDelete.setOnClickListener(v -> confirmDelete());

        findViewById(R.id.chip_study).setOnClickListener(
                v -> editCategory.setText(R.string.category_study));
        findViewById(R.id.chip_work).setOnClickListener(
                v -> editCategory.setText(R.string.category_work));
        findViewById(R.id.chip_life).setOnClickListener(
                v -> editCategory.setText(R.string.category_life));
        findViewById(R.id.chip_other).setOnClickListener(
                v -> editCategory.setText(R.string.category_other));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_DUE_TIME, dueTime);
        outState.putInt(STATE_STATUS, status);
    }

    private void pickDate() {
        Calendar calendar = TimeUtils.calendarOf(dueTime);
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar updated = TimeUtils.calendarOf(dueTime);
                    updated.set(year, month, dayOfMonth);
                    dueTime = updated.getTimeInMillis();
                    updateTimeButtons();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void pickTime() {
        Calendar calendar = TimeUtils.calendarOf(dueTime);
        new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    Calendar updated = TimeUtils.calendarOf(dueTime);
                    updated.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    updated.set(Calendar.MINUTE, minute);
                    updated.set(Calendar.SECOND, 0);
                    updated.set(Calendar.MILLISECOND, 0);
                    dueTime = updated.getTimeInMillis();
                    updateTimeButtons();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true).show();
    }

    private void updateTimeButtons() {
        buttonDate.setText(TimeUtils.formatDate(dueTime));
        buttonTime.setText(TimeUtils.formatTime(dueTime));
    }

    private void save() {
        String title = textOf(editTitle).trim();
        if (TextUtils.isEmpty(title)) {
            toast(R.string.task_title_required);
            return;
        }

        String category = textOf(editCategory).trim();
        if (TextUtils.isEmpty(category)) {
            category = getString(R.string.category_default);
        }

        Task task = new Task();
        task.id = taskId;
        task.title = title;
        task.content = textOf(editContent).trim();
        task.category = category;
        task.dueTime = dueTime;
        task.status = status;
        task.owner = prefs.username();

        if (taskId > 0L) {
            db.update(task);
        } else {
            db.insert(task);
        }

        toast(R.string.task_saved);
        setResult(RESULT_OK);
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.task_delete)
                .setMessage(R.string.task_delete_confirm)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    db.delete(taskId);
                    toast(R.string.task_deleted);
                    setResult(RESULT_OK);
                    finish();
                })
                .show();
    }

    private static int statusOf(int buttonId) {
        if (buttonId == R.id.btn_status_doing) {
            return Task.STATUS_DOING;
        }
        if (buttonId == R.id.btn_status_done) {
            return Task.STATUS_DONE;
        }
        return Task.STATUS_TODO;
    }

    private static int statusButtonId(int status) {
        if (status == Task.STATUS_DOING) {
            return R.id.btn_status_doing;
        }
        if (status == Task.STATUS_DONE) {
            return R.id.btn_status_done;
        }
        return R.id.btn_status_todo;
    }

    private void toast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    private static String textOf(EditText editText) {
        CharSequence text = editText.getText();
        return text == null ? "" : text.toString();
    }
}
