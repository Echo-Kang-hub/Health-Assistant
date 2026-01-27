package com.example.healthassistant;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            String baseUrl = BuildConfig.BACKEND_BASE_URL;
            if (!baseUrl.endsWith("/")) {
                baseUrl = baseUrl + "/";
            }

            // AI 处理较慢，设置 60 秒超时
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
