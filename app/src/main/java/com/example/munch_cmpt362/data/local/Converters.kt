package com.example.munch_cmpt362.data.local

import androidx.room.TypeConverter
import com.example.munch_cmpt362.BusinessHours
import com.example.munch_cmpt362.Category
import com.example.munch_cmpt362.Location
import com.example.munch_cmpt362.OpenHours
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromLocation(location: Location?): String? {
        return gson.toJson(location)
    }

    @TypeConverter
    fun toLocation(data: String?): Location? {
        return data?.let {
            gson.fromJson(it, Location::class.java)
        }
    }

    @TypeConverter
    fun fromCategory(category: Category?): String? {
        return gson.toJson(category)
    }

    @TypeConverter
    fun toCategory(data: String?): Category? {
        return data?.let {
            gson.fromJson(it, Category::class.java)
        }
    }

    @TypeConverter
    fun fromBusinessHours(businessHours: List<BusinessHours>?): String? {
        return gson.toJson(businessHours)
    }

    @TypeConverter
    fun toBusinessHours(data: String?): List<BusinessHours>? {
        return data?.let {
            val listType = object : TypeToken<List<BusinessHours>>() {}.type
            gson.fromJson(it, listType)
        }
    }

    @TypeConverter
    fun fromOpenHours(openHours: List<OpenHours>?): String? {
        return gson.toJson(openHours)
    }

    @TypeConverter
    fun toOpenHours(data: String?): List<OpenHours>? {
        return data?.let {
            val listType = object : TypeToken<List<OpenHours>>() {}.type
            gson.fromJson(it, listType)
        }
    }

    @TypeConverter
    fun fromString(value: String): Map<String, Any> {
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(value, mapType)
    }

    @TypeConverter
    fun fromMap(map: Map<String, Any>): String {
        return gson.toJson(map)
    }

}
