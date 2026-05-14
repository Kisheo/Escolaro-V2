package com.gcepapers.app.data.api;

import com.gcepapers.app.data.model.ApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Retrofit service interface for the GCE Papers API.
 */
public interface ApiService {

    /**
     * Fetches the full category tree and PDF content list.
     *
     * @param appName  The app identifier (e.g., "gce-pp")
     * @param authToken Bearer token for authorization
     * @return A Call wrapping the ApiResponse
     */
    @GET("api.php")
    Call<ApiResponse> getCategories(
        @Query("app_name") String appName,
        @Header("Authorization") String authToken
    );
}
