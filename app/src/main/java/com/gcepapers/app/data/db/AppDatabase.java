package com.gcepapers.app.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.gcepapers.app.data.db.dao.DownloadedFileDao;
import com.gcepapers.app.data.db.dao.FavoriteItemDao;
import com.gcepapers.app.data.db.dao.RecentFileDao;
import com.gcepapers.app.data.db.entity.DownloadedFile;
import com.gcepapers.app.data.db.entity.FavoriteItem;
import com.gcepapers.app.data.db.entity.RecentFile;

/**
 * Room database for the GCE Papers app.
 * Stores downloaded files, favorites, and recent reading history.
 */
@Database(
    entities = {DownloadedFile.class, FavoriteItem.class, RecentFile.class},
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "gce_papers.db";
    private static volatile AppDatabase instance;

    public abstract DownloadedFileDao downloadedFileDao();
    public abstract FavoriteItemDao favoriteItemDao();
    public abstract RecentFileDao recentFileDao();

    /**
     * Returns the singleton database instance.
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    DB_NAME)
                .fallbackToDestructiveMigration()
                .build();
        }
        return instance;
    }
}
