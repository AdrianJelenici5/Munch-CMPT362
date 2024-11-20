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

    fun updateData(newRestaurants: List<Business>) {
        restaurants = newRestaurants
        notifyDataSetChanged()  // Notify the adapter that the data has changed
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        // Log.d("XD:", "XD: onCreateViewHolder")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        // Log.d("XD:", "XD: onBindViewHolder")
        val restaurant = restaurants[position]
        holder.bind(restaurant)
    }

    override fun getItemCount(): Int = restaurants.size

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView = itemView.findViewById(R.id.tvName)
        private val restaurantInfoTextView: TextView = itemView.findViewById(R.id.restaurantInfo)
        private val ratingTextView: TextView = itemView.findViewById(R.id.tvReview)
        private val imageView: ImageView = itemView.findViewById(R.id.ivImage)
        private val openOrClosed: ImageView = itemView.findViewById(R.id.openOrClosed)
        // private val openNowStatus: TextView = itemView.findViewById(R.id.openNowStatus)

        fun bind(restaurant: Business) {
            // Log.d("XD:", "XD: binding")
            val price = if (restaurant.price != null) "(${restaurant.price}) " else ""

            for (hours in restaurant.business_hours) {
                if (hours.is_open_now) {
                    openOrClosed.setImageResource(R.drawable.green_dot)
                } else {
                    openOrClosed.setImageResource(R.drawable.red_dot)
                }
            }
//            openNowStatus.text = if (hours.is_open_now) "Yes" else "No"
//            openNowStatus.setTextColor(
//                if (hours.is_open_now) android.graphics.Color.GREEN
//                else android.graphics.Color.RED
//            )

            nameTextView.text = "${restaurant.name} ${price}"
            val address = restaurant.location.address1
            val city = restaurant.location.city
            restaurantInfoTextView.text = "${restaurant.categories[0].title} | ${address}, ${city}"
            ratingTextView.text = "Rating: ${restaurant.rating} / 5  (${restaurant.review_count} User Reviews)"
            Glide.with(itemView.context).load(restaurant.image_url).into(imageView)
            //locationTextView.text = restaurant.location

            itemView.setOnClickListener {
                Log.d("XD:", "XD: Item Clicked")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(restaurant.url))
                itemView.context.startActivity(intent)
            }

//            openWebsiteButton.setOnClickListener {
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(restaurant.url))
//                itemView.context.startActivity(intent)
//            }
        }

    }


}


