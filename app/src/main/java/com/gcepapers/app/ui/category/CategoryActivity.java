package com.gcepapers.app.ui.category;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.gcepapers.app.R;
import com.gcepapers.app.ads.AdManager;
import com.gcepapers.app.data.model.Category;
import com.gcepapers.app.data.model.ContentItem;
import com.gcepapers.app.databinding.ActivityCategoryBinding;
import com.gcepapers.app.ui.pdf.PdfViewerActivity;
import com.gcepapers.app.util.NetworkUtils;
import com.gcepapers.app.util.PrefsManager;
import com.gcepapers.app.viewmodel.HomeViewModel;

import java.util.List;

/**
 * Shows contents of a category:
 * - Subcategories in a 2-column grid (if any)
 * - PDFs in a vertical list (if any)
 *
 * Handles recursive navigation through the category tree.
 */
public class CategoryActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_JSON = "category_json";
    public static final String EXTRA_CATEGORY_TITLE = "category_title";

    private ActivityCategoryBinding binding;
    private HomeViewModel viewModel;
    private SubcategoryGridAdapter subcategoryAdapter;
    private ContentListAdapter contentAdapter;
    private Category category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        loadCategory();
        setupRecyclerViews();
        setupViewModel();

        // Show interstitial ad when entering category
        AdManager.getInstance(this).showInterstitialAd(this, null);
    }

    private void loadCategory() {
        String json = getIntent().getStringExtra(EXTRA_CATEGORY_JSON);
        String title = getIntent().getStringExtra(EXTRA_CATEGORY_TITLE);
        if (getSupportActionBar() != null && title != null) {
            getSupportActionBar().setTitle(title);
        }
        if (json != null) {
            category = new Gson().fromJson(json, Category.class);
        }
    }

    private void setupRecyclerViews() {
        // Subcategory grid
        subcategoryAdapter = new SubcategoryGridAdapter(this::openSubcategory);
        binding.recyclerSubcategories.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerSubcategories.setAdapter(subcategoryAdapter);

        // Content list (vertical)
        contentAdapter = new ContentListAdapter(this::openPdf);
        binding.recyclerContent.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerContent.setAdapter(contentAdapter);

        populateContent();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        // Observe downloads to update "Available Offline" indicators
        viewModel.getDownloadRepository().getAllDownloads().observe(this, downloads -> {
            if (contentAdapter != null) {
                contentAdapter.updateDownloadedUrls(downloads);
            }
        });
    }

    private void populateContent() {
        if (category == null) return;

        // Show subcategory grid
        List<Category> subs = category.getSubcategories();
        if (subs != null && !subs.isEmpty()) {
            binding.labelSubcategories.setVisibility(View.VISIBLE);
            binding.recyclerSubcategories.setVisibility(View.VISIBLE);
            subcategoryAdapter.submitList(subs);
        } else {
            binding.labelSubcategories.setVisibility(View.GONE);
            binding.recyclerSubcategories.setVisibility(View.GONE);
        }

        // Show content list
        List<ContentItem> content = category.getContent();
        if (content != null && !content.isEmpty()) {
            binding.labelFiles.setVisibility(View.VISIBLE);
            binding.recyclerContent.setVisibility(View.VISIBLE);
            contentAdapter.submitList(content);
        } else {
            binding.labelFiles.setVisibility(View.GONE);
            binding.recyclerContent.setVisibility(View.GONE);
        }

        // Empty state
        boolean isEmpty = (subs == null || subs.isEmpty()) && (content == null || content.isEmpty());
        binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void openSubcategory(Category sub) {
        Intent intent = new Intent(this, CategoryActivity.class);
        intent.putExtra(EXTRA_CATEGORY_JSON, new Gson().toJson(sub));
        intent.putExtra(EXTRA_CATEGORY_TITLE, sub.getName());
        startActivity(intent);
    }

    private void openPdf(ContentItem item) {
        // Increment session count and check rewarded ad
        PrefsManager prefs = PrefsManager.getInstance(this);
        int count = prefs.incrementAndGetSessionPdfCount();

        if (AdManager.shouldShowRewardedAd(count) && NetworkUtils.isOnline(this)) {
            // Show rewarded interstitial first (only if online)
            AdManager.getInstance(this).showRewardedInterstitialAd(this,
                () -> launchPdfViewer(item));
        } else {
            launchPdfViewer(item);
        }
    }

    private void launchPdfViewer(ContentItem item) {
        Intent intent = new Intent(this, PdfViewerActivity.class);
        intent.putExtra(PdfViewerActivity.EXTRA_PDF_TITLE, item.getTitle());
        intent.putExtra(PdfViewerActivity.EXTRA_PDF_URL, item.getUrl());
        startActivity(intent);
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
