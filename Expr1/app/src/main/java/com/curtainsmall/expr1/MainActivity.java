package com.curtainsmall.expr1;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * 主页面：上方为 Fragment 容器，下方为底部导航栏（主页 / 工作台 / 用户）。
 * 点击底部导航按钮时，替换容器中的 Fragment。
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment target = createFragment(item.getItemId());
            if (target != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, target)
                        .commit();
            }
            return true;
        });

        // 首次进入时默认显示"主页"
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private Fragment createFragment(int itemId) {
        if (itemId == R.id.nav_workbench) {
            return new WorkbenchFragment();
        }
        if (itemId == R.id.nav_profile) {
            return new ProfileFragment();
        }
        if (itemId == R.id.nav_home) {
            return new HomeFragment();
        }
        return null;
    }
}
