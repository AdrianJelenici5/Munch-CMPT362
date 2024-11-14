package com.example.munch_cmpt362.ui.swipe

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.munch_cmpt362.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction
import com.yuyakaido.android.cardstackview.StackFrom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import munch_cmpt362.database.MunchDatabase
import munch_cmpt362.database.restaurants.RestaurantDao
import munch_cmpt362.database.restaurants.RestaurantRepository


class SwipeFragment : Fragment() {
    private lateinit var cardStackView: CardStackView
    private lateinit var title : TextView
    private lateinit var noMoreRestaurantsText: TextView
    private lateinit var cardStackLayoutManager: CardStackLayoutManager
    private lateinit var restaurantAdapter: RestaurantAdapter

    private lateinit var database: MunchDatabase
    private lateinit var databaseDao: RestaurantDao
    private lateinit var repository: RestaurantRepository


    private val swipeViewModel: SwipeViewModel by viewModels()

    private var lat = 0.0
    private var lng = 0.0

    val db = Firebase.firestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_swipe, container, false)
        title = view.findViewById(R.id.title)
        cardStackView = view.findViewById(R.id.card_stack_view)
        noMoreRestaurantsText = view.findViewById(R.id.noMoreRestaurantsText)

        restaurantAdapter = RestaurantAdapter(emptyList())
        cardStackView.adapter = restaurantAdapter

        database = MunchDatabase.getInstance(requireContext())
        databaseDao = database.restaurantDao
        repository = RestaurantRepository(databaseDao)

        setupCardStackView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeViewModel.restaurants.observe(requireActivity()) { restaurants ->
            Log.d("JP:", "from view model ${restaurants}")
            if (restaurants.isNotEmpty()) {
                restaurantAdapter = RestaurantAdapter(restaurants)
                cardStackView.adapter = restaurantAdapter
            } else {
                noMoreRestaurantsText.visibility = View.VISIBLE
            }
        }
        swipeViewModel.fetchRestaurants(isFakeData = false, lat, lng, databaseDao)
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        lat = latitude
        lng = longitude
        view?.findViewById<TextView>(R.id.myLocationText)?.text = "Lat: $lat, Long: $lng"
    }

        private fun setupCardStackView() {
        cardStackLayoutManager = CardStackLayoutManager(requireContext(), object : CardStackListener {
            override fun onCardDragging(direction: Direction?, ratio: Float) {
                when (direction) {
                    Direction.Right -> {

                    }

                    Direction.Left -> {

                    }

                    else -> {}
                }
            }

            override fun onCardSwiped(direction: Direction?) {
                if (cardStackLayoutManager.topPosition == restaurantAdapter.itemCount) {
                    noMoreRestaurantsText.visibility = View.VISIBLE
                }

                val position = cardStackLayoutManager.topPosition - 1 // Get the position of the swiped card
                val swipedRestaurant = restaurantAdapter.getItem(position) // Get the restaurant that was swiped

                when (direction) {
                    Direction.Right -> { //Liked = + 10 score
                        CoroutineScope(Dispatchers.IO).launch {
                            val score = repository.getScoreById(swipedRestaurant.id)
                            repository.updateScore(swipedRestaurant.id, score + 20)
                        }
//                        swipeViewModel.removeRestaurant(swipedRestaurant.id) //remove from view model so it doesn't show up when loading fragment again
                        //dk what's wrong, list just won't update
                    }

                    Direction.Left -> { //Disliked = - 10 score
                        CoroutineScope(Dispatchers.IO).launch {
                            val score = repository.getScoreById(swipedRestaurant.id)
                            repository.updateScore(swipedRestaurant.id, score - 20)
                        }
//                        swipeViewModel.removeRestaurant(swipedRestaurant.id) //remove from view model so it doesn't show up when loading fragment again
                    }

                    else -> {}
                }
            }

            override fun onCardRewound() {

            }

            override fun onCardCanceled() {

            }

            override fun onCardAppeared(view: View?, position: Int) {
            }

            override fun onCardDisappeared(view: View?, position: Int) {
            }
        })

        cardStackLayoutManager.setStackFrom(StackFrom.None)
        cardStackLayoutManager.setVisibleCount(3)
        cardStackLayoutManager.setTranslationInterval(8.0f)
        cardStackLayoutManager.setScaleInterval(0.95f)
        cardStackLayoutManager.setSwipeThreshold(0.3f)
        cardStackLayoutManager.setMaxDegree(20.0f)
        cardStackLayoutManager.setDirections(Direction.HORIZONTAL)
        cardStackLayoutManager.setCanScrollHorizontal(true)
        cardStackLayoutManager.setCanScrollVertical(false)

        cardStackView.layoutManager = cardStackLayoutManager
    }

    fun postTop3Online() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid

            CoroutineScope(Dispatchers.IO).launch {
                val top3Rest = repository.getTop3Restaurants()
                val top3RestMap = top3Rest.mapIndexed { index, restaurant ->
                    "restaurant_${index + 1}" to restaurant
                }.toMap()

                db.collection("user-preference").document(userId).set(top3RestMap)
                    .addOnSuccessListener {
                        Log.d("JP:", "User preference $top3RestMap successfully uploaded to Firestore")
                    }
                    .addOnFailureListener { e ->
                        Log.w("JP:", "Error adding document", e)
                    }
            }
        } else {
            Log.w("JP:", "No authenticated user found. Cannot upload preferences.")
        }
    }

    override fun onPause() {
        super.onPause()
        postTop3Online()
    }

    override fun onStart() {
        super.onStart()
        postTop3Online()
    }

}
