package com.curtainsmall.expr2.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 账号信息存储（SharedPreferences）。
 * <p>
 * 实验要求：第一次使用时需要用户名、密码验证，可以用 SharedPreferences 保存用户数据。
 * 这里保存用户名、密码与登录状态；登录状态用于启动页判断是否需要再次输入密码。
 * <p>
 * 说明：课程实验为便于查看直接保存明文口令，真实项目应改用加密存储或服务端校验。
 */
public class UserPrefs {

    private static final String FILE_NAME = "expr2_user";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_LOGGED_IN = "logged_in";

    private final SharedPreferences prefs;

    public UserPrefs(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    /** 是否已经注册过账号（第一次使用为 false） */
    public boolean hasAccount() {
        return prefs.contains(KEY_USERNAME);
    }

    public String username() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean loggedIn) {
        prefs.edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply();
    }

    /** 注册（或修改）账号，并直接置为已登录 */
    public void saveAccount(String username, String password) {
        prefs.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_PASSWORD, password)
                .putBoolean(KEY_LOGGED_IN, true)
                .apply();
    }

    public boolean checkPassword(String password) {
        return password != null && password.equals(prefs.getString(KEY_PASSWORD, ""));
    }

    /** 清除账号信息，回到“首次使用”状态 */
    public void clearAccount() {
        prefs.edit().clear().apply();
    }
}
