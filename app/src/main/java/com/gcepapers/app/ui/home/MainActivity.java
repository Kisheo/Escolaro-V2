package com.gcepapers.app.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.gcepapers.app.R;
import com.gcepapers.app.data.model.Category;
import com.gcepapers.app.data.model.ContentItem;
import com.gcepapers.app.data.db.entity.DownloadedFile;
import com.gcepapers.app.data.db.entity.FavoriteItem;
import com.gcepapers.app.databinding.ActivityMainBinding;
import com.gcepapers.app.ui.category.CategoryActivity;
import com.gcepapers.app.ui.pdf.PdfViewerActivity;
import com.gcepapers.app.ads.AdManager;
import com.gcepapers.app.util.NetworkUtils;
import com.gcepapers.app.util.PrefsManager;
import com.gcepapers.app.ui.search.SearchResultsAdapter;
import com.gcepapers.app.ui.search.SearchActivity;
import com.gcepapers.app.ui.settings.SettingsActivity;
import com.gcepapers.app.viewmodel.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Main screen showing the top-level category grid.
 * Uses RecyclerView with GridLayoutManager (2 columns).
 * Added: Toggle filter to view All / Favorites / Offline (favorites/offline show a list of PDFs).
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private HomeViewModel viewModel;
    private CategoryGridAdapter adapter;
    private SearchResultsAdapter searchAdapter;

    // Caches for LiveData values so toggle switches can populate immediately
    private List<FavoriteItem> favoriteCache = new ArrayList<>();
    private List<DownloadedFile> downloadCache = new ArrayList<>();

    private enum MainFilter { ALL, FAVORITES, OFFLINE }
    private MainFilter currentFilter = MainFilter.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        setupAdapters();
        setupViewModel();
        setupSwipeRefresh();
        setupFilterToggle();
    }

    private void setupAdapters() {
        adapter = new CategoryGridAdapter(category -> openCategory(category));
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setHasFixedSize(true);

        searchAdapter = new SearchResultsAdapter(result -> {
            if (result instanceof ContentItem) {
                ContentItem item = (ContentItem) result;
                // Mirror CategoryActivity's ad logic
                PrefsManager prefs = PrefsManager.getInstance(MainActivity.this);
                int count = prefs.incrementAndGetSessionPdfCount();
                if (AdManager.shouldShowRewardedAd(count) && NetworkUtils.isOnline(MainActivity.this)) {
                    AdManager.getInstance(MainActivity.this).showRewardedInterstitialAd(MainActivity.this,
                        () -> openPdf(item));
                } else {
                    openPdf(item);
                }
            } else if (result instanceof Category) {
                openCategory((Category) result);
            }
        });
        binding.resultsRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.resultsRecycler.setAdapter(searchAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        viewModel.getCategories().observe(this, this::onCategoriesLoaded);
        viewModel.isLoading().observe(this, loading -> {
            binding.swipeRefresh.setRefreshing(loading != null && loading);
            binding.shimmerLayout.setVisibility(
                (loading != null && loading && (adapter.getItemCount() == 0))
                    ? View.VISIBLE : View.GONE);
            if (loading != null && loading && adapter.getItemCount() == 0) {
                binding.shimmerLayout.startShimmer();
            } else {
                binding.shimmerLayout.stopShimmer();
            }
        });
        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showEmptyState(true, error);
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.loadCategories();

        // Observe downloads/favorites to populate caches and update UI when the filter is active
        viewModel.getDownloadRepository().getAllFavorites().observe(this, favs -> {
            favoriteCache = (favs != null) ? favs : new ArrayList<>();
            if (currentFilter == MainFilter.FAVORITES) {
                showFavorites(favoriteCache);
            }
        });
        viewModel.getDownloadRepository().getAllDownloads().observe(this, downloads -> {
            downloadCache = (downloads != null) ? downloads : new ArrayList<>();
            if (currentFilter == MainFilter.OFFLINE) {
                showOffline(downloadCache);
            }
            // Also update adapters in category screens via existing observers elsewhere
        });
    }

    private void onCategoriesLoaded(List<Category> categories) {
        binding.shimmerLayout.stopShimmer();
        binding.shimmerLayout.setVisibility(View.GONE);
        if (categories == null || categories.isEmpty()) {
            showEmptyState(true, getString(R.string.empty_categories));
        } else {
            showEmptyState(false, null);
            adapter.submitList(categories);
        }
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());
    }

    private void showEmptyState(boolean show, String message) {
        binding.emptyStateLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        // Show/hide grid/results accordingly
        binding.recyclerView.setVisibility((!show && currentFilter == MainFilter.ALL) ? View.VISIBLE : View.GONE);
        binding.resultsRecycler.setVisibility((!show && currentFilter != MainFilter.ALL) ? View.VISIBLE : View.GONE);
        if (message != null) {
            binding.emptyStateText.setText(message);
        }
    }

    private void setupFilterToggle() {
        binding.mainFilterToggle.check(R.id.main_filter_all);
        binding.mainFilterToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.main_filter_all) {
                currentFilter = MainFilter.ALL;
                // show categories
                binding.resultsRecycler.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
                showEmptyState(adapter.getItemCount() == 0, getString(R.string.empty_categories));
            } else if (checkedId == R.id.main_filter_favorites) {
                currentFilter = MainFilter.FAVORITES;
                binding.recyclerView.setVisibility(View.GONE);
                binding.resultsRecycler.setVisibility(View.VISIBLE);
                // Populate immediately from cache filled by persistent observer
                showFavorites(favoriteCache);
            } else if (checkedId == R.id.main_filter_offline) {
                currentFilter = MainFilter.OFFLINE;
                binding.recyclerView.setVisibility(View.GONE);
                binding.resultsRecycler.setVisibility(View.VISIBLE);
                showOffline(downloadCache);
              }
          });
      }

    private void showFavorites(List<FavoriteItem> favorites) {
        if (favorites == null || favorites.isEmpty()) {
            showEmptyState(true, getString(R.string.no_results));
            return;
        }
        // Map favorites to ContentItem placeholders
        List<Object> list = new ArrayList<>();
        for (FavoriteItem f : favorites) {
            ContentItem ci = new ContentItem();
            ci.setTitle(f.getTitle());
            ci.setUrl(f.getUrl());
            ci.setSize(0);
            ci.setCategoryPath("");
            list.add(ci);
        }
        searchAdapter.submitResults(list, "");
        showEmptyState(false, null);
    }

    private void showOffline(List<DownloadedFile> downloads) {
        if (downloads == null || downloads.isEmpty()) {
            showEmptyState(true, getString(R.string.no_downloads));
            return;
        }
        List<Object> list = new ArrayList<>();
        for (DownloadedFile d : downloads) {
            ContentItem ci = new ContentItem();
            ci.setTitle(d.getTitle());
            ci.setUrl(d.getUrl());
            ci.setSize(d.getFileSize());
            ci.setCategoryPath("");
            ci.setDownloaded(true);
            ci.setLocalFilePath(d.getLocalPath());
            list.add(ci);
        }
        searchAdapter.submitResults(list, "");
        showEmptyState(false, null);
    }

    private void openCategory(Category category) {
        Intent intent = new Intent(this, CategoryActivity.class);
        intent.putExtra(CategoryActivity.EXTRA_CATEGORY_JSON,
            new com.google.gson.Gson().toJson(category));
        intent.putExtra(CategoryActivity.EXTRA_CATEGORY_TITLE, category.getName());
        startActivity(intent);
    }

    private void openPdf(ContentItem item) {
        Intent intent = new Intent(this, PdfViewerActivity.class);
        intent.putExtra(PdfViewerActivity.EXTRA_PDF_TITLE, item.getTitle());
        intent.putExtra(PdfViewerActivity.EXTRA_PDF_URL, item.getUrl());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
