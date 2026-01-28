package com.example.healthassistant;

import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static Retrofit authRetrofit = null;
    private static ApiService apiService = null;
    private static ApiService authService = null;

    public static ApiService getApiService() {
        if (apiService == null) {
            synchronized (RetrofitClient.class) {
                if (apiService == null) {
                    HttpUrl baseUrl = requireHttpUrl(normalizeBaseUrl(BuildConfig.BACKEND_BASE_URL), "BACKEND_BASE_URL");
                    SessionManager sessionManager = new SessionManager(HealthAssistantApp.getInstance());

                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .addInterceptor(new AuthInterceptor(sessionManager))
                            .authenticator(new TokenAuthenticator(sessionManager, baseUrl))
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(baseUrl)
                            .client(okHttpClient)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    apiService = retrofit.create(ApiService.class);
                }
            }
        }
        return apiService;
    }

    public static ApiService getAuthService() {
        if (authService == null) {
            synchronized (RetrofitClient.class) {
                if (authService == null) {
                    HttpUrl baseUrl = requireHttpUrl(normalizeBaseUrl(BuildConfig.BACKEND_BASE_URL), "BACKEND_BASE_URL");
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .build();
                    authRetrofit = new Retrofit.Builder()
                            .baseUrl(baseUrl)
                            .client(okHttpClient)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    authService = authRetrofit.create(ApiService.class);
                }
            }
        }
        return authService;
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return "";
        }
        String trimmed = baseUrl.trim();
        return trimmed.endsWith("/") ? trimmed : trimmed + "/";
    }

    private static HttpUrl requireHttpUrl(String baseUrl, String label) {
        HttpUrl url = HttpUrl.parse(baseUrl);
        if (url == null) {
            throw new IllegalStateException(label + " is not a valid URL: " + baseUrl);
        }
        return url;
    }
}
