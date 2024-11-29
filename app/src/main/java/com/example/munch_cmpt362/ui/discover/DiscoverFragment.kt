package com.example.munch_cmpt362.ui.discover

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.TypedValueCompat.dpToPx
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.munch_cmpt362.Business
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.ui.reviews.ReviewAdapter
import com.example.munch_cmpt362.ui.reviews.ReviewViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

// TODO:
//  1) Show all locations in list on map as well
//  2) If you click on element in list:
//      -> Also zooms in on element in map, colors it diff color, and doesnt show any other element in list
//      -> If you're in expanded mode when does this, kicks you back to shruken mode
//  3) If you click on element in map:
//      -> Also zooms in on element in map, colors it diff color, and doesnt show any other element in list
//  4) For both of those above, below the restaurnt in lst view will be a button to go back to default view of all restaurnts
//  6) Make search work
//  7) Also when in exapnded form, move shrink button to underneath list

@AndroidEntryPoint
class DiscoverFragment : Fragment(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager

    private var mapCentered = false
    private lateinit var markerOptions: MarkerOptions
    private lateinit var polylineOptions: PolylineOptions
    private lateinit var polylines: ArrayList<Polyline>

    private val discoverViewModel: DiscoverViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var reviewAdapter : ReviewAdapter
    private lateinit var expandTextView : TextView

    private var lat = 0.0
    private var lng = 0.0

    private var expanded = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_discover, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        reviewAdapter = ReviewAdapter(emptyList(), lat, lng)
        recyclerView.adapter = reviewAdapter

        expandTextView = view.findViewById(R.id.expandTextView)

        expandTextView.setOnClickListener {

            if (expanded == false) {
                val params = recyclerView.layoutParams as ConstraintLayout.LayoutParams
                params.topMargin = dpToPx(140) // Set the top margin to 100
                recyclerView.layoutParams = params
                recyclerView.requestLayout()
                expandTextView.text = "shrink"
                expanded = true
            }
            else {
                val params = recyclerView.layoutParams as ConstraintLayout.LayoutParams
                params.topMargin = dpToPx(475) // Set the top margin to 100
                recyclerView.layoutParams = params
                recyclerView.requestLayout()
                expandTextView.text = "expand"
                expanded = false
            }

        }

        return view
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize map
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        discoverViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
            if (restaurants.isNotEmpty()) {
                // TODO: This is probs where we put map markers
                //  or maybe we copy the restaurants.observe into onMapReady?
                val sortedRestaurants = /*sortRestaurants(*/restaurants//)
                reviewAdapter = ReviewAdapter(sortedRestaurants, lat, lng)
                recyclerView.adapter = reviewAdapter
            } else {
                recyclerView.visibility = View.GONE
//                emptyTextView.visibility = View.VISIBLE
                Log.d("XD:", "No restaurants available.")
            }
        }
        discoverViewModel.fetchRestaurants(lat, lng)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.setOnMapClickListener(this)
        mMap.setOnMapLongClickListener(this)
        polylineOptions = PolylineOptions().color(Color.BLACK)
        polylines = ArrayList()
        markerOptions = MarkerOptions()

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
            mMap.addMarker(markerOptions)
            polylineOptions.add(latLng)
            mapCentered = true
        }
    }

    override fun onMapClick(latLng: LatLng) {
//        for (polyline in polylines) polyline.remove()
//        polylineOptions.points.clear()
    }

    override fun onMapLongClick(latLng: LatLng) {
//        markerOptions.position(latLng)
//        mMap.addMarker(markerOptions)
//        polylineOptions.add(latLng)
//        polylines.add(mMap.addPolyline(polylineOptions))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::locationManager.isInitialized) {
            locationManager.removeUpdates(this)
        }
    }

    /////////////////

    fun updateLocation(latitude: Double, longitude: Double) {
        lat = latitude
        lng = longitude
    }

//    private fun sortRestaurants(restaurants: List<Business>): List<Business> {
////        Log.d("XD:", "Sorting restaurants")
////        return when (selectedSortTypeId) {
////            0 -> restaurants.sortedByDescending { it.rating } // Sort by name
////            1 -> restaurants.sortedByDescending { it.review_count } // Sort by descending rating
////            2 -> restaurants.sortedBy { it.name } // Sort by descending rating
////            3 -> restaurants.sortedBy { it.price?.length ?: Int.MAX_VALUE }
////            4 -> restaurants.sortedBy { it.categories[0].title }
////            5 -> restaurants.sortedByDescending { it.isOpenNow() }
////            6 -> sortRestaurantsByDistance(restaurants, lat, lng)
////            else -> restaurants
////        }
//    }

    private fun Business.isOpenNow(): Boolean {
        return this.business_hours.any { it.is_open_now }
    }

    private fun getLatLngFromAddress(context: Context, address: String): Pair<Double, Double>? {
        val geocoder = Geocoder(context, Locale.getDefault())

        // Attempt to get the list of addresses based on the address string
        val addressList: List<Address>? = geocoder.getFromLocationName(address, 1)

        if (!addressList.isNullOrEmpty()) {
            // If the list is not null or empty, get the first address
            val address = addressList[0]
            val latitude = address.latitude
            val longitude = address.longitude
            return Pair(latitude, longitude)
        } else {
            // Handle the case where the address could not be geocoded
            Log.e("Geocoding", "Address not found or geocoding failed.")
        }

        return null // Return null if geocoding fails or no results are found
    }

    fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        // Radius of the Earth in meters
        val R = 6371000.0

        // Convert latitude and longitude from degrees to radians
        val lat1Rad = Math.toRadians(lat1)
        val lng1Rad = Math.toRadians(lng1)
        val lat2Rad = Math.toRadians(lat2)
        val lng2Rad = Math.toRadians(lng2)

        // Difference in coordinates
        val dLat = lat2Rad - lat1Rad
        val dLng = lng2Rad - lng1Rad

        // Haversine formula
        val a = sin(dLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

//        Log.d("XD:", "XD: current: (${lng1}, ${lng1}) vs. (${lng2}, ${lng2}) : restaurant")
//        Log.d("XD:", "XD: distance: ${R*c}")

        // Distance in meters
        return R * c
    }

//    fun sortRestaurantsByDistance(
//        restaurants: List<Business>,
//        currentLat: Double,
//        currentLng: Double
//    ): List<Business> {
//        return restaurants.sortedBy { business ->
//            // Get LatLng for the business address
//            val latLng = getLatLngFromAddress(requireContext(), "${business.location.address1}, ${business.location.city}, ${business.location.country}")
//
//            // If LatLng is null, assign a large value to ensure it's sorted last
//            val distance = if (latLng != null) {
//                calculateDistance(currentLat, currentLng, latLng.first, latLng.second)
//            } else {
//                Double.MAX_VALUE  // Business without valid coordinates will be last
//            }
//
//            distance
//        }
//    }
//
//    private fun sortRestaurants() {
//        Log.d("XD:", "Sorting restaurants 2")
//        reviewViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
//            if (restaurants.isNotEmpty()) {
//                val sortedRestaurants = sortRestaurants(restaurants)
//                reviewAdapter = ReviewAdapter(sortedRestaurants, lat, lng)
//                recyclerView.adapter = reviewAdapter
//            } else {
////                noMoreRestaurantsText.visibility = View.VISIBLE
//                Log.d("XD:", "No restaurants available.")
//            }
//        }
//        reviewViewModel.fetchRestaurants(lat, lng)
//    }

}