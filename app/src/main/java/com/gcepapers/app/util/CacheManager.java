package com.gcepapers.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.gcepapers.app.data.model.ApiResponse;

/**
 * Manages local JSON cache with a 7-day expiry.
 * Stores the raw API response to disk via SharedPreferences for quick retrieval.
 */
public class CacheManager {

    private static final String PREFS_NAME = "gce_cache";
    private static final String KEY_JSON_CACHE = "json_cache";
    private static final String KEY_CACHE_TIMESTAMP = "cache_timestamp";
    private static final long CACHE_DURATION_MS = 7L * 24 * 60 * 60 * 1000; // 7 days

    private final SharedPreferences prefs;
    private final Gson gson;

    public CacheManager(Context context) {
        prefs = context.getApplicationContext()
                       .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    /**
     * Saves the API response to local cache with current timestamp.
     */
    public void saveCache(ApiResponse response) {
        try {
            String json = gson.toJson(response);
            prefs.edit()
                 .putString(KEY_JSON_CACHE, json)
                 .putLong(KEY_CACHE_TIMESTAMP, System.currentTimeMillis())
                 .apply();
        } catch (Exception e) {
            // Ignore serialization errors
        }
    }

    /**
     * Loads cached API response from disk.
     *
     * @return Parsed ApiResponse or null if no cache exists.
     */
    public ApiResponse loadCache() {
        String json = prefs.getString(KEY_JSON_CACHE, null);
        if (json == null || json.isEmpty()) return null;
        try {
            return gson.fromJson(json, ApiResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns true if a valid (non-expired) cache exists.
     */
    public boolean isCacheValid() {
        long timestamp = prefs.getLong(KEY_CACHE_TIMESTAMP, 0);
        if (timestamp == 0) return false;
        return (System.currentTimeMillis() - timestamp) < CACHE_DURATION_MS;
    }

    /**
     * Returns true if any cache exists (even if expired).
     */
    public boolean hasCache() {
        String json = prefs.getString(KEY_JSON_CACHE, null);
        return json != null && !json.isEmpty();
    }

    /**
     * Clears the JSON cache.
     */
    public void clearCache() {
        prefs.edit()
             .remove(KEY_JSON_CACHE)
             .remove(KEY_CACHE_TIMESTAMP)
             .apply();
    }

    /**
     * Returns the cache age in milliseconds, or -1 if no cache.
     */
    public long getCacheAgeMs() {
        long timestamp = prefs.getLong(KEY_CACHE_TIMESTAMP, 0);
        if (timestamp == 0) return -1;
        return System.currentTimeMillis() - timestamp;
    }
}
