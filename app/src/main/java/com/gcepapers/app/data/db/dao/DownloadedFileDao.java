package com.gcepapers.app.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.gcepapers.app.data.db.entity.DownloadedFile;

import java.util.List;

/**
 * DAO for managing downloaded PDF files in the database.
 */
@Dao
public interface DownloadedFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DownloadedFile file);

    @Delete
    void delete(DownloadedFile file);

    @Query("DELETE FROM downloaded_files WHERE url = :url")
    void deleteByUrl(String url);

    @Query("DELETE FROM downloaded_files")
    void deleteAll();

    @Query("SELECT * FROM downloaded_files ORDER BY download_time DESC")
    LiveData<List<DownloadedFile>> getAllDownloads();

    @Query("SELECT * FROM downloaded_files ORDER BY download_time DESC")
    List<DownloadedFile> getAllDownloadsSync();

    @Query("SELECT * FROM downloaded_files WHERE url = :url LIMIT 1")
    DownloadedFile getByUrl(String url);

    @Query("SELECT COUNT(*) FROM downloaded_files")
    int getCount();

    @Query("SELECT SUM(file_size) FROM downloaded_files")
    long getTotalSize();

    @Query("SELECT * FROM downloaded_files WHERE title LIKE '%' || :query || '%' ORDER BY download_time DESC")
    LiveData<List<DownloadedFile>> searchDownloads(String query);
}
