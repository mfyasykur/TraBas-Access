package com.ppb.trabas_access.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.ppb.trabas_access.R
import com.ppb.trabas_access.model.dao.Destination

class CarouselAdapter(
    private val destinations: List<Destination>,
    private val viewPager2: ViewPager2
) : RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.carousel_item, parent, false)
        return CarouselViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {

        val destination = destinations[position]
        holder.tvCarouselTitle.text = destination.name

        Glide.with(holder.itemView.context)
            .load(destination.image)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return destinations.size
    }

    inner class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_carousel)
        val tvCarouselTitle: TextView = itemView.findViewById(R.id.tv_carousel_title)
    }
}
