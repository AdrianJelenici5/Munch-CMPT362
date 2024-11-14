package com.example.munch_cmpt362.ui.swipe

import android.content.Intent
import android.net.Uri
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

class RestaurantAdapter(private var restaurants: List<Business>) :
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

    fun getItem(position: Int): Business {
        return restaurants[position]
    }

    fun updateRestaurants(newRestaurants: List<Business>) {
        this.restaurants = newRestaurants
        notifyDataSetChanged()
    }

    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.restaurantName)
        private val ratingTextView: TextView = itemView.findViewById(R.id.restaurantRating)
        private val imageView: ImageView = itemView.findViewById(R.id.restaurantImage)
        private val restaurantType: TextView = itemView.findViewById(R.id.restaurantCuisine)
        private val price: TextView = itemView.findViewById(R.id.price)
        private val openWebsiteButton: ImageButton = itemView.findViewById(R.id.openWebsiteButton)
        private val phoneNumber: TextView = itemView.findViewById(R.id.phoneNumber)
        private val openNowStatus: TextView = itemView.findViewById(R.id.openNowStatus)
        private val openHoursTable: TableLayout = itemView.findViewById(R.id.openHoursTable)

        fun bind(restaurant: Business) {
            nameTextView.text = restaurant.name
            ratingTextView.text = "Rating: ${restaurant.rating} / 5  (${restaurant.review_count} User Reviews)"
            restaurantType.text = "${restaurant.categories[0].title}"
            price.text = restaurant.price ?: ""
            price.visibility = if (restaurant.price != null) View.VISIBLE else View.GONE

            phoneNumber.text = "Phone #: ${restaurant.phone}"
            phoneNumber.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${restaurant.phone}"))
                itemView.context.startActivity(intent)
            }

            openWebsiteButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(restaurant.url))
                itemView.context.startActivity(intent)
            }
            Glide.with(itemView.context).load(restaurant.image_url).into(imageView)

            // Clear existing rows except the header
            openHoursTable.removeViews(1, openHoursTable.childCount - 1)

            for (hours in restaurant.business_hours) {
                for (openHour in hours.open) {
                    val dayName = getDayName(openHour.day)
                    val formattedStart = formatTime(openHour.start)
                    val formattedEnd = formatTime(openHour.end)
                    val hoursText = if (openHour.is_overnight) "$formattedStart - Next Day $formattedEnd" else "$formattedStart - $formattedEnd"

                    val row = TableRow(itemView.context).apply {
                        setPadding(8, 8, 8, 8)
                        setBackgroundColor(android.graphics.Color.WHITE)
                    }

                    val dayTextView = TextView(itemView.context).apply {
                        text = dayName
                        setTextColor(android.graphics.Color.DKGRAY)
                    }

                    val hoursTextView = TextView(itemView.context).apply {
                        text = hoursText
                        setPadding(16, 0, 0, 0)
                        setTextColor(android.graphics.Color.DKGRAY)
                    }

                    openNowStatus.text = if (hours.is_open_now) "Yes" else "No"
                    openNowStatus.setTextColor(
                        if (hours.is_open_now) android.graphics.Color.GREEN
                        else android.graphics.Color.RED
                    )

                    row.addView(dayTextView)
                    row.addView(hoursTextView)

                    openHoursTable.addView(row)
                }
            }
        }

        private fun getDayName(day: Int): String {
            return when (day) {
                0 -> "Sunday"
                1 -> "Monday"
                2 -> "Tuesday"
                3 -> "Wednesday"
                4 -> "Thursday"
                5 -> "Friday"
                6 -> "Saturday"
                else -> "Unknown"
            }
        }

        private fun formatTime(time: String): String {
            val hour = time.substring(0, 2).toInt()
            val minute = time.substring(2, 4)
            val period = if (hour < 12) "AM" else "PM"
            val formattedHour = if (hour % 12 == 0) 12 else hour % 12
            return "$formattedHour:$minute $period"
        }
    }
}
