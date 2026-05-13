package com.gcepapers.app.data.repository;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.gcepapers.app.data.db.AppDatabase;
import com.gcepapers.app.data.db.dao.DownloadedFileDao;
import com.gcepapers.app.data.db.dao.FavoriteItemDao;
import com.gcepapers.app.data.db.dao.RecentFileDao;
import com.gcepapers.app.data.db.entity.DownloadedFile;
import com.gcepapers.app.data.db.entity.FavoriteItem;
import com.gcepapers.app.data.db.entity.RecentFile;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for managing local user data: downloads, favorites, and recents.
 */
public class DownloadRepository {

    private final DownloadedFileDao downloadedFileDao;
    private final FavoriteItemDao favoriteItemDao;
    private final RecentFileDao recentFileDao;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public DownloadRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.downloadedFileDao = db.downloadedFileDao();
        this.favoriteItemDao = db.favoriteItemDao();
        this.recentFileDao = db.recentFileDao();
    }

    // ==================== DOWNLOADS ====================

    public LiveData<List<DownloadedFile>> getAllDownloads() {
        return downloadedFileDao.getAllDownloads();
    }

    public void insertDownload(DownloadedFile file) {
        executor.execute(() -> downloadedFileDao.insert(file));
    }

    public void deleteDownload(DownloadedFile file) {
        executor.execute(() -> {
            // Delete physical file
            if (file.getLocalPath() != null) {
                File f = new File(file.getLocalPath());
                if (f.exists()) f.delete();
            }
            downloadedFileDao.delete(file);
        });
    }

    public void deleteDownloadByUrl(String url) {
        executor.execute(() -> {
            DownloadedFile file = downloadedFileDao.getByUrl(url);
            if (file != null) {
                if (file.getLocalPath() != null) {
                    File f = new File(file.getLocalPath());
                    if (f.exists()) f.delete();
                }
                downloadedFileDao.deleteByUrl(url);
            }
        });
    }

    public void deleteAllDownloads() {
        executor.execute(() -> {
            List<DownloadedFile> files = downloadedFileDao.getAllDownloadsSync();
            for (DownloadedFile file : files) {
                if (file.getLocalPath() != null) {
                    File f = new File(file.getLocalPath());
                    if (f.exists()) f.delete();
                }
            }
            downloadedFileDao.deleteAll();
        });
    }

    public DownloadedFile getDownloadByUrl(String url) {
        return downloadedFileDao.getByUrl(url);
    }

    public LiveData<List<DownloadedFile>> searchDownloads(String query) {
        return downloadedFileDao.searchDownloads(query);
    }

    // ==================== FAVORITES ====================

    public LiveData<List<FavoriteItem>> getAllFavorites() {
        return favoriteItemDao.getAllFavorites();
    }

    public void addFavorite(FavoriteItem item) {
        executor.execute(() -> favoriteItemDao.insert(item));
    }

    public void removeFavorite(String url) {
        executor.execute(() -> favoriteItemDao.deleteByUrl(url));
    }

    public boolean isFavorite(String url) {
        return favoriteItemDao.isFavorite(url) > 0;
    }

    // ==================== RECENTS ====================

    public LiveData<List<RecentFile>> getRecentFiles() {
        return recentFileDao.getRecentFiles();
    }

    public LiveData<List<RecentFile>> getContinueReading() {
        return recentFileDao.getContinueReading();
    }

    public void addOrUpdateRecent(RecentFile file) {
        executor.execute(() -> recentFileDao.insert(file));
    }

    public void updateLastPage(String url, int page) {
        executor.execute(() -> recentFileDao.updatePage(url, page, System.currentTimeMillis()));
    }

    public void updateTotalPages(String url, int totalPages) {
        executor.execute(() -> recentFileDao.updateTotalPages(url, totalPages));
    }

    public RecentFile getRecentByUrl(String url) {
        return recentFileDao.getByUrl(url);
    }
}
