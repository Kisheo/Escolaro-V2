package com.gcepapers.app.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents the top-level API response from the GCE Papers API.
 */
public class ApiResponse {

    @SerializedName("generated_at")
    private String generatedAt;

    @SerializedName("categories")
    private List<Category> categories;

    public ApiResponse() {}

    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }

    public List<Category> getCategories() { return categories; }
    public void setCategories(List<Category> categories) { this.categories = categories; }
}
