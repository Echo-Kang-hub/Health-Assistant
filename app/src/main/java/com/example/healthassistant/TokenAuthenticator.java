package com.example.healthassistant;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TokenAuthenticator implements Authenticator {
    private final SessionManager sessionManager;
    private final ApiService authService;

    public TokenAuthenticator(SessionManager sessionManager, HttpUrl baseUrl) {
        this.sessionManager = sessionManager;
        OkHttpClient client = new OkHttpClient.Builder().build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.authService = retrofit.create(ApiService.class);
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        if (responseCount(response) >= 2) {
            return null;
        }
        String refreshToken = sessionManager.getRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty()) {
            return null;
        }

        Call<ApiResponse<TokenPair>> call = authService.refresh(new RefreshRequest(refreshToken));
        retrofit2.Response<ApiResponse<TokenPair>> refreshResponse = call.execute();
        if (!refreshResponse.isSuccessful() || refreshResponse.body() == null || refreshResponse.body().getData() == null) {
            sessionManager.clearTokens();
            return null;
        }
        TokenPair tokens = refreshResponse.body().getData();
        sessionManager.saveTokens(tokens.getAccessToken(), tokens.getRefreshToken());
        return response.request().newBuilder()
                .header("Authorization", "Bearer " + tokens.getAccessToken())
                .build();
    }

    private int responseCount(Response response) {
        int count = 1;
        while ((response = response.priorResponse()) != null) {
            count++;
        }
        return count;
    }
}
