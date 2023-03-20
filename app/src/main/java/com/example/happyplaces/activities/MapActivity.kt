package com.example.happyplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happyplaces.R
import com.example.happyplaces.databinding.ActivityMapBinding
import com.example.happyplaces.models.HappyPlaceModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

// TODO Displaying a marker on the map an zooming with animation(Step 3: Extend a OnMapReadyCallback interface.)
class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    // TODO Displaying a marker on the map an zooming with animation(Step 1: Create a variable for data model class.)
    // START
    private var mHappyPlaceDetails: HappyPlaceModel? = null
    // END
    private var binding: ActivityMapBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // TODO Displaying a marker on the map an zooming with animation (Step 2: Receives the details through intent and used further.)
        // START
        // Check if the intent has extra information
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mHappyPlaceDetails =
                intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }
        if (mHappyPlaceDetails != null) {

            setSupportActionBar(binding?.toolbarMap)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            // Title of the action bar is the title of the happyPLace passed
            supportActionBar!!.title = mHappyPlaceDetails!!.title

            binding?.toolbarMap?.setNavigationOnClickListener {
                onBackPressed()
            }
            // Get the fragment used in the activity_map.xml
            val supportMapFragment: SupportMapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            // This means that it should run in the background
            // Make this class extend OnMapReady interface to remove any errors
            supportMapFragment.getMapAsync(this)
        }
        // END


    }

    // TODO Displaying a marker on the map an zooming with animation (Step 4: After extending an interface adding the location pin to the map when the map is ready using the latitude and longitude.)
    override fun onMapReady(googleMap: GoogleMap?) {
        // Get location where we will insert the marker
        val position = LatLng(
            mHappyPlaceDetails!!.latitude,
            mHappyPlaceDetails!!.longitude
        )
        /**
         * Add a marker on the location using the latitude and longitude and move the camera to it.
         */
        googleMap?.addMarker(MarkerOptions().position(position).title(mHappyPlaceDetails!!.location))
        // Zooming in animation
        // Has multiple zoom values
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 15f)
        googleMap?.animateCamera(newLatLngZoom)
    }
}