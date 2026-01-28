package com.example.healthassistant;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private ChatAdapter adapter;
    private EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        RecyclerView recyclerView = findViewById(R.id.recycler_chat);
        input = findViewById(R.id.input_chat);
        Button sendButton = findViewById(R.id.button_send);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter();
        recyclerView.setAdapter(adapter);

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String message = input.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            return;
        }
        adapter.addMessage(new ChatMessage("用户", message));
        input.setText("");

        String tone = new SessionManager(this).getAiTone();
        ChatRequest request = new ChatRequest(message, tone, new HashMap<>());
        RetrofitClient.getApiService().chat(request).enqueue(new Callback<ApiResponse<ChatResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ChatResponse>> call, Response<ApiResponse<ChatResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    ChatResponse chat = response.body().getData();
                    adapter.addMessage(new ChatMessage("AI", chat.getReply() + "\n" + chat.getDisclaimer()));
                } else {
                    Toast.makeText(ChatActivity.this, "回复失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ChatResponse>> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
