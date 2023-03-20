package com.example.happyplaces.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import android.util.Log
import java.io.IOException
import java.util.*

// TODO Get The Address From the Latitude and Longitude (Step 1: Create a AsyncTask class fot getting an address from the latitude and longitude from the location provider.)
// AsyncTask used so that this class can run in the background and prevent the main thread from locking
class GetAddressFromLatLng(context: Context,private val latitude: Double, private val longitude: Double) :
    AsyncTask<Void, String, String>() {
    // Creates a readable location when given a longitude and latitude
    // TODO Get The Address From the Latitude and Longitude (Step 2: Initialize a variable of type GeoCoder that will be used to turn the longitude and latitude given into human readable location.)
    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())

    /**
     * A variable of address listener interface.
     */
    private lateinit var mAddressListener: AddressListener

    // TODO Get The Address From the Latitude and Longitude (Step 3: In the background use the Geocoder to return a String location.)
    override fun doInBackground(vararg params: Void?): String {
        try {
            /**
             * Returns an array of Addresses that are known to describe the
             * area immediately surrounding the given latitude and longitude.
             */
            // TODO Get The Address From the Latitude and Longitude (Step 4: The Geocoder returns a list of type Address. An Address is a datatype which contains a set of strings describing the address. This set has elements such as Locale,PostalCode,CountryCode)
            // Take the first result...maxResults == 1
            val addressList: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            // If addressList is not null or empty take an address and break it up into sections
            // TODO Get The Address From the Latitude and Longitude (Step 5: Using a StringBuilder, convert the Address given into a neat and ledgible string.)
            if (addressList != null && addressList.isNotEmpty()) {
                val address: Address = addressList[0]
                val sb = StringBuilder()
                // Go through all the addresses available
                for (i in 0..address.maxAddressLineIndex) {
                    // Separate the address values with a comma
                    sb.append(address.getAddressLine(i)).append(",")
                }
                sb.deleteCharAt(sb.length - 1) // Here we remove the last comma that we have added above from the address.
                return sb.toString()
            }

        }catch(e: IOException) {
            Log.e("HappyPlaces", "Unable connect to Geocoder")
        }
        return " "
    }


    // TODO Get The Address From the Latitude and Longitude (Step 6: onPostExecute method of AsyncTask where the result will be received and assigned to the interface accordingly.)
    // The class defined interface is then used in the post execute functionality
    override fun onPostExecute(result: String?) {
        if (result == null) {
            mAddressListener.onError()
        } else {
            mAddressListener.onAddressFound(result)
        }
        super.onPostExecute(result)
    }

    // TODO Get The Address From the Latitude and Longitude (Step 7: A public function to set the AddressListener.)
    // The instance defined interface methods are connected to the class defined interface here
    fun setAddressListener(addressListener: AddressListener) {
        mAddressListener = addressListener
    }

    // The interface enables polymorphism in the sense that the implementations of its methods can be defined in the instance this class is used
    // Provides access to the AddressListener type
    interface AddressListener {
        // What do we want to do when the address is found
        // Implementation of these methods instance dependent...this means that their definitions are determined when this class is called
        fun onAddressFound(address: String?)
        fun onError()
    }

    // An AsyncTask doesn't do anything until is it executed
    // TODO Get The Address From the Latitude and Longitude (Step 8: A public function to execute the AsyncTask from the class is called.)
    fun getAddress() {
        execute()
    }



}