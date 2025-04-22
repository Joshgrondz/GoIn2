package com.example.goin2.API_and_location

data class LocationPayload(
    val id: Int = 0,
    val userid: Int,
    val latitude: Double,
    val longitude: Double,
    val locAccuracy: Float,
    val locAltitude: Double,
    val locSpeed: Float,
    val locBearing: Float,
    val locProvider: String,
    val timestampMs: Long,
    val user: String  // "student" or "teacher"
)
