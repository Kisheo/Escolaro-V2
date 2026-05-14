package com.gcepapers.app.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.gcepapers.app.data.db.entity.FavoriteItem;

import java.util.List;

/**
 * DAO for managing favorite PDF files in the database.
 */
@Dao
public interface FavoriteItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FavoriteItem item);

    @Delete
    void delete(FavoriteItem item);

    @Query("DELETE FROM favorites WHERE url = :url")
    void deleteByUrl(String url);

    @Query("SELECT * FROM favorites ORDER BY added_time DESC")
    LiveData<List<FavoriteItem>> getAllFavorites();

    @Query("SELECT * FROM favorites WHERE url = :url LIMIT 1")
    FavoriteItem getByUrl(String url);

    @Query("SELECT COUNT(*) FROM favorites WHERE url = :url")
    int isFavorite(String url);

    @Query("SELECT COUNT(*) FROM favorites")
    int getCount();
}
