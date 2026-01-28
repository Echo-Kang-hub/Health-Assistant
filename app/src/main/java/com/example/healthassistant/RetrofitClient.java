package com.example.healthassistant;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.OkHttpClient;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static final Object LOCK = new Object();
    private static final AtomicReference<BackendMode> BACKEND_MODE =
            new AtomicReference<>(BackendMode.PRIMARY);

    private enum BackendMode {
        PRIMARY,
        FALLBACK
    }

    public static ApiService getApiService() {
        if (retrofit == null) {
            synchronized (LOCK) {
                if (retrofit == null) {
                    String primaryBaseUrl = normalizeBaseUrl(BuildConfig.BACKEND_BASE_URL);
                    String fallbackBaseUrl = normalizeBaseUrl(BuildConfig.FALLBACK_BACKEND_BASE_URL);

                    HttpUrl primaryHttpUrl = requireHttpUrl(primaryBaseUrl, "BACKEND_BASE_URL");
                    HttpUrl fallbackHttpUrl = requireHttpUrl(fallbackBaseUrl, "FALLBACK_BACKEND_BASE_URL");

                    // AI 处理较慢，设置 60 秒超时
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .addInterceptor(new FallbackInterceptor(primaryHttpUrl, fallbackHttpUrl))
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(primaryHttpUrl)
                            .client(okHttpClient)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit.create(ApiService.class);
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

    private static class FallbackInterceptor implements Interceptor {
        private final HttpUrl primaryBaseUrl;
        private final HttpUrl fallbackBaseUrl;

        private FallbackInterceptor(HttpUrl primaryBaseUrl, HttpUrl fallbackBaseUrl) {
            this.primaryBaseUrl = primaryBaseUrl;
            this.fallbackBaseUrl = fallbackBaseUrl;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            BackendMode mode = BACKEND_MODE.get();
            Request initialRequest = mode == BackendMode.FALLBACK
                    ? request.newBuilder().url(rewriteBaseUrl(request.url(), fallbackBaseUrl)).build()
                    : request;

            try {
                Response response = chain.proceed(initialRequest);
                if (mode == BackendMode.PRIMARY && shouldFallback(response)) {
                    response.close();
                    Request fallbackRequest = request.newBuilder()
                            .url(rewriteBaseUrl(request.url(), fallbackBaseUrl))
                            .build();
                    Response fallbackResponse = chain.proceed(fallbackRequest);
                    BACKEND_MODE.set(BackendMode.FALLBACK);
                    return fallbackResponse;
                }
                return response;
            } catch (IOException primaryError) {
                if (mode == BackendMode.FALLBACK) {
                    throw primaryError;
                }
                Request fallbackRequest = request.newBuilder()
                        .url(rewriteBaseUrl(request.url(), fallbackBaseUrl))
                        .build();
                Response fallbackResponse = chain.proceed(fallbackRequest);
                BACKEND_MODE.set(BackendMode.FALLBACK);
                return fallbackResponse;
            }
        }

        private boolean shouldFallback(Response response) {
            int code = response.code();
            return code == 404 || code == 502 || code == 503 || code == 504;
        }

        private HttpUrl rewriteBaseUrl(HttpUrl originalUrl, HttpUrl newBaseUrl) {
            return originalUrl.newBuilder()
                    .scheme(newBaseUrl.scheme())
                    .host(newBaseUrl.host())
                    .port(newBaseUrl.port())
                    .build();
        }
    }
}
