package com.gcepapers.app.ui.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.gcepapers.app.R;
import com.gcepapers.app.databinding.ActivityPrivacyPolicyBinding;

import java.net.URI;
import java.net.URISyntaxException;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private ActivityPrivacyPolicyBinding binding;
    private String privacyHost = null;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.privacy_policy);
        }

        String url = getString(R.string.privacy_policy_url);
        try {
            URI u = new URI(url);
            privacyHost = u.getHost();
        } catch (URISyntaxException ignored) { }

        WebView webView = binding.webview;
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(false);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);

        binding.progress.setVisibility(View.VISIBLE);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                binding.progress.setProgress(newProgress);
                binding.progress.setVisibility(newProgress < 100 ? View.VISIBLE : View.GONE);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri req = request.getUrl();
                String host = req.getHost();
                // If host is same as privacyHost, load inside WebView, otherwise open external browser
                if (privacyHost != null && host != null && host.endsWith(privacyHost)) {
                    return false;
                }
                // External link -> open in browser
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW, req);
                    startActivity(i);
                } catch (Exception e) { }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                binding.progress.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                // Load local fallback
                binding.progress.setVisibility(View.GONE);
                try {
                    view.loadUrl("file:///android_asset/privacy_fallback.html");
                } catch (Exception ignored) {}
            }
        });

        // Back handling using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                WebView w = binding.webview;
                if (w.canGoBack()) {
                    w.goBack();
                } else {
                    finish();
                }
            }
        });

        // Try to load URL; if offline or fails, onReceivedError will load fallback
        webView.loadUrl(url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
