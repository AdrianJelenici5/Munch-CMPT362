package com.example.munch_cmpt362.ui.reviews

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.munch_cmpt362.Business
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.ui.swipe.RestaurantAdapter.RestaurantViewHolder

class ReviewAdapter(private var restaurants: List<Business>) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        Log.d("XD:", "XD: onCreateViewHolder")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        Log.d("XD:", "XD: onBindViewHolder")
        val restaurant = restaurants[position]
        holder.bind(restaurant)
    }

    override fun getItemCount(): Int = restaurants.size

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView = itemView.findViewById(R.id.tvName)
        private val ratingTextView: TextView = itemView.findViewById(R.id.tvReview)
        private val imageView: ImageView = itemView.findViewById(R.id.ivImage)

        fun bind(restaurant: Business) {
            Log.d("XD:", "XD: binding")
            nameTextView.text = restaurant.name
            ratingTextView.text = "Rating: ${restaurant.rating} / 5  (${restaurant.review_count} User Reviews)"
            }
        }


}


