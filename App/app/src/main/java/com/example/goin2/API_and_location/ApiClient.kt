package com.example.goin2.API_and_location

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
    private val mainHandler = Handler(Looper.getMainLooper())
    private val client = OkHttpClient()
    private const val BASE_URL = "https://webapplication120250408230542-draxa5ckg5gabacc.canadacentral-01.azurewebsites.net"

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

    fun pingServer(callback: (Boolean) -> Unit) {
        Log.d("ApiClient", "Pinging server via /api/User...")
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/User")
                    .get()
                    .addHeader("accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()
                Log.d("ApiClient", "Ping response: $body")

                val success = if (!body.isNullOrEmpty()) {
                    val jsonArray = JSONArray(body)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        if (obj.has("firstName") || obj.has("lastName")) {
                            response.close()
                            mainHandler.post { callback(true) }
                            return@Thread
                        }
                    }
                    false
                } else {
                    false
                }

                response.close()
                mainHandler.post { callback(success) }

            } catch (e: Exception) {
                Log.e("ApiClient", "Ping failed: ${e.message}")
                mainHandler.post { callback(false) }
            }
        }.start()
    }


    fun sendLocation(location: LocationPayload) {
        Thread {
            try {
                val json = JSONObject().apply {
                    put("id", 0)
                    put("userid", location.userid)
                    put("latitude", location.latitude)
                    put("longitude", location.longitude)
                    put("locAccuracy", location.locAccuracy)
                    put("locAltitude", location.locAltitude)
                    put("locSpeed", location.locSpeed)
                    put("locBearing", location.locBearing)
                    put("locProvider", location.locProvider)
                    put("timestampMs", location.timestampMs)
                    put("user", location.user)
                }

                val requestBody = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$BASE_URL/api/Location")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                Log.d("ApiClient", "sendLocation response: ${response.code}")
                response.close()
            } catch (e: Exception) {
                Log.e("ApiClient", "sendLocation exception: ${e.message}", e)
            }
        }.start()
    }


    fun getStudents(callback: (String) -> Unit) {
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/Students")
                    .addHeader("accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                Log.d("ApiClient", "getStudents: $body")

                if (body.isNullOrEmpty()) {
                    mainHandler.post { callback("[]") }
                } else {
                    mainHandler.post { callback(body) }
                }

                response.close()
            } catch (e: Exception) {
                Log.e("ApiClient", "getStudents exception: ${e.message}", e)
                mainHandler.post { callback("[]") }
            }
        }.start()
    }

    fun getLastKnownLocation(studentId: Int = 7, callback: (Double, Double) -> Unit) {
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/Location/LastKnown/$studentId")
                    .addHeader("accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                Log.d("ApiClient", "getLastKnownLocation: $body")

                if (!body.isNullOrEmpty()) {
                    val obj = JSONObject(body)
                    val lat = obj.getDouble("latitude")
                    val lon = obj.getDouble("longitude")
                    mainHandler.post { callback(lat, lon) }
                } else {
                    mainHandler.post { callback(0.0, 0.0) }
                }

                response.close()
            } catch (e: Exception) {
                Log.e("ApiClient", "getLastKnownLocation exception: ${e.message}", e)
                mainHandler.post { callback(0.0, 0.0) }
            }
        }.start()
    }
}
