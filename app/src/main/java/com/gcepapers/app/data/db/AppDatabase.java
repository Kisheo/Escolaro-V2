package com.gcepapers.app.data.db;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

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

    private static final String TAG = "AppDatabase";
    private static final String DB_NAME = "gce_papers.db";
    private static volatile AppDatabase instance;

    public abstract DownloadedFileDao downloadedFileDao();
    public abstract FavoriteItemDao favoriteItemDao();
    public abstract RecentFileDao recentFileDao();

    /**
     * Room callback to ensure the SQLite page size supports 16KB pages.
     * On database creation and open we attempt to set PRAGMA page_size = 16384
     * and then VACUUM so the change takes effect. This ensures compatibility
     * on devices/SQLite builds that expect a 16 KB page size.
     */
    private static final RoomDatabase.Callback PAGE_SIZE_CALLBACK = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            try {
                // Attempt to set page size to 16KB and force it to take effect
                db.execSQL("PRAGMA page_size = 16384");
                db.execSQL("VACUUM");
            } catch (Exception e) {
                // Log but don't crash - if this fails, DB will keep default page size
                Log.w(TAG, "Failed to set page_size onCreate", e);
            }
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            try (Cursor cursor = db.query("PRAGMA page_size")) {
                if (cursor.moveToFirst()) {
                    int pageSize = cursor.getInt(0);
                    if (pageSize != 16384) {
                        try {
                            db.execSQL("PRAGMA page_size = 16384");
                            db.execSQL("VACUUM");
                            Log.i(TAG, "Adjusted page_size to 16384 and vacuumed database");
                        } catch (Exception inner) {
                            Log.w(TAG, "Failed to adjust page_size onOpen", inner);
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to read or adjust page_size onOpen", e);
            }
        }
    };

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
                .addCallback(PAGE_SIZE_CALLBACK)
                .build();
        }
        return instance;
    }
}
