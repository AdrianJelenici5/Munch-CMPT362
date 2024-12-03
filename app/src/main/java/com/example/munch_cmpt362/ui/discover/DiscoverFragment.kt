package com.example.munch_cmpt362.ui.discover

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.munch_cmpt362.Business
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.data.local.database.MunchDatabase
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

// TODO:
//  6) Also sort all restaurnants in this fragment by distance closes to you
//      -> means i have to optimize this sort method

@AndroidEntryPoint
class DiscoverFragment : Fragment(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager

    private var mapCentered = false
    private lateinit var markerOptions: MarkerOptions
    private lateinit var polylineOptions: PolylineOptions
    private lateinit var polylines: ArrayList<Polyline>

    private val discoverViewModel: DiscoverViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var discoverAdapter : DiscoverAdapter
    private lateinit var expandTextView : TextView
    private lateinit var labelTextView : TextView
    private lateinit var searchView : SearchView

    private var lat = 0.0
    private var lng = 0.0

    private var expanded = false
    private var isFocused = false

    val markersMap = mutableMapOf<String, Marker>()
    val onItemClicked: (Business) -> Unit = { restaurant ->
        val marker = markersMap[restaurant.name]
        if (marker != null) {
            onMarkerClick(marker)
        }
        searchView.clearFocus()
        isFocused = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_discover, container, false)

        /////////////////////////
        // DEFINING VARIABLES //
        ////////////////////////

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        discoverAdapter = DiscoverAdapter(emptyList(), lat, lng, onItemClicked)
        recyclerView.adapter = discoverAdapter

        expandTextView = view.findViewById(R.id.expandTextView)
        labelTextView = view.findViewById(R.id.labelTextView)
        searchView = view.findViewById(R.id.search_view)

        //////////////////////////////
        // LIST VIEW FUNCTIONALITY //
        /////////////////////////////

        labelTextView.setOnClickListener {
            if (labelTextView.text.equals("    <- Back to full list") ) {
                discoverViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
                    updateRecyclerViewWithRestaurantList(restaurants)
                }
                labelTextView.text = "    All restaurants in your area:"
                searchView.setQuery("", false)
                resetMap()
            }
        }

        ///////////////////////////
        // EXPAND FUNCTIONALITY //
        //////////////////////////

        expandTextView.setOnClickListener {

            if (expanded == false) {
                expandList()
            }
            else {
                shrinkList()
                if (isFocused) {
                    discoverViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
                        updateRecyclerViewWithRestaurantList(restaurants)
                    }
                    labelTextView.text = "    All restaurants in your area:"
                    searchView.setQuery("", false)
                    resetMap()
                }
                isFocused = false
            }

        }

        ////////////////////////////////
        // SEARCH VIEW FUNCTIONALITY //
        ///////////////////////////////

        val closeButton = searchView.findViewById<View>(androidx.appcompat.R.id.search_close_btn)

        closeButton?.setOnClickListener {
            searchView.setQuery("", false)
            if (expanded == false) {
                shrinkList()
            }
        }

        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val searchQuery = searchView.query.toString()
                if (searchQuery != null) {
                    updateListOnQueryChange(searchQuery)
                }
                else {
                    Log.d("searchQuery:", "searchQuery: $searchQuery")
                    discoverViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
                        updateRecyclerViewWithRestaurantList(restaurants)
                    }
                    labelTextView.text = "    All restaurants in your area:"
                    for (marker in markersMap.values) {
                        marker.hideInfoWindow()
                    }
                }
                mapCentered = false
                initLocationManager()
                expandList()
                isFocused = true
            }
        }

        searchView.setOnClickListener {
            val searchQuery = searchView.query.toString()
            updateListOnQueryChange(searchQuery)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchQuery ->
                    updateListOnQueryChange(searchQuery)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {searchQuery ->
                    updateListOnQueryChange(searchQuery)
                }
                return true
            }

        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        discoverViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
            if (restaurants.isNotEmpty()) {
                val sortedRestaurants = /*sortRestaurants(*/restaurants//)
                discoverAdapter = DiscoverAdapter(sortedRestaurants, lat, lng, onItemClicked)
                recyclerView.adapter = discoverAdapter
            } else {
                recyclerView.visibility = View.GONE
                Log.d("XD:", "No restaurants available.")
            }
        }
        discoverViewModel.fetchRestaurants(lat, lng)
    }

    ////////////////////
    // MAP FUNCTIONS //
    ///////////////////

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.setOnMapClickListener(this)
        polylineOptions = PolylineOptions().color(Color.BLACK)
        polylines = ArrayList()
        markerOptions = MarkerOptions()

        discoverViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
            restaurants?.forEach { restaurant ->

                val latLngPair = getLatLngFromAddress(requireContext(),
                    "${restaurant.location.address1}, ${restaurant.location.city}, ${restaurant.location.country}")
                val latLng = latLngPair?.let { LatLng(it.first, latLngPair.second) }
                val markerOptions = latLng?.let {
                    MarkerOptions()
                        .position(it)
                        .title(restaurant.name)
                }
                if (markerOptions != null) {
                    mMap.addMarker(markerOptions)
                }
                markerOptions?.let { marker ->
                    val mapMarker = mMap.addMarker(marker)
                    if (mapMarker != null) {
                        markersMap[restaurant.name] = mapMarker
                        Log.d("AJ:", "Marker added for ${restaurant.name}")
                    }
                }
            }

        }

        mMap.setOnMarkerClickListener { marker ->
            onMarkerClick(marker)
        }

        initLocationManager()
    }

    private fun initLocationManager() {
        try {
            locationManager =
                requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return

            val location =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) onLocationChanged(location)

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0f, this
            )

        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onLocationChanged(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        if (!mapCentered) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12f)
            mMap.animateCamera(cameraUpdate)
            markerOptions.position(latLng)
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            mMap.addMarker(markerOptions)
            polylineOptions.add(latLng)
            mapCentered = true
        }
    }

    override fun onMapClick(latLng: LatLng) {
        discoverViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
            updateRecyclerViewWithRestaurantList(restaurants)
        }
        searchView.clearFocus()
        labelTextView.text = "    All restaurants in your area:"
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        if (marker.title != null) {
            discoverViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
                val matchingRestaurant = restaurants.find { it.name == marker.title }
                matchingRestaurant?.let {
                    updateRecyclerViewWithRestaurant(it)
                }
            }
            labelTextView.text = "    <- Back to full list"
        } else {
            discoverViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
                updateRecyclerViewWithRestaurantList(restaurants)
            }
            labelTextView.text = "    All restaurants in your area:"
        }

        val latLng = marker.position
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12f)
        mMap.animateCamera(cameraUpdate)
        marker.showInfoWindow()

        shrinkList()
        // searchView.setQuery("", false)

        return true
    }

    private fun resetMap() {
        for (marker in markersMap.values) {
            marker.hideInfoWindow()
        }
        mapCentered = false
        initLocationManager()
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        lat = latitude
        lng = longitude
    }

    //////////////////////////////
    // RECYCLER VIEW FUNCTIONS //
    /////////////////////////////

    fun updateRecyclerViewWithRestaurantList(restaurants: List<Business>) {
        discoverAdapter = DiscoverAdapter(restaurants, lat, lng, onItemClicked)
        recyclerView.adapter = discoverAdapter
    }

    fun updateRecyclerViewWithRestaurant(restaurant: Business) {
        val filteredList = listOf(restaurant)
        discoverAdapter = DiscoverAdapter(filteredList, lat, lng, onItemClicked)
        recyclerView.adapter = discoverAdapter
        val position = filteredList.indexOf(restaurant)
        recyclerView.scrollToPosition(position)
    }

    private fun updateListOnQueryChange(searchQuery: String) {
        labelTextView.text = "    Search Results like '$searchQuery':"
        Log.d("searchQuery:", "searchQuery: $searchQuery")
        discoverViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
            if (searchQuery.isEmpty()) {
                Log.d("searchQuery:", "searchQuery is null: $searchQuery")
                labelTextView.text = "    All restaurants in your area:"
                updateRecyclerViewWithRestaurantList(restaurants)
                //labelTextView.text = "    All restaurants in your area:"
            } else {
                val matchingRestaurants = findSimilarRestaurants(restaurants, searchQuery)
                if (matchingRestaurants != null) {
                    updateRecyclerViewWithRestaurantList(matchingRestaurants)
                } else {
                    updateRecyclerViewWithRestaurantList(emptyList())
                }
            }
        }
    }

    fun findSimilarRestaurants(restaurants: List<Business>, searchQuery: String): List<Business> {
        val similarRestaurants = mutableListOf<Business>()
        for (restaurant in restaurants) {
            val restaurantName = restaurant.name
            if (restaurantName.contains(searchQuery, ignoreCase = true)) {
                similarRestaurants.add(restaurant)
            }
        }
        return similarRestaurants
    }

    /////////////////////////////
    // OTHER HELPER FUNCTIONS //
    ////////////////////////////

    private fun expandList() {
        val params = recyclerView.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = dpToPx(132) // Set the top margin to 100
        recyclerView.layoutParams = params
        recyclerView.requestLayout()
        expandTextView.text = "exit"
        expanded = true

        val params2 = searchView.layoutParams as ConstraintLayout.LayoutParams
//        params2.marginStart = dpToPx(0)
//        params2.marginEnd = dpToPx(0)
        searchView.layoutParams = params2
        searchView.requestLayout()

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.view?.visibility = View.GONE
        //mapFragment?.getView()?.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
    }

    private fun shrinkList() {
        val params = recyclerView.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = dpToPx(475)
        recyclerView.layoutParams = params
        recyclerView.requestLayout()
        expandTextView.text = "expand"
        expanded = false
        searchView.clearFocus()

        val params2 = searchView.layoutParams as ConstraintLayout.LayoutParams
        params2.marginStart = dpToPx(20)
        params2.marginEnd = dpToPx(20)
        searchView.layoutParams = params2
        searchView.requestLayout()

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.view?.visibility = View.VISIBLE
    }

    private fun getLatLngFromAddress(context: Context, address: String): Pair<Double, Double>? {
        val geocoder = Geocoder(context, Locale.getDefault())

        val addressList: List<Address>? = geocoder.getFromLocationName(address, 1)

        if (!addressList.isNullOrEmpty()) {
            val address = addressList[0]
            val latitude = address.latitude
            val longitude = address.longitude
            return Pair(latitude, longitude)
        }

        return null // Return null if geocoding fails or no results are found
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    //////////////////////

    override fun onDestroyView() {
        super.onDestroyView()
        if (::locationManager.isInitialized) {
            locationManager.removeUpdates(this)
        }
    }

    override fun onResume() {
        super.onResume()
        // Lock orientation to portrait (example)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onPause() {
        super.onPause()
        // Restore to the system default (sensor-based orientation)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }


}