package com.curtainsmall.expr2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.curtainsmall.expr2.data.UserPrefs;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * 登录 / 注册页。
 * <p>
 * 第一次使用（SharedPreferences 中没有账号）时进入注册模式，要求输入用户名、
 * 密码与确认密码；之后进入登录模式，用已保存的账号校验密码。
 * <p>
 * 校验通过后把登录状态写入 SharedPreferences，下次启动由启动页直接进入主页面。
 */
public class LoginActivity extends AppCompatActivity {

    private UserPrefs prefs;
    private boolean registerMode;

    private TextInputLayout layoutConfirm;
    private TextInputEditText editUsername;
    private TextInputEditText editPassword;
    private TextInputEditText editConfirm;
    private MaterialButton buttonSubmit;
    private MaterialButton buttonReset;
    private TextView textTitle;
    private TextView textHint;

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

        prefs = new UserPrefs(this);

        layoutConfirm = findViewById(R.id.til_confirm);
        editUsername = findViewById(R.id.et_username);
        editPassword = findViewById(R.id.et_password);
        editConfirm = findViewById(R.id.et_confirm);
        buttonSubmit = findViewById(R.id.btn_submit);
        buttonReset = findViewById(R.id.btn_reset);
        textTitle = findViewById(R.id.tv_login_title);
        textHint = findViewById(R.id.tv_login_hint);

        registerMode = !prefs.hasAccount();
        applyMode();

        buttonSubmit.setOnClickListener(v -> submit());
        buttonReset.setOnClickListener(v -> confirmReset());
    }

    private void applyMode() {
        textTitle.setText(registerMode ? R.string.login_title_register : R.string.login_title_login);
        buttonSubmit.setText(registerMode ? R.string.login_action_register : R.string.login_action_login);
        layoutConfirm.setVisibility(registerMode ? View.VISIBLE : View.GONE);
        buttonReset.setVisibility(registerMode ? View.GONE : View.VISIBLE);
        textHint.setText(registerMode ? R.string.login_first_hint : R.string.login_saved_hint);

        if (registerMode) {
            editUsername.setText("");
            editPassword.setText("");
            editConfirm.setText("");
            editUsername.requestFocus();
        } else {
            editUsername.setText(prefs.username());
            CharSequence text = editUsername.getText();
            editUsername.setSelection(text == null ? 0 : text.length());
            editPassword.setText("");
        }
    }

    private void submit() {
        String username = textOf(editUsername).trim();
        String password = textOf(editPassword);

        if (TextUtils.isEmpty(username)) {
            toast(R.string.error_username_empty);
            return;
        }
        if (TextUtils.isEmpty(password)) {
            toast(R.string.error_password_empty);
            return;
        }
        if (password.length() < 4) {
            toast(R.string.error_password_short);
            return;
        }

        if (registerMode) {
            String confirm = textOf(editConfirm);
            if (!password.equals(confirm)) {
                toast(R.string.error_password_mismatch);
                return;
            }
            prefs.saveAccount(username, password);
            toast(R.string.register_success);
            goHome();
            return;
        }

        if (!username.equals(prefs.username())) {
            toast(R.string.error_user_changed);
            return;
        }
        if (!prefs.checkPassword(password)) {
            toast(R.string.error_password_wrong);
            editPassword.setText("");
            editPassword.requestFocus();
            return;
        }
        prefs.setLoggedIn(true);
        toast(R.string.login_success);
        goHome();
    }

    private void goHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.reset_title)
                .setMessage(R.string.reset_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    prefs.clearAccount();
                    registerMode = true;
                    applyMode();
                    toast(R.string.reset_done);
                })
                .show();
    }

    private void toast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    private static String textOf(EditText editText) {
        CharSequence text = editText.getText();
        return text == null ? "" : text.toString();
    }
}
