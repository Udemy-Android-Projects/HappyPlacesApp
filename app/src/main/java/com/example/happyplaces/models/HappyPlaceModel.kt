package com.example.happyplaces.models

// This is how a happyPlace will look
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
)
