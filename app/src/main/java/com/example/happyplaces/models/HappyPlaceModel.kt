package com.example.happyplaces.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

// This is how a happyPlace will look
// The class is made serializable so that it can be passed between objects
// We can use parcelable since it will make tha application significantly faster
data class HappyPlaceModel(
    val id: Int,
    val title: String,
    // Image is of type string since we are using the image path
    val image: String,
    val description: String,
    val date: String,
    val location: String,
    val longitude: Double,
    val latitude: Double
) : Serializable

