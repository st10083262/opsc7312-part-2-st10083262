package com.notemaster


import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashApiService {
    @GET("search/photos")
    fun searchPhotos(
        @Query("query") query: String,
        @Query("client_id") clientId: String
    ): Call<UnsplashResponse>
}
