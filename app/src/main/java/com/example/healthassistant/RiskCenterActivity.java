package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RiskCenterActivity extends AppCompatActivity {
    private RiskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_risk_center);

        RecyclerView recyclerView = findViewById(R.id.recycler_risks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RiskAdapter(risk -> {
            Intent intent = new Intent(this, RiskDetailActivity.class);
            intent.putExtra("risk_id", risk.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        loadRisks();
    }

    private void loadRisks() {
        RetrofitClient.getApiService().getRisks(true, 1, 50)
                .enqueue(new Callback<ApiResponse<PagedResponse<Risk>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PagedResponse<Risk>>> call, Response<ApiResponse<PagedResponse<Risk>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            adapter.setItems(response.body().getData().getItems());
                        } else {
                            Toast.makeText(RiskCenterActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PagedResponse<Risk>>> call, Throwable t) {
                        Toast.makeText(RiskCenterActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
