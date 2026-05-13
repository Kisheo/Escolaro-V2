package com.gcepapers.app.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents a category in the GCE Papers hierarchy.
 * Categories can be nested recursively to unlimited depth.
 */
public class Category {

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("icon")
    private String icon;

    @SerializedName("content")
    private List<ContentItem> content;

    @SerializedName("subcategories")
    private List<Category> subcategories;

    // Computed path for navigation (not from JSON)
    private String path;

    public Category() {}

    public Category(String name, String description, String icon,
                    List<ContentItem> content, List<Category> subcategories) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.content = content;
        this.subcategories = subcategories;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public List<ContentItem> getContent() { return content; }
    public void setContent(List<ContentItem> content) { this.content = content; }

    public List<Category> getSubcategories() { return subcategories; }
    public void setSubcategories(List<Category> subcategories) { this.subcategories = subcategories; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    /**
     * Returns true if this category has subcategories.
     */
    public boolean hasSubcategories() {
        return subcategories != null && !subcategories.isEmpty();
    }

    /**
     * Returns true if this category has content items.
     */
    public boolean hasContent() {
        return content != null && !content.isEmpty();
    }

    /**
     * Returns the total number of PDF files in this category and all subcategories recursively.
     */
    public int getTotalPdfCount() {
        int count = 0;
        if (content != null) {
            count += content.size();
        }
        if (subcategories != null) {
            for (Category sub : subcategories) {
                count += sub.getTotalPdfCount();
            }
        }
        return count;
    }
}
