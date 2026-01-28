package com.example.healthassistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class SessionManager {
    private static final String PREFS_NAME = "healthassistant_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_ONBOARDING_SEEN = "onboarding_seen";
    private static final String KEY_DEMO_MODE = "demo_mode";
    private static final String KEY_AI_TONE = "ai_tone";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveTokens(String accessToken, String refreshToken) {
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply();
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return !TextUtils.isEmpty(getAccessToken());
    }

    public void clearTokens() {
        prefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .apply();
    }

    public boolean hasSeenOnboarding() {
        return prefs.getBoolean(KEY_ONBOARDING_SEEN, false);
    }

    public void setOnboardingSeen(boolean seen) {
        prefs.edit().putBoolean(KEY_ONBOARDING_SEEN, seen).apply();
    }

    public boolean isDemoMode() {
        return prefs.getBoolean(KEY_DEMO_MODE, false);
    }

    public void setDemoMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_DEMO_MODE, enabled).apply();
    }

    public String getAiTone() {
        return prefs.getString(KEY_AI_TONE, "professional");
    }

    public void setAiTone(String tone) {
        prefs.edit().putString(KEY_AI_TONE, tone).apply();
    }
}
