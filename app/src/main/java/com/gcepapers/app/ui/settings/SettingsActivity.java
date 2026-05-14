package com.gcepapers.app.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.gcepapers.app.BuildConfig;
import com.gcepapers.app.R;
import com.gcepapers.app.databinding.ActivitySettingsBinding;
import com.gcepapers.app.ui.downloads.DownloadsActivity;
import com.gcepapers.app.util.CacheManager;
import com.gcepapers.app.util.PrefsManager;

import java.util.Locale;

/**
 * Settings screen with:
 * - Dark mode toggle
 * - Language switch (English / French)
 * - Clear JSON cache
 * - Downloads manager
 * - App version
 * - Privacy policy
 * - Rate app
 * - Share app
 */
public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private PrefsManager prefs;
    private CacheManager cacheManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings);
        }

        prefs = PrefsManager.getInstance(this);
        cacheManager = new CacheManager(this);

        setupDarkMode();
        setupLanguage();
        setupClearCache();
        setupDownloadsManager();
        setupAppInfo();
        setupPrivacyPolicy();
        setupRateApp();
        setupShareApp();
    }

    private void setupDarkMode() {
        binding.switchDarkMode.setChecked(prefs.isDarkMode());
        binding.switchDarkMode.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.setDarkMode(isChecked);
            AppCompatDelegate.setDefaultNightMode(
                isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });
        binding.darkModeRow.setOnClickListener(v ->
            binding.switchDarkMode.setChecked(!binding.switchDarkMode.isChecked()));
    }

    private void setupLanguage() {
        String currentLang = prefs.getLanguage();
        binding.currentLanguage.setText("en".equals(currentLang) ? R.string.lang_english : R.string.lang_french);

        binding.languageRow.setOnClickListener(v -> showLanguageDialog());
    }

    private void showLanguageDialog() {
        String[] options = {getString(R.string.lang_english), getString(R.string.lang_french)};
        int current = "en".equals(prefs.getLanguage()) ? 0 : 1;

        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.select_language)
            .setSingleChoiceItems(options, current, (dialog, which) -> {
                String lang = which == 0 ? "en" : "fr";
                prefs.setLanguage(lang);
                binding.currentLanguage.setText(which == 0 ? R.string.lang_english : R.string.lang_french);
                dialog.dismiss();
                Toast.makeText(this, R.string.restart_for_language, Toast.LENGTH_LONG).show();
            })
            .show();
    }

    private void setupClearCache() {
        binding.clearCacheRow.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.clear_cache)
                .setMessage(R.string.clear_cache_confirm)
                .setPositiveButton(R.string.clear, (d, w) -> {
                    cacheManager.clearCache();
                    Toast.makeText(this, R.string.cache_cleared, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
        });
    }

    private void setupDownloadsManager() {
        binding.downloadsRow.setOnClickListener(v ->
            startActivity(new Intent(this, DownloadsActivity.class)));
    }

    private void setupAppInfo() {
        binding.appVersion.setText(getString(R.string.version, BuildConfig.VERSION_NAME));
    }

    private void setupPrivacyPolicy() {
        binding.privacyPolicyRow.setOnClickListener(v -> {
            String url = getString(R.string.privacy_policy_url);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        });
    }

    private void setupRateApp() {
        binding.rateAppRow.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + getPackageName())));
            } catch (Exception e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        });
    }

    private void setupShareApp() {
        binding.shareAppRow.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_text,
                "https://play.google.com/store/apps/details?id=" + getPackageName()));
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)));
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
