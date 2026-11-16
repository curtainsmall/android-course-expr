package com.curtainsmall.expr2;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.curtainsmall.expr2.ui.CalendarFragment;
import com.curtainsmall.expr2.ui.ProfileFragment;
import com.curtainsmall.expr2.ui.TasksFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * 主页面：底部导航（任务 / 日历 / 我的），点击时替换容器中的 Fragment。
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

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_tasks);
        }
    }

    private Fragment createFragment(int itemId) {
        if (itemId == R.id.nav_calendar) {
            return new CalendarFragment();
        }
        if (itemId == R.id.nav_profile) {
            return new ProfileFragment();
        }
        if (itemId == R.id.nav_tasks) {
            return new TasksFragment();
        }
        return null;
    }
}
