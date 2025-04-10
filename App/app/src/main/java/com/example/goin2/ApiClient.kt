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


    //This implementation just gets all students then searches for a name that matches the input.
    //Doing this API side might be better eventually, but it works.
    fun getStudentIdByName(name: String, callback: (Int?) -> Unit) {
        Log.d("ApiClient", "getStudentIdByName: looking for name = $name")
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/Students")
                    .addHeader("accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                Log.d("ApiClient", "getStudentIdByName: response = $body")

                if (body.isNullOrEmpty()) {
                    Log.w("ApiClient", "getStudentIdByName: Empty body")
                    mainHandler.post { callback(null) }
                    return@Thread
                }

                val jsonArray = JSONArray(body)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    Log.d("ApiClient", "Checking student: ${obj.getString("name")} (id=${obj.getInt("id")})")
                    if (obj.getString("name").equals(name, ignoreCase = true)) {
                        val foundId = obj.getInt("id")
                        Log.d("ApiClient", "Match found: id = $foundId")
                        mainHandler.post { callback(foundId) }
                        return@Thread
                    }
                }

                Log.d("ApiClient", "No matching student name found")
                mainHandler.post { callback(null) }

            } catch (e: Exception) {
                Log.e("ApiClient", "getStudentIdByName exception: ${e.message}", e)
                mainHandler.post { callback(null) }
            }
        }.start()
    }

    fun sendLocation(location: LocationPayload, studentId: Int = 7) {
        Log.d("ApiClient", "sendLocation: Sending location for studentId = $studentId")
        Log.d("ApiClient", "Payload = $location")

        Thread {
            try {
                val json = JSONObject().apply {
                    put("id", 0)
                    put("userId", studentId)
                    put("latitude", location.latitude)
                    put("longitude", location.longitude)
                    put("accuracy", location.accuracy)
                    put("timestampMs", location.timestamp)
                    put("provider", location.provider)
                }

                Log.d("ApiClient", "sendLocation JSON = $json")

                val requestBody = json.toString()
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("$BASE_URL/api/SampleLocations")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful) {
                    Log.e("ApiClient", "Failed to send location: HTTP ${response.code}")
                    Log.e("ApiClient", "Response body: $responseBody")
                } else {
                    Log.d("ApiClient", "Location sent successfully: $responseBody")
                }

            } catch (e: Exception) {
                Log.e("ApiClient", "sendLocation exception: ${e.message}", e)
            }
        }.start()
    }

    fun getStudents(callback: (String) -> Unit) {
        Log.d("ApiClient", "getStudents: Fetching all student names")
        Thread {
            val result = try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/Students")
                    .addHeader("accept", "text/plain")
                    .build()

                val response = client.newCall(request).execute()
                val bodyString = response.body?.string()

                Log.d("ApiClient", "getStudents response = $bodyString")

                if (!response.isSuccessful) {
                    "HTTP error: ${response.code}"
                } else if (bodyString == null) {
                    mainHandler.post { callback("Response body is null") }
                    return@Thread
                } else {
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
        Log.d("ApiClient", "getLastKnownLocation: Fetching for studentId = $studentId")

        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/SampleLocations/Student/$studentId/Latest")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                Log.d("ApiClient", "getLastKnownLocation response = $body")

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

                Log.d("ApiClient", "Parsed location: lat=$lat, lng=$lng")

                Handler(Looper.getMainLooper()).post {
                    callback(lat, lng)
                }
            } catch (e: Exception) {
                Log.e("ApiClient", "Error parsing location response: ${e.message}", e)
            }
        }.start()
    }
}
