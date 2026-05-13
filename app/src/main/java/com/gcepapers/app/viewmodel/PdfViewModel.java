package com.gcepapers.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gcepapers.app.data.db.entity.DownloadedFile;
import com.gcepapers.app.data.db.entity.FavoriteItem;
import com.gcepapers.app.data.model.ContentItem;
import com.gcepapers.app.data.repository.DownloadRepository;

import java.util.List;

/**
 * ViewModel for the PDF viewer screen.
 * Handles download tracking, favorites, and reading history.
 */
public class PdfViewModel extends AndroidViewModel {

    private final DownloadRepository downloadRepository;
    private final MutableLiveData<Integer> currentPage = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalPages = new MutableLiveData<>(0);

    public PdfViewModel(@NonNull Application application) {
        super(application);
        downloadRepository = new DownloadRepository(application);
    }

    public LiveData<Integer> getCurrentPage() { return currentPage; }
    public LiveData<Integer> getTotalPages() { return totalPages; }

    public void setCurrentPage(int page) { currentPage.setValue(page); }
    public void setTotalPages(int pages) { totalPages.setValue(pages); }

    public void saveDownload(DownloadedFile file) {
        downloadRepository.insertDownload(file);
    }

    public DownloadedFile getDownloadByUrl(String url) {
        return downloadRepository.getDownloadByUrl(url);
    }

    public void addFavorite(FavoriteItem item) {
        downloadRepository.addFavorite(item);
    }

    public void removeFavorite(String url) {
        downloadRepository.removeFavorite(url);
    }

    public boolean isFavorite(String url) {
        return downloadRepository.isFavorite(url);
    }

    public void updateLastPage(String url, int page) {
        downloadRepository.updateLastPage(url, page);
    }

    public void updateTotalPages(String url, int totalPages) {
        downloadRepository.updateTotalPages(url, totalPages);
    }

    public DownloadRepository getDownloadRepository() {
        return downloadRepository;
    }
}
