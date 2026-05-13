package com.gcepapers.app.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.gcepapers.app.data.db.entity.RecentFile;

import java.util.List;

/**
 * DAO for managing recently opened PDF files in the database.
 */
@Dao
public interface RecentFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RecentFile file);

    @Query("DELETE FROM recent_files WHERE url = :url")
    void deleteByUrl(String url);

    @Query("DELETE FROM recent_files")
    void deleteAll();

    @Query("SELECT * FROM recent_files ORDER BY last_opened DESC LIMIT 50")
    LiveData<List<RecentFile>> getRecentFiles();

    @Query("SELECT * FROM recent_files ORDER BY last_opened DESC LIMIT 50")
    List<RecentFile> getRecentFilesSync();

    @Query("SELECT * FROM recent_files WHERE url = :url LIMIT 1")
    RecentFile getByUrl(String url);

    @Query("UPDATE recent_files SET last_page = :page, last_opened = :time WHERE url = :url")
    void updatePage(String url, int page, long time);

    @Query("UPDATE recent_files SET total_pages = :totalPages WHERE url = :url")
    void updateTotalPages(String url, int totalPages);

    @Query("SELECT * FROM recent_files WHERE last_page > 0 AND last_page < total_pages AND total_pages > 1 ORDER BY last_opened DESC LIMIT 10")
    LiveData<List<RecentFile>> getContinueReading();
}
