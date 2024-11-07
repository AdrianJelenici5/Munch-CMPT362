package com.example.munch_cmpt362

data class YelpResponse(
    val businesses: List<Business>
)

data class Business(
    val id: String,
    val name: String,
    val rating: Float,
    val review_count: Int,
    val price: String?,
    val location: Location,
    val phone: String,
    val categories: List<Category>,
    val url: String,
    val image_url: String,
    val business_hours: List<BusinessHours>
)

data class BusinessHours(
    val hour_type: String,
    val open: List<OpenHours>,
    val is_open_now: Boolean
)

data class OpenHours(
    val day: Int,
    val start: String,
    val end: String,
    val is_overnight: Boolean
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