package com.example.munch_cmpt362.ui.swipe

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.munch_cmpt362.Business
import com.example.munch_cmpt362.R

class RestaurantAdapter(private val restaurants: List<Business>) :
    RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = restaurants[position]
        holder.bind(restaurant)
    }

    override fun getItemCount(): Int = restaurants.size

    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.restaurantName)
        private val ratingTextView: TextView = itemView.findViewById(R.id.restaurantRating)
        private val imageView: ImageView = itemView.findViewById(R.id.restaurantImage)
        private val restaurantType: TextView = itemView.findViewById(R.id.restaurantCuisine)
        private val price: TextView = itemView.findViewById(R.id.price)
        private val openWebsiteButton: ImageButton = itemView.findViewById(R.id.openWebsiteButton)

        fun bind(restaurant: Business) {
            nameTextView.text = restaurant.name
            ratingTextView.text = "Rating: ${restaurant.rating} / 5  (${restaurant.review_count} User Reviews)"
            restaurantType.text = restaurant.categories[0].title
            price.text = restaurant.price ?: ""
            price.visibility = if (restaurant.price != null) View.VISIBLE else View.GONE
            openWebsiteButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(restaurant.url))
                itemView.context.startActivity(intent)
            }
            Glide.with(itemView.context).load(restaurant.image_url).into(imageView)
        }
    }
}
