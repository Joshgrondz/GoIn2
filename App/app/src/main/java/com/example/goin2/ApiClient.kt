package com.example.goin2

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object ApiClient {
    private val client = OkHttpClient()
    private const val BASE_URL = "https://webapplication120250408230542-draxa5ckg5gabacc.canadacentral-01.azurewebsites.net"
    private val mainHandler = Handler(Looper.getMainLooper())

    fun sendLocation(location: LocationPayload, studentId: Int = 7) {
        Thread {
            try {
                val json = JSONObject().apply {
                    put("id", 0) // DB assigns
                    put("userId", studentId)
                    put("latitude", location.latitude)
                    put("longitude", location.longitude)
                    put("accuracy", location.accuracy)
                    put("timestampMs", location.timestamp)
                    put("provider", location.provider)
                }

                val requestBody = json.toString()
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("$BASE_URL/api/SampleLocations")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.e("ApiClient", "Failed to send location: ${response.code}")
                } else {
                    Log.d("ApiClient", "Location sent successfully")
                }

            } catch (e: Exception) {
                Log.e("ApiClient", "sendLocation exception: ${e.message}")
            }
        }.start()
    }



    fun getStudents(callback: (String) -> Unit) {
        Thread {
            val result = try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/Students")
                    .addHeader("accept", "text/plain")
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    "HTTP error: ${response.code}"
                } else {
                    val bodyString = response.body?.string()
                    if (bodyString == null) {
                        mainHandler.post { callback("Response body is null") }
                        return@Thread
                    }

                    val jsonArray = JSONArray(bodyString)
                    val resultBuilder = StringBuilder()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        resultBuilder.append("${obj.getString("name")}\n")
                    }

                    resultBuilder.toString().trim()
                }
            } catch (e: Exception) {
                "Exception: ${e::class.simpleName} - ${e.message ?: "No message"}"
            }

            mainHandler.post {
                callback(result)
            }
        }.start()
    }

    fun getLastKnownLocation(studentId: Int = 7, callback: (Double, Double) -> Unit) {
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/SampleLocations/Student/$studentId/Latest")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (body.isNullOrBlank()) {
                    Log.e("ApiClient", "Empty response body")
                    return@Thread
                }

                val json = JSONObject(body)

                if (!json.has("latitude") || !json.has("longitude")) {
                    Log.e("ApiClient", "Missing latitude or longitude in response")
                    return@Thread
                }

                val lat = json.getDouble("latitude")
                val lng = json.getDouble("longitude")

                Handler(Looper.getMainLooper()).post {
                    callback(lat, lng)
                }
            } catch (e: Exception) {
                Log.e("ApiClient", "Error parsing location response: ${e.message}")
            }
        }.start()
    }


}
