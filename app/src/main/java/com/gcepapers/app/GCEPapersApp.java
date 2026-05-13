package com.gcepapers.app;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;

import com.gcepapers.app.ads.AdManager;
import com.gcepapers.app.util.PrefsManager;

import java.util.Locale;

/**
 * Application class for GCE Papers.
 * Initializes global singletons and applies user preferences.
 */
public class GCEPapersApp extends Application {

    private static GCEPapersApp instance;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Apply dark mode preference
        PrefsManager prefs = PrefsManager.getInstance(this);
        if (prefs.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Initialize AdMob
        AdManager.getInstance(this);
    }

    public static GCEPapersApp getInstance() {
        return instance;
    }
}
