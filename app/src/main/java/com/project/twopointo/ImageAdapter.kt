package com.project.twopointo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide

class URLImageAdapter(private var imageUrls: List<String>, private val viewPager: ViewPager2) : RecyclerView.Adapter<URLImageAdapter.ViewHolder>() {


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_ormawa)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_container, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val actualPosition = position % imageUrls.size
        val imageUrl = imageUrls[actualPosition]
        Glide.with(holder.imageView.context)
            .load(imageUrl)
            .override(1080, 1360)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }

    fun updateImages(newImageUrls: List<String>) {
        this.imageUrls = newImageUrls
        notifyDataSetChanged()
    }
}

