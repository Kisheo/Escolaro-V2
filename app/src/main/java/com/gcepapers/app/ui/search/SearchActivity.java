package com.gcepapers.app.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gcepapers.app.R;
import com.gcepapers.app.data.model.Category;
import com.gcepapers.app.data.model.ContentItem;
import com.gcepapers.app.databinding.ActivitySearchBinding;
import com.gcepapers.app.ui.category.CategoryActivity;
import com.gcepapers.app.ui.pdf.PdfViewerActivity;
import com.gcepapers.app.ads.AdManager;
import com.gcepapers.app.util.NetworkUtils;
import com.gcepapers.app.util.PrefsManager;
import com.gcepapers.app.viewmodel.SearchViewModel;

import java.util.List;

/**
 * Search screen with instant local searching through all categories and PDFs.
 * Highlights matches and navigates to category or PDF on selection.
 */
public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private SearchViewModel viewModel;
    private SearchResultsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.search);
        }

        setupRecyclerView();
        setupViewModel();
        setupSearchInput();
    }

    private void setupRecyclerView() {
        adapter = new SearchResultsAdapter(this::onResultClick);
        binding.recyclerResults.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerResults.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        viewModel.getSearchResults().observe(this, this::onSearchResults);
        viewModel.isSearching().observe(this, searching -> {
            binding.progressBar.setVisibility(searching != null && searching ? View.VISIBLE : View.GONE);
        });
    }

    private void setupSearchInput() {
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    adapter.submitList(null);
                    binding.emptyState.setVisibility(View.GONE);
                    binding.emptyStateText.setText(R.string.search_hint);
                    binding.emptyState.setVisibility(View.VISIBLE);
                } else {
                    viewModel.search(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.searchInput.requestFocus();
        binding.emptyStateText.setText(R.string.search_hint);
        binding.emptyState.setVisibility(View.VISIBLE);
    }

    private void onSearchResults(List<Object> results) {
        if (results == null || results.isEmpty()) {
            String query = binding.searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                binding.emptyStateText.setText(R.string.no_results);
                binding.emptyState.setVisibility(View.VISIBLE);
            }
            adapter.submitResults(null, query);
            binding.recyclerResults.setVisibility(View.GONE);
        } else {
            binding.emptyState.setVisibility(View.GONE);
            binding.recyclerResults.setVisibility(View.VISIBLE);
            String query = binding.searchInput.getText().toString().trim();
            adapter.submitResults(results, query);
        }
    }

    private void onResultClick(Object result) {
        if (result instanceof ContentItem) {
            ContentItem item = (ContentItem) result;
            PrefsManager prefs = PrefsManager.getInstance(this);
            int count = prefs.incrementAndGetSessionPdfCount();

            if (AdManager.shouldShowRewardedAd(count) && NetworkUtils.isOnline(this)) {
                AdManager.getInstance(this).showRewardedInterstitialAd(this,
                    () -> openPdf(item));
            } else {
                openPdf(item);
            }
        } else if (result instanceof Category) {
            Category cat = (Category) result;
            Intent intent = new Intent(this, CategoryActivity.class);
            intent.putExtra(CategoryActivity.EXTRA_CATEGORY_JSON,
                new com.google.gson.Gson().toJson(cat));
            intent.putExtra(CategoryActivity.EXTRA_CATEGORY_TITLE, cat.getName());
            startActivity(intent);
        }
    }

    private void openPdf(ContentItem item) {
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
