package com.gcepapers.app.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Room entity representing a downloaded PDF file.
 */
@Entity(tableName = "downloaded_files")
public class DownloadedFile {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "url")
    private String url;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "local_path")
    private String localPath;

    @ColumnInfo(name = "category_path")
    private String categoryPath;

    @ColumnInfo(name = "file_size")
    private long fileSize;

    @ColumnInfo(name = "download_time")
    private long downloadTime;

    public DownloadedFile() {}

    public DownloadedFile(@NonNull String url, String title, String localPath,
                          String categoryPath, long fileSize, long downloadTime) {
        this.url = url;
        this.title = title;
        this.localPath = localPath;
        this.categoryPath = categoryPath;
        this.fileSize = fileSize;
        this.downloadTime = downloadTime;
    }

    @NonNull
    public String getUrl() { return url; }
    public void setUrl(@NonNull String url) { this.url = url; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocalPath() { return localPath; }
    public void setLocalPath(String localPath) { this.localPath = localPath; }

    public String getCategoryPath() { return categoryPath; }
    public void setCategoryPath(String categoryPath) { this.categoryPath = categoryPath; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public long getDownloadTime() { return downloadTime; }
    public void setDownloadTime(long downloadTime) { this.downloadTime = downloadTime; }
}
