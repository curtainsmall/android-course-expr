package com.curtainsmall.expr2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.curtainsmall.expr2.LoginActivity;
import com.curtainsmall.expr2.R;
import com.curtainsmall.expr2.data.TaskDbHelper;
import com.curtainsmall.expr2.data.UserPrefs;

/**
 * “我的”页：显示当前账号与任务统计，并提供退出登录。
 */
public class ProfileFragment extends Fragment {

    private UserPrefs prefs;
    private TaskDbHelper db;

    private TextView textUsername;
    private TextView textTotal;
    private TextView textPending;
    private TextView textDone;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = new UserPrefs(requireContext());
        db = TaskDbHelper.get(requireContext());

        textUsername = view.findViewById(R.id.tv_username);
        textTotal = view.findViewById(R.id.tv_stat_total);
        textPending = view.findViewById(R.id.tv_stat_pending);
        textDone = view.findViewById(R.id.tv_stat_done);

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> logout());
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        if (textUsername == null || prefs == null || db == null) {
            return;
        }
        textUsername.setText(prefs.username());
        int[] stats = db.stats(prefs.username());
        textTotal.setText(String.valueOf(stats[0]));
        textDone.setText(String.valueOf(stats[1]));
        textPending.setText(String.valueOf(stats[0] - stats[1]));
    }

    private void logout() {
        prefs.setLoggedIn(false);
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
