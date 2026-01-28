package com.example.healthassistant;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataSourcesActivity extends AppCompatActivity {
    private DataSourceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_sources);

        RecyclerView recyclerView = findViewById(R.id.recycler_data_sources);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DataSourceAdapter(this::toggleSource);
        recyclerView.setAdapter(adapter);

        loadSources();
    }

    private void loadSources() {
        RetrofitClient.getApiService().getDataSources().enqueue(new Callback<ApiResponse<List<DataSource>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<DataSource>>> call, Response<ApiResponse<List<DataSource>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    adapter.setItems(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<DataSource>>> call, Throwable t) {
                Toast.makeText(DataSourcesActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleSource(DataSource source, boolean connect) {
        DataSourceUpdateRequest request = new DataSourceUpdateRequest(connect ? "connected" : "disconnected", "wearable");
        RetrofitClient.getApiService().updateDataSource(source.getId(), request)
                .enqueue(new Callback<ApiResponse<DataSource>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<DataSource>> call, Response<ApiResponse<DataSource>> response) {
                        loadSources();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<DataSource>> call, Throwable t) {
                        Toast.makeText(DataSourcesActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
