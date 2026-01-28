package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        SessionManager sessionManager = new SessionManager(this);
        Button buttonStart = findViewById(R.id.button_onboarding_start);
        TextView textSkip = findViewById(R.id.text_onboarding_skip);

        buttonStart.setOnClickListener(v -> {
            sessionManager.setOnboardingSeen(true);
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        });

        textSkip.setOnClickListener(v -> {
            sessionManager.setOnboardingSeen(true);
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        });
    }
}
