package com.gcepapers.app.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages app-wide settings via SharedPreferences.
 */
public class PrefsManager {

    private static final String PREFS_NAME = "gce_prefs";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_SESSION_PDF_COUNT = "session_pdf_count";

    private static volatile PrefsManager instance;
    private final SharedPreferences prefs;

    private PrefsManager(Context context) {
        prefs = context.getApplicationContext()
                       .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static PrefsManager getInstance(Context context) {
        if (instance == null) {
            synchronized (PrefsManager.class) {
                if (instance == null) {
                    instance = new PrefsManager(context);
                }
            }
        }
        return instance;
    }

    // Dark mode
    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    // Language ("en" or "fr")
    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, "en");
    }

    public void setLanguage(String lang) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply();
    }

    // Session PDF open count (for rewarded ad logic)
    public int getSessionPdfCount() {
        return prefs.getInt(KEY_SESSION_PDF_COUNT, 0);
    }

    public int incrementAndGetSessionPdfCount() {
        int count = getSessionPdfCount() + 1;
        prefs.edit().putInt(KEY_SESSION_PDF_COUNT, count).apply();
        return count;
    }

    public void resetSessionPdfCount() {
        prefs.edit().putInt(KEY_SESSION_PDF_COUNT, 0).apply();
    }
}
