package com.example.munch_cmpt362.ui.swipe

import RestaurantAdapter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import com.example.munch_cmpt362.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.munch_cmpt362.data.local.database.MunchDatabase
import com.example.munch_cmpt362.data.local.dao.RestaurantDao
import dagger.hilt.android.AndroidEntryPoint
import munch_cmpt362.database.restaurants.RestaurantRepository
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@AndroidEntryPoint
class SwipeFragment : Fragment() {
    private lateinit var cardStackView: CardStackView
    private lateinit var title: TextView
    private lateinit var noMoreRestaurantsText: TextView
    private lateinit var decisionIcon: ImageView
    private lateinit var cardStackLayoutManager: CardStackLayoutManager
    private lateinit var restaurantAdapter: RestaurantAdapter
    private lateinit var expandSearchButton: Button

    private val swipeViewModel: SwipeViewModel by viewModels()

    private lateinit var database: MunchDatabase
    private lateinit var databaseDao: RestaurantDao
    private lateinit var repository: RestaurantRepository

    private var lat: Double = 0.0
    private var lng: Double = 0.0

    private val db = Firebase.firestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_swipe, container, false)
        initializeUI(view)
        initializeRepository()
        setupCardStackView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            val isButtonVisible = it.getBoolean("expandSearchButtonVisible", false)
            expandSearchButton.visibility = if (isButtonVisible) View.VISIBLE else View.GONE
        }

        swipeViewModel.fetchRestaurants(isFakeData = false, lat, lng, databaseDao)
        observeViewModel()
    }

    private fun initializeUI(view: View) {
        title = view.findViewById(R.id.title)
        cardStackView = view.findViewById(R.id.card_stack_view)
        noMoreRestaurantsText = view.findViewById(R.id.noMoreRestaurantsText)
        decisionIcon = view.findViewById(R.id.decision_icon)
        expandSearchButton = view.findViewById(R.id.expandSearchButton)
        restaurantAdapter = RestaurantAdapter()
        cardStackView.adapter = restaurantAdapter

        expandSearchButton.setOnClickListener {
            expandSearch()
        }
    }

    private fun initializeRepository() {
        database = MunchDatabase.getInstance(requireContext())
        databaseDao = database.restaurantDao
        repository = RestaurantRepository(databaseDao)
    }

    fun expandSearch() {
        expandSearchButton.visibility = View.GONE
        noMoreRestaurantsText.visibility = View.GONE

        Log.d("JP:", "old coords: ${lat}, ${lng}")
        val randomCoord = generateRandomCoordinate(lat, lng, 10000.0)

        // Update lat/lng with the new randomized location
        val newLat = randomCoord.first
        val newLng = randomCoord.second

        Log.d("JP:", "new coords: ${newLat}, ${newLng}")

        swipeViewModel.fetchRestaurantsWithExcludeList(newLat, newLng, databaseDao) { flag ->
            Log.d("JP:", "flag value: $flag")
            if (flag) {
                noMoreRestaurantsText.visibility = View.VISIBLE
                val layoutParams = noMoreRestaurantsText.layoutParams as RelativeLayout.LayoutParams
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
                noMoreRestaurantsText.layoutParams = layoutParams
                expandSearchButton.visibility = View.GONE
            }
        }
    }

    private fun generateRandomCoordinate(lat: Double, lng: Double, radiusMeters: Double): Pair<Double, Double> {
        val earthRadius = 6371000.0 // Earth radius in meters

        // Randomize distance and angle
        val randomDistance = sqrt(Math.random()) * radiusMeters // sqrt for uniform distribution
        val randomAngle = Math.random() * 2 * Math.PI // Angle in radians (0 to 2π)

        // Convert center point to radians
        val latRad = Math.toRadians(lat)
        val lngRad = Math.toRadians(lng)

        // Calculate new latitude in radians
        val newLatRad = asin(
            sin(latRad) * cos(randomDistance / earthRadius) +
                    cos(latRad) * sin(randomDistance / earthRadius) * cos(randomAngle)
        )

        // Calculate new longitude in radians
        val newLngRad = lngRad + atan2(
            sin(randomAngle) * sin(randomDistance / earthRadius) * cos(latRad),
            cos(randomDistance / earthRadius) - sin(latRad) * sin(newLatRad)
        )

        // Convert back to degrees
        val newLat = Math.toDegrees(newLatRad)
        val newLng = Math.toDegrees(newLngRad)

        return Pair(newLat, newLng)
    }

    private fun setupCardStackView() {
        cardStackLayoutManager = CardStackLayoutManager(requireContext(), object : CardStackListener {
            override fun onCardDragging(direction: Direction?, ratio: Float) {
                handleCardDragging(direction, ratio)
            }

            override fun onCardSwiped(direction: Direction?) {
                handleCardSwiped(direction)
            }

            override fun onCardRewound() {}
            override fun onCardCanceled() {
                decisionIcon.visibility = View.GONE
            }

            override fun onCardAppeared(view: View?, position: Int) {}
            override fun onCardDisappeared(view: View?, position: Int) {}
        })

        cardStackLayoutManager.apply {
            setStackFrom(StackFrom.None)
            setVisibleCount(3)
            setTranslationInterval(8.0f)
            setScaleInterval(0.95f)
            setSwipeThreshold(0.3f)
            setMaxDegree(20.0f)
            setDirections(Direction.HORIZONTAL)
            setCanScrollHorizontal(true)
            setCanScrollVertical(false)
        }

        cardStackView.layoutManager = cardStackLayoutManager
    }

    private fun handleCardDragging(direction: Direction?, ratio: Float) {
        when (direction) {
            Direction.Right -> {
                updateDecisionIcon(R.drawable.ic_thumbs_up, Color.GREEN, ratio)
            }
            Direction.Left -> {
                updateDecisionIcon(R.drawable.ic_thumbs_down, Color.RED, ratio)
            }
            else -> decisionIcon.visibility = View.GONE
        }
    }

    private fun handleCardSwiped(direction: Direction?) {
        val position = cardStackLayoutManager.topPosition - 1
        val swipedRestaurant = restaurantAdapter.getItemAtPosition(position)

        when (direction) {
            Direction.Right -> updateRestaurantScore(swipedRestaurant.id, 20)
            Direction.Left -> updateRestaurantScore(swipedRestaurant.id, -20)
            else -> {}
        }

//        Log.d("JP:", "2. removing restaurants on swiped")
        swipeViewModel.queueRestaurantForRemoval(swipedRestaurant.id)

        decisionIcon.visibility = View.GONE

        if (cardStackLayoutManager.topPosition == restaurantAdapter.itemCount) {
            noMoreRestaurantsText.visibility = View.VISIBLE
            expandSearchButton.visibility = View.VISIBLE
        }
    }

    private fun updateDecisionIcon(iconRes: Int, color: Int, alpha: Float) {
        decisionIcon.setImageResource(iconRes)
        decisionIcon.setColorFilter(color)
        decisionIcon.visibility = View.VISIBLE
        decisionIcon.alpha = alpha
    }

    private fun updateRestaurantScore(restaurantId: String, scoreDelta: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val currentScore = repository.getScoreById(restaurantId)
            repository.updateScore(restaurantId, currentScore + scoreDelta)
        }
    }

    private fun observeViewModel() {
        swipeViewModel.restaurants.distinctUntilChanged().observe(viewLifecycleOwner) { restaurants ->
            if (restaurants.isNotEmpty()) {
//                Log.d("JP:", "1. observed new restaurant and update to adapter")
                restaurantAdapter.submitList(restaurants)
                noMoreRestaurantsText.visibility = View.GONE
            } else {
                noMoreRestaurantsText.visibility = View.VISIBLE
            }
        }
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        lat = latitude
        lng = longitude
        view?.findViewById<TextView>(R.id.myLocationText)?.text = "Lat: $lat, Long: $lng"
    }

    private fun postTop3Online() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            lifecycleScope.launch(Dispatchers.IO) {
                val top3Restaurants = repository.getTop3Restaurants()
                val top3Map = top3Restaurants.mapIndexed { index, restaurant ->
                    "restaurant_${index + 1}" to restaurant
                }.toMap()

                db.collection("user-preference").document(userId).set(top3Map)
                    .addOnSuccessListener {
                        Log.d("SwipeFragment", "User preference successfully uploaded: $top3Map")
                    }
                    .addOnFailureListener { e ->
                        Log.e("SwipeFragment", "Error uploading preferences", e)
                    }
            }
        } else {
            Log.w("SwipeFragment", "No authenticated user found. Cannot upload preferences.")
        }
    }

    override fun onPause() {
        super.onPause()
        swipeViewModel.processPendingRemovals()
        postTop3Online()
    }

    override fun onStart() {
        super.onStart()
        postTop3Online()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("expandSearchButtonVisible", expandSearchButton.visibility == View.VISIBLE)
    }
}
