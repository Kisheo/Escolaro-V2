package com.gcepapers.app.ui.downloads;

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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.gcepapers.app.R;
import com.gcepapers.app.data.db.entity.DownloadedFile;
import com.gcepapers.app.data.db.entity.FavoriteItem;
import com.gcepapers.app.databinding.ActivityDownloadsBinding;
import com.gcepapers.app.ui.pdf.PdfViewerActivity;
import com.gcepapers.app.util.FileUtils;
import com.gcepapers.app.viewmodel.DownloadsViewModel;

import java.util.List;

/**
 * Downloads Manager screen showing all locally stored PDFs.
 * Features: list, search, open, delete individual, delete all, storage usage.
 */
public class DownloadsActivity extends AppCompatActivity {

    private ActivityDownloadsBinding binding;
    private DownloadsViewModel viewModel;
    private DownloadedFilesAdapter adapter;

    private enum Filter { ALL, FAVORITES, OFFLINE }
    private Filter currentFilter = Filter.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityDownloadsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.downloads);
        }

        setupRecyclerView();
        setupViewModel();
        setupSearch();
        setupDeleteAll();
        setupFilterToggle();
    }

    private void setupRecyclerView() {
        adapter = new DownloadedFilesAdapter(
            this::openPdf,
            this::confirmDelete
        );
        binding.recyclerDownloads.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerDownloads.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(DownloadsViewModel.class);
        // observe all downloads by default
        observeAllDownloads();
    }

    private void setupFilterToggle() {
        // default select All
        binding.filterToggleGroup.check(R.id.filter_all);
        binding.filterToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return; // ignore unchecks
            if (checkedId == R.id.filter_all) {
                currentFilter = Filter.ALL;
                observeAllDownloads();
            } else if (checkedId == R.id.filter_favorites) {
                currentFilter = Filter.FAVORITES;
                observeFavorites();
            } else if (checkedId == R.id.filter_offline) {
                currentFilter = Filter.OFFLINE;
                observeOffline();
            }
        });
    }

    private void observeAllDownloads() {
        viewModel.getAllDownloads().removeObservers(this);
        viewModel.getAllDownloads().observe(this, this::onDownloadsLoaded);
    }

    private void observeFavorites() {
        viewModel.getFavorites().removeObservers(this);
        viewModel.getFavorites().observe(this, favorites -> {
            if (favorites == null) {
                onDownloadsLoaded(null);
                return;
            }
            List<DownloadedFile> mapped = new java.util.ArrayList<>();
            for (FavoriteItem f : favorites) {
                DownloadedFile df = new DownloadedFile(f.getUrl(), f.getTitle(), null, "", 0L, f.getAddedTime());
                mapped.add(df);
            }
            onDownloadsLoaded(mapped);
        });
    }

    private void observeOffline() {
        viewModel.getOfflineFiles().removeObservers(this);
        viewModel.getOfflineFiles().observe(this, this::onDownloadsLoaded);
    }

    private void onDownloadsLoaded(List<DownloadedFile> files) {
        adapter.submitList(files);
        if (files == null || files.isEmpty()) {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.recyclerDownloads.setVisibility(View.GONE);
            binding.storageInfo.setText(getString(R.string.storage_used, "0 B"));
        } else {
            binding.emptyState.setVisibility(View.GONE);
            binding.recyclerDownloads.setVisibility(View.VISIBLE);
            long totalSize = 0;
            for (DownloadedFile f : files) totalSize += (f.getFileSize() > 0 ? f.getFileSize() : 0);
            binding.storageInfo.setText(getString(R.string.storage_used, FileUtils.formatFileSize(totalSize)));
        }
    }

    private void setupSearch() {
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                // Handle search depending on the active filter
                if (query.isEmpty()) {
                    // Re-observe according to filter
                    if (currentFilter == Filter.FAVORITES) observeFavorites();
                    else if (currentFilter == Filter.OFFLINE) observeOffline();
                    else observeAllDownloads();
                } else {
                    if (currentFilter == Filter.FAVORITES) {
                        // Client-side filter on favorites
                        viewModel.getFavorites().removeObservers(DownloadsActivity.this);
                        viewModel.getFavorites().observe(DownloadsActivity.this, favorites -> {
                            if (favorites == null) { onDownloadsLoaded(null); return; }
                            List<DownloadedFile> mapped = new java.util.ArrayList<>();
                            String q = query.toLowerCase();
                            for (FavoriteItem f : favorites) {
                                if (f.getTitle() != null && f.getTitle().toLowerCase().contains(q)) {
                                    mapped.add(new DownloadedFile(f.getUrl(), f.getTitle(), null, "", 0L, f.getAddedTime()));
                                }
                            }
                            onDownloadsLoaded(mapped);
                        });
                    } else {
                        // Use repository search which searches downloads
                        viewModel.getAllDownloads().removeObservers(DownloadsActivity.this);
                        viewModel.searchDownloads(query).observe(DownloadsActivity.this,
                            DownloadsActivity.this::onDownloadsLoaded);
                    }
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupDeleteAll() {
        binding.deleteAllButton.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_all_downloads)
                .setMessage(R.string.delete_all_confirm)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    viewModel.deleteAllDownloads();
                    Snackbar.make(binding.getRoot(), R.string.all_downloads_deleted, Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
        });
    }

    private void confirmDelete(DownloadedFile file) {
        String title = com.gcepapers.app.util.FileUtils.stripPdfExtensionForDisplay(
            file.getTitle() != null ? file.getTitle() : file.getUrl());
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_download)
            .setMessage(title)
            .setPositiveButton(R.string.delete, (d, w) -> {
                if (currentFilter == Filter.FAVORITES) {
                    // Remove favorite entry
                    viewModel.removeFavorite(file.getUrl());
                    Snackbar.make(binding.getRoot(), R.string.removed_from_favorites, Snackbar.LENGTH_SHORT).show();
                } else {
                    viewModel.deleteDownload(file);
                    Snackbar.make(binding.getRoot(), R.string.download_deleted, Snackbar.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void openPdf(DownloadedFile file) {
        Intent intent = new Intent(this, PdfViewerActivity.class);
        intent.putExtra(PdfViewerActivity.EXTRA_PDF_TITLE, file.getTitle());
        intent.putExtra(PdfViewerActivity.EXTRA_PDF_URL, file.getUrl());
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
