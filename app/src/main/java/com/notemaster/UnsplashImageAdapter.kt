package com.notemaster

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class UnsplashImageAdapter(private var images: List<UnsplashPhoto>) : RecyclerView.Adapter<UnsplashImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.unsplashImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val photo = images[position]

        Glide.with(holder.itemView.context)
            .load(photo.urls.small)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = images.size

    // Method to update the list of images
    fun updateImages(newImages: List<UnsplashPhoto>) {
        images = newImages
        notifyDataSetChanged()  // Notify the adapter that the data has changed
    }
}
