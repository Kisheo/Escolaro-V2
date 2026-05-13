package com.gcepapers.app.ads;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.gcepapers.app.R;

/**
 * Centralized AdManager that handles App Open Ads, Interstitial Ads,
 * and Rewarded Interstitial Ads.
 *
 * Features:
 * - Global 30-second cooldown between fullscreen ads (App Open + Interstitial)
 * - Prevents overlapping/consecutive fullscreen ads
 * - Preloads ads for instant display
 * - Graceful fallback if ads unavailable
 * - Lifecycle-safe implementation
 */
public class AdManager {

    private static final String TAG = "AdManager";
    private static final long FULLSCREEN_AD_COOLDOWN_MS = 30_000L; // 30 seconds
    private static final int REWARDED_INTERVAL = 5; // Every 5th PDF

    private static volatile AdManager instance;

    private final Context appContext;
    private InterstitialAd interstitialAd;
    private AppOpenAd appOpenAd;
    private RewardedInterstitialAd rewardedInterstitialAd;

    // Cooldown tracking for fullscreen ads
    private long lastFullscreenAdTimeMs = 0;
    private boolean isShowingAd = false;

    // Ad unit IDs (loaded from strings.xml at runtime)
    private String interstitialAdUnitId;
    private String appOpenAdUnitId;
    private String rewardedInterstitialAdUnitId;

    private AdManager(Context context) {
        this.appContext = context.getApplicationContext();
        loadAdUnitIds();
        MobileAds.initialize(appContext, initializationStatus -> {
            Log.d(TAG, "AdMob initialized");
            preloadAds();
        });
    }

    public static AdManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AdManager.class) {
                if (instance == null) {
                    instance = new AdManager(context);
                }
            }
        }
        return instance;
    }

    private void loadAdUnitIds() {
        interstitialAdUnitId = appContext.getString(R.string.admob_interstitial_id);
        appOpenAdUnitId = appContext.getString(R.string.admob_app_open_id);
        rewardedInterstitialAdUnitId = appContext.getString(R.string.admob_rewarded_interstitial_id);
    }

    // ==================== PRELOADING ====================

    public void preloadAds() {
        preloadInterstitial();
        preloadAppOpen();
        preloadRewardedInterstitial();
    }

    private void preloadInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(appContext, interstitialAdUnitId, adRequest,
            new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd ad) {
                    interstitialAd = ad;
                    Log.d(TAG, "Interstitial loaded");
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError error) {
                    interstitialAd = null;
                    Log.d(TAG, "Interstitial failed: " + error.getMessage());
                }
            });
    }

    private void preloadAppOpen() {
        AdRequest adRequest = new AdRequest.Builder().build();
        AppOpenAd.load(appContext, appOpenAdUnitId, adRequest,
            new AppOpenAd.AppOpenAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull AppOpenAd ad) {
                    appOpenAd = ad;
                    Log.d(TAG, "App open ad loaded");
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError error) {
                    appOpenAd = null;
                    Log.d(TAG, "App open ad failed: " + error.getMessage());
                }
            });
    }

    private void preloadRewardedInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedInterstitialAd.load(appContext, rewardedInterstitialAdUnitId, adRequest,
            new RewardedInterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull RewardedInterstitialAd ad) {
                    rewardedInterstitialAd = ad;
                    Log.d(TAG, "Rewarded interstitial loaded");
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError error) {
                    rewardedInterstitialAd = null;
                    Log.d(TAG, "Rewarded interstitial failed: " + error.getMessage());
                }
            });
    }

    // ==================== COOLDOWN CHECK ====================

    private boolean isFullscreenAdCooledDown() {
        return (System.currentTimeMillis() - lastFullscreenAdTimeMs) >= FULLSCREEN_AD_COOLDOWN_MS;
    }

    private void recordFullscreenAdShown() {
        lastFullscreenAdTimeMs = System.currentTimeMillis();
    }

    // ==================== APP OPEN AD ====================

    /**
     * Shows the App Open Ad if available and cooldown allows.
     */
    public void showAppOpenAd(Activity activity, Runnable onComplete) {
        if (isShowingAd || !isFullscreenAdCooledDown() || appOpenAd == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        isShowingAd = true;
        appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                appOpenAd = null;
                isShowingAd = false;
                preloadAppOpen();
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                appOpenAd = null;
                isShowingAd = false;
                preloadAppOpen();
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onAdShowedFullScreenContent() {
                recordFullscreenAdShown();
            }
        });
        appOpenAd.show(activity);
    }

    // ==================== INTERSTITIAL AD ====================

    /**
     * Shows an interstitial ad if available and cooldown allows.
     * Automatically reloads after display.
     */
    public void showInterstitialAd(Activity activity, Runnable onComplete) {
        if (isShowingAd || !isFullscreenAdCooledDown() || interstitialAd == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        isShowingAd = true;
        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                interstitialAd = null;
                isShowingAd = false;
                preloadInterstitial();
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                interstitialAd = null;
                isShowingAd = false;
                preloadInterstitial();
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onAdShowedFullScreenContent() {
                recordFullscreenAdShown();
            }
        });
        interstitialAd.show(activity);
    }

    // ==================== REWARDED INTERSTITIAL AD ====================

    /**
     * Shows a rewarded interstitial ad (no user opt-in).
     * Called automatically on every 5th PDF open (when online).
     *
     * @param activity   Host activity
     * @param onComplete Called after ad completes (or skipped)
     */
    public void showRewardedInterstitialAd(Activity activity, Runnable onComplete) {
        if (rewardedInterstitialAd == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        rewardedInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                rewardedInterstitialAd = null;
                preloadRewardedInterstitial();
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                rewardedInterstitialAd = null;
                preloadRewardedInterstitial();
                if (onComplete != null) onComplete.run();
            }
        });
        rewardedInterstitialAd.show(activity, rewardItem -> {
            // Reward granted — no action needed
        });
    }

    /**
     * Determines if the rewarded interstitial should show based on session PDF count.
     * Returns true every 5th PDF opened.
     *
     * @param pdfOpenCount Current session PDF count (after incrementing)
     */
    public static boolean shouldShowRewardedAd(int pdfOpenCount) {
        return pdfOpenCount > 0 && pdfOpenCount % REWARDED_INTERVAL == 0;
    }

    public boolean isAdAvailable() {
        return interstitialAd != null || appOpenAd != null;
    }
}
