package com.example.munch_cmpt362.ui.discover

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.munch_cmpt362.Business
import com.example.munch_cmpt362.R
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
//  1) Change 'Selected Restaurant' to '<- Back to Full List'
//  2) Make expand and shrink always keep the same restaurant
//  3) Make expanded view look better
//  4) Make onQueryChange show restaurants similar
//      -> if it restaurant name contains current searchQuery with at most one differences
//      -> i.e. 'sub' shows 'subway' AND 'sab' also shows 'subway'
//  5) Add a view model for horizantal changes
//  6) Also sort all restaurnants in this fragment by distance closes to you
//      -> means i have to optimize this sort method
//  7) Determine why cant scroll to bottom of recyler view


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

    val markersMap = mutableMapOf<String, Marker>()
    val onItemClicked: (Business) -> Unit = { restaurant ->
        val marker = markersMap[restaurant.name]
        if (marker != null) {
            onMarkerClick(marker)
        }
        searchView.clearFocus()
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

        }

        ///////////////////////////
        // EXPAND FUNCTIONALITY //
        //////////////////////////

        expandTextView.setOnClickListener {

            if (expanded == false) {
                expandList()
            }
            else {
                val params = recyclerView.layoutParams as ConstraintLayout.LayoutParams
                params.topMargin = dpToPx(475) // Set the top margin to 100
                recyclerView.layoutParams = params
                recyclerView.requestLayout()
                expandTextView.text = "expand list"
                searchView.clearFocus()
                expanded = false
                discoverViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
                    updateRecyclerViewWithAllRestaurants(restaurants)
                }
                labelTextView.text = "    All restaurants in your area:"
                searchView.setQuery("", false)
            }

        }

        ////////////////////////////////
        // SEARCH VIEW FUNCTIONALITY //
        ///////////////////////////////

        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                expandList()
            }
        }

        searchView.setOnClickListener {
            val searchQuery = searchView.query.toString()
            updateList(searchQuery)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchQuery ->
                    updateList(searchQuery)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {searchQuery ->
                    updateList(searchQuery)
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
        println("debug: onLocationChanged() ${location.latitude} ${location.longitude}")
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
            updateRecyclerViewWithAllRestaurants(restaurants)
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
            labelTextView.text = "    Selected restuarant:"
        } else {
            discoverViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
                updateRecyclerViewWithAllRestaurants(restaurants)
            }
            labelTextView.text = "    All restaurants in your area:"
        }

        val latLng = marker.position
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12f)  // Zoom level of 15
        mMap.animateCamera(cameraUpdate)
        marker.showInfoWindow()

        val params = recyclerView.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = dpToPx(475)
        recyclerView.layoutParams = params
        recyclerView.requestLayout()
        expandTextView.text = "expand list"
        expanded = false

        searchView.clearFocus()

        return true
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        lat = latitude
        lng = longitude
    }

    //////////////////////////////
    // RECYCLER VIEW FUNCTIONS //
    /////////////////////////////

    fun updateRecyclerViewWithAllRestaurants(restaurants: List<Business>) {
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

    private fun updateList(searchQuery: String) {
        labelTextView.text = "    Search Results for '$searchQuery':"
        discoverViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
            if (searchQuery.isEmpty()) {
                updateRecyclerViewWithAllRestaurants(restaurants)
                labelTextView.text = "    All restaurants in your area:"
            } else {
                val matchingRestaurant = restaurants.find { it.name.equals(searchQuery, ignoreCase = true) }
                if (matchingRestaurant != null) {
                    updateRecyclerViewWithRestaurant(matchingRestaurant)
                } else {
                    updateRecyclerViewWithAllRestaurants(emptyList())
                }
            }
        }
    }

    /////////////////////////////
    // OTHER HELPER FUNCTIONS //
    ////////////////////////////

    private fun expandList() {
        discoverViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
            updateRecyclerViewWithAllRestaurants(restaurants)
        }
        labelTextView.text = "    All restaurants in your area:"
        for (marker in markersMap.values) {
            marker.hideInfoWindow()
        }
        mapCentered = false
        initLocationManager()
        val params = recyclerView.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = dpToPx(135) // Set the top margin to 100
        recyclerView.layoutParams = params
        recyclerView.requestLayout()
        expandTextView.text = "exit"
        expanded = true
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


}