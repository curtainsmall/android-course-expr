package com.curtainsmall.expr2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import com.curtainsmall.expr2.model.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务表的 SQLite 帮助类。
 * <p>
 * 表结构：task(_id, title, content, category, due_time, status, owner, created_at)
 * 每个任务属于一个 owner（即当前登录用户名），因此“查看个人的所有任务”=
 * 按 owner 过滤。
 * <p>
 * 使用单例 + ApplicationContext，避免 Fragment/Activity 反复重建时泄漏与重复打开数据库。
 */
public class TaskDbHelper extends SQLiteOpenHelper {

    /** 按时间顺序 */
    public static final int ORDER_BY_TIME = 0;
    /** 按类别分组（类别内再按时间） */
    public static final int ORDER_BY_CATEGORY = 1;

    private static final String DB_NAME = "expr2.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE = "task";
    private static final String COL_ID = "_id";
    private static final String COL_TITLE = "title";
    private static final String COL_CONTENT = "content";
    private static final String COL_CATEGORY = "category";
    private static final String COL_DUE_TIME = "due_time";
    private static final String COL_STATUS = "status";
    private static final String COL_OWNER = "owner";
    private static final String COL_CREATED_AT = "created_at";

    private static volatile TaskDbHelper sInstance;

    public static TaskDbHelper get(Context context) {
        if (sInstance == null) {
            synchronized (TaskDbHelper.class) {
                if (sInstance == null) {
                    sInstance = new TaskDbHelper(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private TaskDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT NOT NULL, "
                + COL_CONTENT + " TEXT, "
                + COL_CATEGORY + " TEXT NOT NULL, "
                + COL_DUE_TIME + " INTEGER NOT NULL, "
                + COL_STATUS + " INTEGER NOT NULL DEFAULT 0, "
                + COL_OWNER + " TEXT NOT NULL, "
                + COL_CREATED_AT + " INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX idx_task_owner_due ON " + TABLE
                + " (" + COL_OWNER + ", " + COL_DUE_TIME + ")");
        db.execSQL("CREATE INDEX idx_task_owner_category ON " + TABLE
                + " (" + COL_OWNER + ", " + COL_CATEGORY + ")");
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public long insert(Task task) {
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, task.title);
        values.put(COL_CONTENT, task.content);
        values.put(COL_CATEGORY, task.category);
        values.put(COL_DUE_TIME, task.dueTime);
        values.put(COL_STATUS, task.status);
        values.put(COL_OWNER, task.owner);
        values.put(COL_CREATED_AT, System.currentTimeMillis());
        return getWritableDatabase().insert(TABLE, null, values);
    }

    public int update(Task task) {
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, task.title);
        values.put(COL_CONTENT, task.content);
        values.put(COL_CATEGORY, task.category);
        values.put(COL_DUE_TIME, task.dueTime);
        values.put(COL_STATUS, task.status);
        values.put(COL_OWNER, task.owner);
        return getWritableDatabase().update(TABLE, values,
                COL_ID + " = ?", new String[]{String.valueOf(task.id)});
    }

    public int delete(long id) {
        return getWritableDatabase().delete(TABLE,
                COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int updateStatus(long id, int status) {
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, status);
        return getWritableDatabase().update(TABLE, values,
                COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public Task findById(long id) {
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().query(TABLE, null,
                    COL_ID + " = ?", new String[]{String.valueOf(id)},
                    null, null, null);
            if (cursor.moveToFirst()) {
                return read(cursor);
            }
            return null;
        } finally {
            closeQuietly(cursor);
        }
    }

    /** 某个用户的全部任务；order 为 ORDER_BY_TIME 或 ORDER_BY_CATEGORY */
    public List<Task> query(String owner, int order) {
        String orderBy;
        if (order == ORDER_BY_CATEGORY) {
            orderBy = COL_CATEGORY + " COLLATE NOCASE ASC, " + COL_DUE_TIME + " ASC";
        } else {
            orderBy = COL_DUE_TIME + " ASC, " + COL_ID + " ASC";
        }
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().query(TABLE, null,
                    COL_OWNER + " = ?", new String[]{owner},
                    null, null, orderBy);
            return readAll(cursor);
        } finally {
            closeQuietly(cursor);
        }
    }

    /** [fromInclusive, toExclusive) 区间内的任务，按时间升序 */
    public List<Task> queryRange(String owner, long fromInclusive, long toExclusive) {
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().query(TABLE, null,
                    COL_OWNER + " = ? AND " + COL_DUE_TIME + " >= ? AND " + COL_DUE_TIME + " < ?",
                    new String[]{owner, String.valueOf(fromInclusive), String.valueOf(toExclusive)},
                    null, null, COL_DUE_TIME + " ASC, " + COL_ID + " ASC");
            return readAll(cursor);
        } finally {
            closeQuietly(cursor);
        }
    }

    /** 返回 {全部, 已完成} 两个计数 */
    public int[] stats(String owner) {
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery(
                    "SELECT COUNT(*), SUM(CASE WHEN " + COL_STATUS + " = ? THEN 1 ELSE 0 END)"
                            + " FROM " + TABLE + " WHERE " + COL_OWNER + " = ?",
                    new String[]{String.valueOf(Task.STATUS_DONE), owner});
            if (cursor.moveToFirst()) {
                int total = cursor.getInt(0);
                int done = cursor.isNull(1) ? 0 : cursor.getInt(1);
                return new int[]{total, done};
            }
            return new int[]{0, 0};
        } finally {
            closeQuietly(cursor);
        }
    }

    private static List<Task> readAll(Cursor cursor) {
        List<Task> list = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(read(cursor));
            }
        }
        return list;
    }

    private static Task read(Cursor cursor) {
        Task task = new Task();
        task.id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
        task.title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
        task.content = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT));
        task.category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY));
        task.dueTime = cursor.getLong(cursor.getColumnIndexOrThrow(COL_DUE_TIME));
        task.status = cursor.getInt(cursor.getColumnIndexOrThrow(COL_STATUS));
        task.owner = cursor.getString(cursor.getColumnIndexOrThrow(COL_OWNER));
        return task;
    }

    private static void closeQuietly(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }
}
