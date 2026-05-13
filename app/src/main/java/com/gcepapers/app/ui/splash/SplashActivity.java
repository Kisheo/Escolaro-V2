package com.gcepapers.app.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.gcepapers.app.ads.AdManager;
import com.gcepapers.app.databinding.ActivitySplashBinding;
import com.gcepapers.app.ui.home.MainActivity;

/**
 * Splash screen shown on app launch.
 * Uses the AndroidX SplashScreen API for a modern native splash.
 * Optionally shows an App Open Ad before transitioning to MainActivity.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1500L;
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Attempt to show App Open Ad, then proceed to MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            AdManager adManager = AdManager.getInstance(this);
            adManager.showAppOpenAd(this, this::navigateToMain);
        }, SPLASH_DELAY_MS);
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
