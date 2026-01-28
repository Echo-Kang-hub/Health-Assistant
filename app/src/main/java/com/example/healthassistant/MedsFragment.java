package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedsFragment extends Fragment {
    private MedicationAdapter adapter;
    private EditText searchInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meds, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_meds);
        searchInput = view.findViewById(R.id.input_meds_search);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MedicationAdapter();
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.fab_scan).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ScanActivity.class));
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadMedications(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        loadMedications("");
    }

    private void loadMedications(String query) {
        RetrofitClient.getApiService().getMedications(query, null, 1, 50)
                .enqueue(new Callback<ApiResponse<PagedResponse<Medication>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PagedResponse<Medication>>> call, Response<ApiResponse<PagedResponse<Medication>>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            List<Medication> meds = response.body().getData().getItems();
                            adapter.setItems(meds != null ? meds : new ArrayList<>());
                        } else {
                            Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PagedResponse<Medication>>> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "网络异常", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
