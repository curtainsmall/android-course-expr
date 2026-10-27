package com.curtainsmall.expr1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.curtainsmall.expr1.data.UserDbHelper;

/**
 * 登录页：
 * 1. 用户名、密码输入框 + "确认"、"取消"两个按钮；
 * 2. 用 SQLite 数据库中保存的账号密码校验；
 * 3. 校验失败提示"用户名或密码错误"并允许重新输入；
 * 4. 失败次数写入 SharedPreferences，连续 3 次失败退出程序。
 */
public class LoginActivity extends AppCompatActivity {

    /** SharedPreferences 文件名与键名 */
    private static final String PREFS_NAME = "login_prefs";
    private static final String KEY_FAIL_COUNT = "fail_count";
    /** 允许的最大失败次数 */
    private static final int MAX_FAIL_COUNT = 3;

    private EditText etUsername;
    private EditText etPassword;
    private UserDbHelper dbHelper;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        dbHelper = new UserDbHelper(this);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitApp();
            }
        });
    }

    /** 校验用户名和密码 */
    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.login_error_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. 校验通过：次数清零，进入主页面
        if (dbHelper.checkUser(username, password)) {
            prefs.edit().putInt(KEY_FAIL_COUNT, 0).apply();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // 2. 校验失败：失败次数 +1 并写入 SharedPreferences
        int failCount = prefs.getInt(KEY_FAIL_COUNT, 0) + 1;
        prefs.edit().putInt(KEY_FAIL_COUNT, failCount).apply();

        if (failCount >= MAX_FAIL_COUNT) {
            // 超过三次：提示后退出程序
            Toast.makeText(this, R.string.login_error_locked, Toast.LENGTH_LONG).show();
            prefs.edit().putInt(KEY_FAIL_COUNT, 0).apply();
            exitApp();
        } else {
            Toast.makeText(this, R.string.login_error_invalid, Toast.LENGTH_SHORT).show();
            etPassword.setText("");
            etPassword.requestFocus();
        }
    }

    /** 退出程序 */
    private void exitApp() {
        finishAffinity();
        System.exit(0);
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
