package com.curtainsmall.expr2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.curtainsmall.expr2.data.UserPrefs;

/**
 * 启动页：读取 SharedPreferences 中的登录状态，决定进入登录页还是主页面。
 */
public class SplashActivity extends AppCompatActivity {

    private static final long DELAY_MS = 900L;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable next = () -> {
        UserPrefs prefs = new UserPrefs(this);
        Class<?> target = prefs.isLoggedIn() ? MainActivity.class : LoginActivity.class;
        startActivity(new Intent(this, target));
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler.postDelayed(next, DELAY_MS);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(next);
        super.onDestroy();
    }
}
