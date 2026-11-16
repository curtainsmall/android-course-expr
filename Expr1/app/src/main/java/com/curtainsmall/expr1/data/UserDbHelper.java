package com.curtainsmall.expr1.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite 数据库帮助类。
 * <p>
 * 表结构：user(_id 自增主键, username 唯一, password)
 * 首次创建数据库时插入一条默认账号：admin / 123456
 */
public class UserDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "expr1.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_USER = "user";
    public static final String COL_ID = "_id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";

    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "123456";

    public UserDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USER + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USERNAME + " TEXT NOT NULL UNIQUE, "
                + COL_PASSWORD + " TEXT NOT NULL)");

        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, DEFAULT_USERNAME);
        values.put(COL_PASSWORD, DEFAULT_PASSWORD);
        db.insert(TABLE_USER, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    /**
     * 校验用户名和密码是否存在且匹配。
     *
     * @return true 表示数据库中保存的账号密码与此一致
     */
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USER,
                    new String[]{COL_ID},
                    COL_USERNAME + " = ? AND " + COL_PASSWORD + " = ?",
                    new String[]{username, password},
                    null, null, null);
            return cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
