package com.notemaster

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ImageSearchActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: UnsplashImageAdapter
    private val unsplashApi: UnsplashApiService = UnsplashApiClient.instance.create(UnsplashApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_search)

        // Fetch the API key from the strings.xml resource
        val unsplashAccessKey = getString(R.string.unsplash_access_key)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.unsplashRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter with an empty list
        imageAdapter = UnsplashImageAdapter(listOf())
        recyclerView.adapter = imageAdapter

        // Search for photos using a query
        searchPhotos("inspiration", unsplashAccessKey)
    }

    // Function to search for photos from the Unsplash API
    private fun searchPhotos(query: String, accessKey: String) {
        unsplashApi.searchPhotos(query, accessKey).enqueue(object : Callback<UnsplashResponse> {
            override fun onResponse(call: Call<UnsplashResponse>, response: Response<UnsplashResponse>) {
                if (response.isSuccessful) {
                    val photos = response.body()?.results
                    if (photos != null && photos.isNotEmpty()) {
                        // Update adapter with new images from Unsplash
                        imageAdapter.updateImages(photos)
                    } else {
                        Toast.makeText(this@ImageSearchActivity, "No images found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ImageSearchActivity, "Failed to load images", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UnsplashResponse>, t: Throwable) {
                Toast.makeText(this@ImageSearchActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
