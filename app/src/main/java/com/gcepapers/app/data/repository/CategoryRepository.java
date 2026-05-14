package com.gcepapers.app.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gcepapers.app.data.api.ApiService;
import com.gcepapers.app.data.api.RetrofitClient;
import com.gcepapers.app.data.model.ApiResponse;
import com.gcepapers.app.data.model.Category;
import com.gcepapers.app.data.model.ContentItem;
import com.gcepapers.app.util.CacheManager;
import com.gcepapers.app.util.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for managing category data.
 * Implements offline-first strategy: load from cache, refresh when needed.
 */
public class CategoryRepository {

    private static final String APP_NAME = "gce-pp";
    private static final String BEARER_TOKEN =
        "Bearer PPp26YsDLt2RI4hGXzPW1KDVt9IbKrw5L2E8RTz3QFYv53FVaiksXGSkdbpqgGGT";

    private final Context context;
    private final CacheManager cacheManager;
    private final ApiService apiService;

    // LiveData fields
    private final MutableLiveData<List<Category>> categoriesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public CategoryRepository(Context context) {
        this.context = context.getApplicationContext();
        this.cacheManager = new CacheManager(context);
        this.apiService = RetrofitClient.getInstance().getApiService();
    }

    public LiveData<List<Category>> getCategories() { return categoriesLiveData; }
    public LiveData<Boolean> isLoading() { return loadingLiveData; }
    public LiveData<String> getError() { return errorLiveData; }

    /**
     * Loads categories using offline-first strategy:
     * 1. If valid cache exists, load immediately from cache.
     * 2. If cache expired or missing and online, fetch from network.
     * 3. If offline and stale cache exists, use stale cache.
     */
    public void loadCategories() {
        // Always show cached data immediately if available
        if (cacheManager.hasCache()) {
            ApiResponse cached = cacheManager.loadCache();
            if (cached != null && cached.getCategories() != null) {
                categoriesLiveData.postValue(cached.getCategories());
            }
        }

        // Refresh from network if cache expired or missing
        if (!cacheManager.isCacheValid()) {
            if (NetworkUtils.isOnline(context)) {
                fetchFromNetwork();
            } else if (!cacheManager.hasCache()) {
                errorLiveData.postValue("No internet connection and no cached data available.");
            }
        }
    }

    /**
     * Forces a network refresh regardless of cache state.
     */
    public void refreshFromNetwork() {
        if (NetworkUtils.isOnline(context)) {
            fetchFromNetwork();
        } else {
            errorLiveData.postValue("No internet connection.");
        }
    }

    private void fetchFromNetwork() {
        loadingLiveData.postValue(true);
        apiService.getCategories(APP_NAME, BEARER_TOKEN)
            .enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    loadingLiveData.postValue(false);
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse body = response.body();
                        cacheManager.saveCache(body);
                        if (body.getCategories() != null) {
                            categoriesLiveData.postValue(body.getCategories());
                        }
                    } else {
                        errorLiveData.postValue("Server error: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    loadingLiveData.postValue(false);
                    errorLiveData.postValue("Network error: " + t.getMessage());
                }
            });
    }

    /**
     * Recursively searches all categories and content for the given query.
     *
     * @param query Search term (case-insensitive)
     * @return A flat list of matching ContentItems and Categories
     */
    public List<Object> search(String query) {
        List<Object> results = new ArrayList<>();
        List<Category> categories = categoriesLiveData.getValue();
        if (categories == null || query == null || query.isEmpty()) return results;
        String lowerQuery = query.toLowerCase().trim();
        for (Category cat : categories) {
            searchRecursive(cat, lowerQuery, results, "");
        }
        return results;
    }

    private void searchRecursive(Category category, String query,
                                  List<Object> results, String path) {
        String catPath = path.isEmpty() ? category.getName() : path + " > " + category.getName();

        // Check if category name matches
        if (category.getName() != null &&
            category.getName().toLowerCase().contains(query)) {
            category.setPath(catPath);
            results.add(category);
        }

        // Search content
        if (category.getContent() != null) {
            for (ContentItem item : category.getContent()) {
                if (item.getTitle() != null &&
                    item.getTitle().toLowerCase().contains(query)) {
                    item.setCategoryPath(catPath);
                    results.add(item);
                }
            }
        }

        // Recurse into subcategories
        if (category.getSubcategories() != null) {
            for (Category sub : category.getSubcategories()) {
                searchRecursive(sub, query, results, catPath);
            }
        }
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }
}
