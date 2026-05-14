package com.gcepapers.app.ui.pdf;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.ViewModelProvider;

import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.material.snackbar.Snackbar;
import com.gcepapers.app.R;
import com.gcepapers.app.data.db.entity.DownloadedFile;
import com.gcepapers.app.data.db.entity.FavoriteItem;
import com.gcepapers.app.data.db.entity.RecentFile;
import com.gcepapers.app.databinding.ActivityPdfViewerBinding;
import com.gcepapers.app.util.FileUtils;
import com.gcepapers.app.util.NetworkUtils;
import com.gcepapers.app.viewmodel.PdfViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Full-featured PDF viewer with:
 * - Download-on-demand (only when user opens file)
 * - Offline reading from cached files
 * - Pinch/double-tap zoom
 * - Page navigation + counter
 * - Remember last page
 * - Fullscreen mode
 * - Dark mode support
 * - Share + open externally
 * - Keep screen on while reading
 * - Retry on error
 */
public class PdfViewerActivity extends AppCompatActivity
    implements OnLoadCompleteListener, OnPageChangeListener, OnErrorListener {

    public static final String EXTRA_PDF_TITLE = "pdf_title";
    public static final String EXTRA_PDF_URL = "pdf_url";
    private static final String TAG = "PdfViewerActivity";

    private ActivityPdfViewerBinding binding;
    private PdfViewModel viewModel;
    private String pdfTitle;
    private String pdfUrl;
    private File localPdfFile;
    private ProgressDialog progressDialog;
    private int lastPage = 0;
    private boolean isFavorite = false;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityPdfViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Keep screen on while reading
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        pdfTitle = getIntent().getStringExtra(EXTRA_PDF_TITLE);
        pdfUrl = getIntent().getStringExtra(EXTRA_PDF_URL);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(com.gcepapers.app.util.FileUtils.stripPdfExtensionForDisplay(pdfTitle));
        }

        viewModel = new ViewModelProvider(this).get(PdfViewModel.class);

        // Check favorite status
        // Run DB access off the main thread to avoid Room's main-thread assertion
        executor.execute(() -> {
            boolean fav = false;
            try {
                fav = viewModel.isFavorite(pdfUrl);
            } catch (Exception ignored) {}
            boolean finalFav = fav;
            mainHandler.post(() -> {
                isFavorite = finalFav;
                // Refresh options menu so favorite icon reflects the correct state
                invalidateOptionsMenu();
            });
        });

        setupRetryButton();
        loadPdf();
    }

    private void setupRetryButton() {
        binding.retryButton.setOnClickListener(v -> {
            binding.errorLayout.setVisibility(View.GONE);
            loadPdf();
        });
    }

    /**
     * Main PDF loading logic:
     * 1. Check if file already cached locally
     * 2. If yes → open immediately
     * 3. If no → download first, then open
     */
    private void loadPdf() {
        binding.loadingIndicator.setVisibility(View.VISIBLE);
        binding.errorLayout.setVisibility(View.GONE);
        binding.pdfView.setVisibility(View.GONE);

        // Check Room DB for cached file
        executor.execute(() -> {
            DownloadedFile downloaded = viewModel.getDownloadByUrl(pdfUrl);
            if (downloaded != null && downloaded.getLocalPath() != null) {
                File file = new File(downloaded.getLocalPath());
                if (file.exists()) {
                    localPdfFile = file;
                    // Get last page
                    RecentFile recent = null;
                    try {
                        recent = viewModel.getDownloadRepository().getRecentByUrl(pdfUrl);
                    } catch (Exception ignored) {}
                    lastPage = (recent != null) ? recent.getLastPage() : 0;
                    mainHandler.post(this::displayPdfFromFile);
                    return;
                }
            }
            // Need to download
            if (NetworkUtils.isOnline(PdfViewerActivity.this)) {
                mainHandler.post(this::downloadAndOpenPdf);
            } else {
                mainHandler.post(() -> showError(getString(R.string.error_no_internet)));
            }
        });
    }

    private void downloadAndOpenPdf() {
        showProgressDialog();

        executor.execute(() -> {
            try {
                File dir = getExternalFilesDir("pdfs");
                if (dir == null) dir = new File(getFilesDir(), "pdfs");
                if (!dir.exists()) dir.mkdirs();

                String fileName = FileUtils.sanitizeFileName(pdfTitle);
                String fileId = FileUtils.extractDriveFileId(pdfUrl);
                if (fileId != null) fileName = fileId + ".pdf";

                File outputFile = new File(dir, fileName);

                URL url = new URL(pdfUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(60000);
                conn.setInstanceFollowRedirects(true);
                conn.connect();

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new Exception("HTTP " + conn.getResponseCode());
                }

                InputStream input = conn.getInputStream();
                FileOutputStream output = new FileOutputStream(outputFile);
                byte[] buffer = new byte[8192];
                int bytesRead;
                int totalRead = 0;
                int contentLength = conn.getContentLength();

                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                    if (contentLength > 0) {
                        int progress = (int) ((totalRead * 100L) / contentLength);
                        mainHandler.post(() -> updateProgress(progress));
                    }
                }
                output.flush();
                output.close();
                input.close();
                conn.disconnect();

                // Save to DB
                DownloadedFile savedFile = new DownloadedFile(
                    pdfUrl, pdfTitle, outputFile.getAbsolutePath(), "", outputFile.length(),
                    System.currentTimeMillis());
                viewModel.saveDownload(savedFile);

                localPdfFile = outputFile;
                mainHandler.post(() -> {
                    hideProgressDialog();
                    displayPdfFromFile();
                });

            } catch (Exception e) {
                Log.e(TAG, "Download error: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    hideProgressDialog();
                    showError(getString(R.string.error_download_failed) + ": " + e.getMessage());
                });
            }
        });
    }

    private void displayPdfFromFile() {
        if (localPdfFile == null || !localPdfFile.exists()) {
            showError(getString(R.string.error_file_not_found));
            return;
        }

        binding.loadingIndicator.setVisibility(View.GONE);
        binding.pdfView.setVisibility(View.VISIBLE);

        boolean nightMode = (getResources().getConfiguration().uiMode &
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES;

        binding.pdfView.fromFile(localPdfFile)
            .defaultPage(lastPage)
            .onLoad(this)
            .onPageChange(this)
            .onError(this)
            .scrollHandle(new DefaultScrollHandle(this))
            .enableDoubletap(true)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .pageSnap(false)
            .nightMode(nightMode)
            .load();

        // Add to recent files
        recordRecentOpen();
    }

    private void recordRecentOpen() {
        executor.execute(() -> {
            RecentFile recent = new RecentFile(pdfUrl, pdfTitle, "", System.currentTimeMillis(), lastPage, 0);
            viewModel.getDownloadRepository().addOrUpdateRecent(recent);
        });
    }

    @Override
    public void loadComplete(int nbPages) {
        binding.loadingIndicator.setVisibility(View.GONE);
        binding.pageCounter.setVisibility(View.VISIBLE);
        binding.pageCounter.setText(getString(R.string.page_counter, 1, nbPages));
        viewModel.setTotalPages(nbPages);
        viewModel.updateTotalPages(pdfUrl, nbPages);
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        binding.pageCounter.setText(getString(R.string.page_counter, page + 1, pageCount));
        viewModel.setCurrentPage(page);
        viewModel.updateLastPage(pdfUrl, page);
    }

    @Override
    public void onError(Throwable t) {
        binding.loadingIndicator.setVisibility(View.GONE);
        showError(getString(R.string.error_pdf_load_failed));
    }

    private void showError(String message) {
        binding.pdfView.setVisibility(View.GONE);
        binding.loadingIndicator.setVisibility(View.GONE);
        binding.errorLayout.setVisibility(View.VISIBLE);
        binding.errorText.setText(message);
    }

    // ==================== PROGRESS DIALOG ====================

    private void showProgressDialog() {
        mainHandler.post(() -> {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(getString(R.string.downloading));
            progressDialog.setMessage(com.gcepapers.app.util.FileUtils.stripPdfExtensionForDisplay(pdfTitle));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(100);
            progressDialog.setCancelable(false);
            progressDialog.show();
        });
    }

    private void updateProgress(int progress) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setProgress(progress);
        }
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    // ==================== MENU ====================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pdf_menu, menu);
        MenuItem favItem = menu.findItem(R.id.action_favorite);
        if (favItem != null) {
            favItem.setIcon(isFavorite
                ? R.drawable.ic_favorite_filled
                : R.drawable.ic_favorite_outline);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_share) {
            sharePdf();
            return true;
        } else if (id == R.id.action_open_external) {
            openExternally();
            return true;
        } else if (id == R.id.action_favorite) {
            toggleFavorite(item);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sharePdf() {
        if (localPdfFile == null || !localPdfFile.exists()) {
            Snackbar.make(binding.getRoot(), R.string.error_file_not_found, Snackbar.LENGTH_SHORT).show();
            return;
        }
        Uri fileUri = FileProvider.getUriForFile(this,
            getApplicationContext().getPackageName() + ".fileprovider", localPdfFile);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, com.gcepapers.app.util.FileUtils.stripPdfExtensionForDisplay(pdfTitle));
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_pdf)));
    }

    private void openExternally() {
        if (localPdfFile == null || !localPdfFile.exists()) {
            Snackbar.make(binding.getRoot(), R.string.error_file_not_found, Snackbar.LENGTH_SHORT).show();
            return;
        }
        Uri fileUri = FileProvider.getUriForFile(this,
            getApplicationContext().getPackageName() + ".fileprovider", localPdfFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Snackbar.make(binding.getRoot(), R.string.error_no_pdf_viewer, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void toggleFavorite(MenuItem item) {
        if (isFavorite) {
            viewModel.removeFavorite(pdfUrl);
            item.setIcon(R.drawable.ic_favorite_outline);
            isFavorite = false;
            Snackbar.make(binding.getRoot(), R.string.removed_from_favorites, Snackbar.LENGTH_SHORT).show();
        } else {
            FavoriteItem fav = new FavoriteItem(pdfUrl, pdfTitle, "", System.currentTimeMillis());
            viewModel.addFavorite(fav);
            item.setIcon(R.drawable.ic_favorite_filled);
            isFavorite = true;
            Snackbar.make(binding.getRoot(), R.string.added_to_favorites, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
        executor.shutdown();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
