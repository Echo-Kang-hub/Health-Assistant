package com.example.healthassistant;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment;
            if (item.getItemId() == R.id.nav_dashboard) {
                fragment = new DashboardFragment();
            } else if (item.getItemId() == R.id.nav_meds) {
                fragment = new MedsFragment();
            } else if (item.getItemId() == R.id.nav_reports) {
                fragment = new ReportsFragment();
            } else {
                fragment = new SettingsFragment();
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_container, fragment)
                    .commit();
            return true;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        }
    }

    public void selectTab(int itemId) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(itemId);
        }
    }
}
