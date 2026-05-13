package com.gcepapers.app.ui.downloads;

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
import com.gcepapers.app.databinding.ActivityDownloadsBinding;
import com.gcepapers.app.ui.pdf.PdfViewerActivity;
import com.gcepapers.app.util.FileUtils;
import com.gcepapers.app.viewmodel.DownloadsViewModel;

import android.content.Intent;

import java.util.List;

/**
 * Downloads Manager screen showing all locally stored PDFs.
 * Features: list, search, open, delete individual, delete all, storage usage.
 */
public class DownloadsActivity extends AppCompatActivity {

    private ActivityDownloadsBinding binding;
    private DownloadsViewModel viewModel;
    private DownloadedFilesAdapter adapter;

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
    }

    private void setupRecyclerView() {
        adapter = new DownloadedFilesAdapter(
            item -> openPdf(item),
            item -> confirmDelete(item)
        );
        binding.recyclerDownloads.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerDownloads.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(DownloadsViewModel.class);
        viewModel.getAllDownloads().observe(this, this::onDownloadsLoaded);
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
            for (DownloadedFile f : files) totalSize += f.getFileSize();
            binding.storageInfo.setText(getString(R.string.storage_used, FileUtils.formatFileSize(totalSize)));
        }
    }

    private void setupSearch() {
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    viewModel.getAllDownloads().removeObservers(DownloadsActivity.this);
                    viewModel.getAllDownloads().observe(DownloadsActivity.this,
                        DownloadsActivity.this::onDownloadsLoaded);
                } else {
                    viewModel.getAllDownloads().removeObservers(DownloadsActivity.this);
                    viewModel.searchDownloads(query).observe(DownloadsActivity.this,
                        DownloadsActivity.this::onDownloadsLoaded);
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
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_download)
            .setMessage(file.getTitle())
            .setPositiveButton(R.string.delete, (d, w) -> {
                viewModel.deleteDownload(file);
                Snackbar.make(binding.getRoot(), R.string.download_deleted, Snackbar.LENGTH_SHORT).show();
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
