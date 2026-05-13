package com.gcepapers.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.gcepapers.app.data.model.Category;
import com.gcepapers.app.data.repository.CategoryRepository;
import com.gcepapers.app.data.repository.DownloadRepository;
import com.gcepapers.app.data.db.entity.RecentFile;

import java.util.List;

/**
 * ViewModel for the Home/Main screen.
 * Exposes category list, loading state, and error state.
 */
public class HomeViewModel extends AndroidViewModel {

    private final CategoryRepository categoryRepository;
    private final DownloadRepository downloadRepository;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        categoryRepository = new CategoryRepository(application);
        downloadRepository = new DownloadRepository(application);
    }

    public LiveData<List<Category>> getCategories() {
        return categoryRepository.getCategories();
    }

    public LiveData<Boolean> isLoading() {
        return categoryRepository.isLoading();
    }

    public LiveData<String> getError() {
        return categoryRepository.getError();
    }

    public LiveData<List<RecentFile>> getContinueReading() {
        return downloadRepository.getContinueReading();
    }

    public void loadCategories() {
        categoryRepository.loadCategories();
    }

    public void refresh() {
        categoryRepository.refreshFromNetwork();
    }

    public CategoryRepository getCategoryRepository() {
        return categoryRepository;
    }

    public DownloadRepository getDownloadRepository() {
        return downloadRepository;
    }
}
