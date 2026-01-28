package com.example.healthassistant;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final SessionManager sessionManager;

    public AuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String accessToken = sessionManager.getAccessToken();
        if (accessToken != null && !accessToken.isEmpty()) {
            request = request.newBuilder()
                    .header("Authorization", "Bearer " + accessToken)
                    .build();
        }
        return chain.proceed(request);
    }
}
