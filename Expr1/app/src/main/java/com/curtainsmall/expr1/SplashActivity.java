package com.curtainsmall.expr1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 启动页：显示启动画面，停留 3 秒后跳转到登录页。
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 3000L;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable jumpToLogin = new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        handler.postDelayed(jumpToLogin, SPLASH_DELAY_MS);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(jumpToLogin);
        super.onDestroy();
    }
}
