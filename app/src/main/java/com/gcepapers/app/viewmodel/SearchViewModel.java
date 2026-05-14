package com.gcepapers.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gcepapers.app.data.model.ContentItem;
import com.gcepapers.app.data.model.Category;
import com.gcepapers.app.data.repository.CategoryRepository;

import java.util.List;

/**
 * ViewModel for search functionality.
 * Performs recursive search through all categories and PDFs.
 */
public class SearchViewModel extends AndroidViewModel {

    private final CategoryRepository categoryRepository;
    private final MutableLiveData<List<Object>> searchResults = new MutableLiveData<>();
    private final MutableLiveData<Boolean> searching = new MutableLiveData<>(false);
    private String lastQuery = "";

    public SearchViewModel(@NonNull Application application) {
        super(application);
        categoryRepository = new CategoryRepository(application);
        // Load categories so search has data to work with
        categoryRepository.loadCategories();
    }

    public LiveData<List<Object>> getSearchResults() { return searchResults; }
    public LiveData<Boolean> isSearching() { return searching; }

    public void search(String query) {
        if (query == null || query.trim().isEmpty()) {
            searchResults.postValue(null);
            return;
        }
        if (query.equals(lastQuery)) return;
        lastQuery = query;
        searching.postValue(true);
        new Thread(() -> {
            List<Object> results = categoryRepository.search(query);
            searchResults.postValue(results);
            searching.postValue(false);
        }).start();
    }

    public void clearSearch() {
        lastQuery = "";
        searchResults.setValue(null);
    }
}
