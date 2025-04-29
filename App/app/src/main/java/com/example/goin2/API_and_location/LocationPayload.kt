package com.example.goin2.API_and_location

data class LocationPayload(
    val userid: Int,
    val latitude: Double,
    val longitude: Double,
    val locAccuracy: Float,
    val locAltitude: Double,
    val locSpeed: Float,
    val locBearing: Float,
    val locProvider: String,
    val timestampMs: Long
)

data class NotificationResult(
    val id: Int,
    val userid: Int,
    val eventid: Int,
    val notificationDescription: String,
    val notificationTimestamp: String
)
