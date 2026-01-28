package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.hasSeenOnboarding()) {
            startActivity(new Intent(this, OnboardingActivity.class));
        } else if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, AuthActivity.class));
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }
}
