package com.example.munch_cmpt362

data class YelpResponse(
    val businesses: List<Business>
)

data class Business(
    val name: String,
    val rating: Float,
    val review_count: Int,
    val price: String?,
    val location: Location,
    val phone: String,
    val categories: List<Category>,
    val url: String,
    val image_url: String,
)

data class Location(
    val address1: String,
    val city: String,
    val zip_code: String,
    val country: String
)

data class Category(
    val alias: String,
    val title: String
)