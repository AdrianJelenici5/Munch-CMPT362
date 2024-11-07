package com.example.munch_cmpt362.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.ui.swipe.SwipeFragment
import com.example.munch_cmpt362.ui.main.adapter.MyFragmentStateAdapter
import com.example.munch_cmpt362.ui.profile.ProfileFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main), LocationListener {
    private lateinit var swipeFragment: SwipeFragment
    private lateinit var profileFragment: ProfileFragment
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var myMyFragmentStateAdapter: MyFragmentStateAdapter
    private lateinit var locationManager: LocationManager
    private val PERMISSION_REQUEST_CODE = 0
    private val tabTitles = arrayOf("Swipe", "Profile", "temp2")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(R.id.viewpager)
        viewPager.isUserInputEnabled = false
        tabLayout = view.findViewById(R.id.tab)

        profileFragment = ProfileFragment()
        swipeFragment = SwipeFragment()
        val fragments = arrayListOf<Fragment>(swipeFragment, profileFragment)

        myMyFragmentStateAdapter = MyFragmentStateAdapter(requireActivity(), fragments)
        viewPager.adapter = myMyFragmentStateAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        checkPermission()
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            initLocationManager()
            return
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        } else {
            initLocationManager()
        }
    }

    private fun initLocationManager() {
        try {
            locationManager = requireContext().getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return

            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) onLocationChanged(location)

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
        } catch (e: SecurityException) {
            // Handle exception if needed
        }
    }

    override fun onLocationChanged(location: Location) {
        swipeFragment.updateLocation(location.latitude, location.longitude)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            initLocationManager()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::locationManager.isInitialized) {
            locationManager.removeUpdates(this)
        }
    }
}