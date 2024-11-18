package com.example.munch_cmpt362.ui.swipe

import RestaurantAdapter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.munch_cmpt362.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import munch_cmpt362.database.MunchDatabase
import munch_cmpt362.database.restaurants.RestaurantDao
import munch_cmpt362.database.restaurants.RestaurantRepository

class SwipeFragment : Fragment() {
    private lateinit var cardStackView: CardStackView
    private lateinit var title: TextView
    private lateinit var noMoreRestaurantsText: TextView
    private lateinit var decisionIcon: ImageView
    private lateinit var cardStackLayoutManager: CardStackLayoutManager
    private lateinit var restaurantAdapter: RestaurantAdapter

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
        observeViewModel()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeViewModel.fetchRestaurants(isFakeData = false, lat, lng, databaseDao)
    }

    private fun initializeUI(view: View) {
        title = view.findViewById(R.id.title)
        cardStackView = view.findViewById(R.id.card_stack_view)
        noMoreRestaurantsText = view.findViewById(R.id.noMoreRestaurantsText)
        decisionIcon = view.findViewById(R.id.decision_icon)
        restaurantAdapter = RestaurantAdapter()
        cardStackView.adapter = restaurantAdapter
    }

    private fun initializeRepository() {
        database = MunchDatabase.getInstance(requireContext())
        databaseDao = database.restaurantDao
        repository = RestaurantRepository(databaseDao)
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
}
