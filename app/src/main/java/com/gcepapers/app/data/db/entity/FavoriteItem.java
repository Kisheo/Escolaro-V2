package com.gcepapers.app.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Room entity representing a favorite PDF.
 */
@Entity(tableName = "favorites")
public class FavoriteItem {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "url")
    private String url;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "category_path")
    private String categoryPath;

    @ColumnInfo(name = "added_time")
    private long addedTime;

    public FavoriteItem() {}

    public FavoriteItem(@NonNull String url, String title, String categoryPath, long addedTime) {
        this.url = url;
        this.title = title;
        this.categoryPath = categoryPath;
        this.addedTime = addedTime;
    }

    @NonNull
    public String getUrl() { return url; }
    public void setUrl(@NonNull String url) { this.url = url; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategoryPath() { return categoryPath; }
    public void setCategoryPath(String categoryPath) { this.categoryPath = categoryPath; }

    public long getAddedTime() { return addedTime; }
    public void setAddedTime(long addedTime) { this.addedTime = addedTime; }
}
