package com.example.healthassistant;

import android.app.Application;

public class HealthAssistantApp extends Application {
    private static HealthAssistantApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static HealthAssistantApp getInstance() {
        return instance;
    }
}
