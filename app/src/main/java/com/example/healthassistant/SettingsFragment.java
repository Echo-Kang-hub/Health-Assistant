package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends Fragment {
    private Switch demoSwitch;
    private RadioGroup toneGroup;
    private TextView dataSourcesStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        demoSwitch = view.findViewById(R.id.switch_demo);
        toneGroup = view.findViewById(R.id.radio_tone_group);
        dataSourcesStatus = view.findViewById(R.id.text_data_sources_status);
        Button logoutButton = view.findViewById(R.id.button_logout);

        SessionManager sessionManager = new SessionManager(requireContext());
        demoSwitch.setChecked(sessionManager.isDemoMode());
        demoSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDemoMode(isChecked));

        if ("professional".equals(sessionManager.getAiTone())) {
            ((RadioButton) view.findViewById(R.id.radio_professional)).setChecked(true);
        } else {
            ((RadioButton) view.findViewById(R.id.radio_simple)).setChecked(true);
        }
        toneGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String tone = checkedId == R.id.radio_professional ? "professional" : "simple";
            sessionManager.setAiTone(tone);
        });

        view.findViewById(R.id.card_data_sources).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), DataSourcesActivity.class));
        });

        logoutButton.setOnClickListener(v -> {
            RetrofitClient.getApiService().logout(new RefreshRequest(sessionManager.getRefreshToken()))
                    .enqueue(new Callback<ApiResponse<Object>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                            sessionManager.clearTokens();
                            startActivity(new Intent(requireContext(), AuthActivity.class));
                            requireActivity().finish();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                            sessionManager.clearTokens();
                            startActivity(new Intent(requireContext(), AuthActivity.class));
                            requireActivity().finish();
                        }
                    });
        });

        loadDataSources();
    }

    private void toggleDemoMode(boolean enabled) {
        SessionManager sessionManager = new SessionManager(requireContext());
        sessionManager.setDemoMode(enabled);
        Call<ApiResponse<Object>> call = enabled
                ? RetrofitClient.getApiService().enableDemo()
                : RetrofitClient.getApiService().disableDemo();
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), enabled ? "已开启演示模式" : "已关闭演示模式", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "操作失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDataSources() {
        RetrofitClient.getApiService().getDataSources().enqueue(new Callback<ApiResponse<java.util.List<DataSource>>>() {
            @Override
            public void onResponse(Call<ApiResponse<java.util.List<DataSource>>> call, Response<ApiResponse<java.util.List<DataSource>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    java.util.List<DataSource> sources = response.body().getData();
                    if (sources.isEmpty()) {
                        dataSourcesStatus.setText("睡眠/心率/血糖：未连接");
                    } else {
                        dataSourcesStatus.setText("已连接 " + sources.size() + " 项");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<java.util.List<DataSource>>> call, Throwable t) {
            }
        });
    }
}
