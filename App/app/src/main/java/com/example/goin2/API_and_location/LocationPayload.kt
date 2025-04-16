package com.example.goin2.API_and_location

data class LocationPayload(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long,
    val provider: String
)
