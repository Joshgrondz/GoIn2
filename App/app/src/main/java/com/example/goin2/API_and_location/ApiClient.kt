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
        Log.d("ApiClient", "Stubbed getStudentIdByName: always returning 1")
        Thread {
            mainHandler.post {
                callback(1) // always return valid student ID
            }
        }.start()
    }

    fun pingServer(callback: (Boolean) -> Unit) {
        Log.d("ApiClient", "Stubbed pingServer: always true")
        Thread {
            mainHandler.post {
                callback(true)
            }
        }.start()
    }

    fun sendLocation(location: LocationPayload, studentId: Int = 7) {
        Log.d("ApiClient", "Stubbed sendLocation: always succeeds")
        Log.d("ApiClient", "Pretend we sent this: $location for ID=$studentId")
    }

    fun getStudents(callback: (String) -> Unit) {
        Log.d("ApiClient", "Stubbed getStudents: always returns 'true'")
        Thread {
            mainHandler.post {
                callback("true")
            }
        }.start()
    }

    fun getLastKnownLocation(studentId: Int = 7, callback: (Double, Double) -> Unit) {
        Log.d("ApiClient", "Stubbed getLastKnownLocation: always returns fake coords")
        Thread {
            mainHandler.post {
                callback(40.0, -79.0) // fake LatLng
            }
        }.start()
    }
}
