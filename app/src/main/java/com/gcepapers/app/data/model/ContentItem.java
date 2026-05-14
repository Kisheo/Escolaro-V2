package com.gcepapers.app.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a PDF content item within a category.
 */
public class ContentItem {

    @SerializedName("title")
    private String title;

    @SerializedName("url")
    private String url;

    @SerializedName("size")
    private long size;

    // Computed fields (not from JSON)
    private String categoryPath;
    private boolean isDownloaded;
    private String localFilePath;

    public ContentItem() {}

    public ContentItem(String title, String url, long size) {
        this.title = title;
        this.url = url;
        this.size = size;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getCategoryPath() { return categoryPath; }
    public void setCategoryPath(String categoryPath) { this.categoryPath = categoryPath; }

    public boolean isDownloaded() { return isDownloaded; }
    public void setDownloaded(boolean downloaded) { isDownloaded = downloaded; }

    public String getLocalFilePath() { return localFilePath; }
    public void setLocalFilePath(String localFilePath) { this.localFilePath = localFilePath; }

    /**
     * Returns a unique identifier for this content item based on its URL.
     */
    public String getUniqueId() {
        if (url != null && !url.isEmpty()) {
            // Extract Google Drive file ID or use URL hash
            if (url.contains("id=")) {
                return url.substring(url.lastIndexOf("id=") + 3);
            }
            return String.valueOf(url.hashCode());
        }
        return String.valueOf(title != null ? title.hashCode() : 0);
    }

    /**
     * Returns a safe filename for local storage.
     */
    public String getSafeFileName() {
        if (title == null || title.isEmpty()) return "document.pdf";
        String safeName = title.replaceAll("[^a-zA-Z0-9.\\-_\\s]", "_")
                               .replaceAll("\\s+", "_")
                               .trim();
        if (!safeName.toLowerCase().endsWith(".pdf")) {
            safeName += ".pdf";
        }
        return safeName;
    }
}
