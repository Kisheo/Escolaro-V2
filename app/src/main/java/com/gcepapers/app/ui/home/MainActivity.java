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

import com.google.android.material.snackbar.Snackbar;
import com.gcepapers.app.R;
import com.gcepapers.app.data.model.Category;
import com.gcepapers.app.databinding.ActivityMainBinding;
import com.gcepapers.app.ui.category.CategoryActivity;
import com.gcepapers.app.ui.search.SearchActivity;
import com.gcepapers.app.ui.settings.SettingsActivity;
import com.gcepapers.app.viewmodel.HomeViewModel;

import java.util.List;

/**
 * Main screen showing the top-level category grid.
 * Uses RecyclerView with GridLayoutManager (2 columns).
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private HomeViewModel viewModel;
    private CategoryGridAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        setupRecyclerView();
        setupViewModel();
        setupSwipeRefresh();
    }

    private void setupRecyclerView() {
        adapter = new CategoryGridAdapter(category -> openCategory(category));
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setHasFixedSize(true);
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
        binding.recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        if (message != null) {
            binding.emptyStateText.setText(message);
        }
    }

    private void openCategory(Category category) {
        Intent intent = new Intent(this, CategoryActivity.class);
        intent.putExtra(CategoryActivity.EXTRA_CATEGORY_JSON,
            new com.google.gson.Gson().toJson(category));
        intent.putExtra(CategoryActivity.EXTRA_CATEGORY_TITLE, category.getName());
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
