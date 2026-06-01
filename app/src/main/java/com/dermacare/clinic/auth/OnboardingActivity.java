package com.dermacare.clinic.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.dermacare.clinic.R;
import com.dermacare.clinic.util.SessionManager;
import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private MaterialButton btnNext;
    private LinearLayout dotsIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        dotsIndicator = findViewById(R.id.dotsIndicator);

        OnboardingAdapter adapter = new OnboardingAdapter();
        viewPager.setAdapter(adapter);
        setupDots(adapter.getItemCount());
        updateDots(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
                boolean last = position == adapter.getItemCount() - 1;
                btnNext.setText(last ? R.string.get_started : R.string.next);
            }
        });

        findViewById(R.id.btnSkip).setOnClickListener(v -> finishOnboarding());
        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < adapter.getItemCount() - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                finishOnboarding();
            }
        });
    }

    private void setupDots(int count) {
        dotsIndicator.removeAllViews();
        int margin = (int) (6 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            int sizeActive = (int) (10 * getResources().getDisplayMetrics().density);
            int sizeInactive = (int) (8 * getResources().getDisplayMetrics().density);
            int size = sizeInactive;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(margin, 0, margin, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.dot_inactive);
            dotsIndicator.addView(dot);
        }
    }

    private void updateDots(int position) {
        for (int i = 0; i < dotsIndicator.getChildCount(); i++) {
            View dot = dotsIndicator.getChildAt(i);
            boolean active = i == position;
            dot.setBackgroundResource(active ? R.drawable.dot_active : R.drawable.dot_inactive);
            ViewGroup.LayoutParams params = dot.getLayoutParams();
            int size = (int) ((active ? 10 : 8) * getResources().getDisplayMetrics().density);
            params.width = size;
            params.height = size;
            dot.setLayoutParams(params);
            dot.setAlpha(active ? 1f : 0.6f);
        }
    }

    private void finishOnboarding() {
        new SessionManager(this).setOnboarded(true);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
