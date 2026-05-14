package com.gcepapers.app.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Room entity representing a recently opened PDF file.
 */
@Entity(tableName = "recent_files")
public class RecentFile {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "url")
    private String url;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "category_path")
    private String categoryPath;

    @ColumnInfo(name = "last_opened")
    private long lastOpened;

    @ColumnInfo(name = "last_page")
    private int lastPage;

    @ColumnInfo(name = "total_pages")
    private int totalPages;

    public RecentFile() {}

    public RecentFile(@NonNull String url, String title, String categoryPath,
                      long lastOpened, int lastPage, int totalPages) {
        this.url = url;
        this.title = title;
        this.categoryPath = categoryPath;
        this.lastOpened = lastOpened;
        this.lastPage = lastPage;
        this.totalPages = totalPages;
    }

    @NonNull
    public String getUrl() { return url; }
    public void setUrl(@NonNull String url) { this.url = url; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategoryPath() { return categoryPath; }
    public void setCategoryPath(String categoryPath) { this.categoryPath = categoryPath; }

    public long getLastOpened() { return lastOpened; }
    public void setLastOpened(long lastOpened) { this.lastOpened = lastOpened; }

    public int getLastPage() { return lastPage; }
    public void setLastPage(int lastPage) { this.lastPage = lastPage; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    /**
     * Returns reading progress as a percentage (0-100).
     */
    public int getProgressPercent() {
        if (totalPages <= 0) return 0;
        return Math.min(100, (int) ((lastPage * 100.0) / totalPages));
    }
}
