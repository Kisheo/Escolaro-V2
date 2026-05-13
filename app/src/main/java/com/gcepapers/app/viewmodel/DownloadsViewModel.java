package com.gcepapers.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.gcepapers.app.data.db.entity.DownloadedFile;
import com.gcepapers.app.data.repository.DownloadRepository;

import java.util.List;

/**
 * ViewModel for the Downloads Manager screen.
 */
public class DownloadsViewModel extends AndroidViewModel {

    private final DownloadRepository downloadRepository;
    private LiveData<List<DownloadedFile>> allDownloads;

    public DownloadsViewModel(@NonNull Application application) {
        super(application);
        downloadRepository = new DownloadRepository(application);
        allDownloads = downloadRepository.getAllDownloads();
    }

    public LiveData<List<DownloadedFile>> getAllDownloads() { return allDownloads; }

    public LiveData<List<DownloadedFile>> searchDownloads(String query) {
        return downloadRepository.searchDownloads(query);
    }

    public void deleteDownload(DownloadedFile file) {
        downloadRepository.deleteDownload(file);
    }

    public void deleteAllDownloads() {
        downloadRepository.deleteAllDownloads();
    }
}
