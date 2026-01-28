package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthActivity extends AppCompatActivity {
    private boolean isLogin = true;
    private EditText inputAccount;
    private EditText inputPassword;
    private Button buttonPrimary;
    private TextView toggleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        inputAccount = findViewById(R.id.input_auth_account);
        inputPassword = findViewById(R.id.input_auth_password);
        buttonPrimary = findViewById(R.id.button_auth_primary);
        toggleText = findViewById(R.id.text_auth_toggle);

        buttonPrimary.setOnClickListener(v -> handleAuth());
        toggleText.setOnClickListener(v -> {
            isLogin = !isLogin;
            updateUi();
        });
        updateUi();
    }

    private void updateUi() {
        buttonPrimary.setText(isLogin ? R.string.action_login : R.string.action_register);
        toggleText.setText(isLogin ? R.string.auth_toggle_register : R.string.auth_toggle_login);
    }

    private void handleAuth() {
        String account = inputAccount.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthRequest request = account.contains("@")
                ? new AuthRequest(account, null, password)
                : new AuthRequest(null, account, password);

        Call<ApiResponse<TokenPair>> call = isLogin
                ? RetrofitClient.getAuthService().login(request)
                : RetrofitClient.getAuthService().register(request);

        buttonPrimary.setEnabled(false);
        call.enqueue(new Callback<ApiResponse<TokenPair>>() {
            @Override
            public void onResponse(Call<ApiResponse<TokenPair>> call, Response<ApiResponse<TokenPair>> response) {
                buttonPrimary.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    TokenPair tokens = response.body().getData();
                    SessionManager sessionManager = new SessionManager(AuthActivity.this);
                    sessionManager.saveTokens(tokens.getAccessToken(), tokens.getRefreshToken());
                    startActivity(new Intent(AuthActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(AuthActivity.this, "登录失败，请检查账号或密码", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<TokenPair>> call, Throwable t) {
                buttonPrimary.setEnabled(true);
                Toast.makeText(AuthActivity.this, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
