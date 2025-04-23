package com.example.goin2.API_and_location

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.goin2.main.MainActivity
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

    fun createTeacher(
        first: String,
        last: String,
        context: Context,
        callback: (Int?) -> Unit
    ) {
        Thread {
            try {
                val json = JSONObject().apply {
                    put("firstName", first)
                    put("lastName", last)
                    put("userType", "teacher") // Always lowercase
                }

                val requestBody = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$BASE_URL/api/User")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()
                Log.d("ApiClient", "createTeacher response: $body")

                if (!response.isSuccessful || body.isNullOrEmpty()) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Failed to create teacher. Status: ${response.code}", Toast.LENGTH_SHORT).show()
                        callback(null)
                    }
                    return@Thread
                }

                val createdUser = JSONObject(body)
                val id = createdUser.optInt("id", -1)

                if (id != -1) {
                    MainActivity.currentTeacherId = id
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Teacher created successfully.", Toast.LENGTH_SHORT).show()
                        callback(id)
                    }
                } else {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Teacher creation failed — invalid response.", Toast.LENGTH_SHORT).show()
                        callback(null)
                    }
                }

            } catch (e: Exception) {
                Log.e("ApiClient", "createTeacher exception: ${e.message}", e)
                val message = if (e.message?.contains("already exists", ignoreCase = true) == true) {
                    "Teacher already exists."
                } else {
                    "Error creating teacher: ${e.message}"
                }

                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    callback(null)
                }
            }
        }.start()
    }

    fun getClassesByTeacher(teacherId: Int, callback: (List<Pair<Int, String>>) -> Unit) {
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/Class")
                    .addHeader("accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()
                val result = mutableListOf<Pair<Int, String>>()

                if (!body.isNullOrEmpty()) {
                    val json = JSONArray(body)
                    for (i in 0 until json.length()) {
                        val item = json.getJSONObject(i)
                        if (item.getInt("teacherid") == teacherId) {
                            val classId = item.getInt("id")
                            val className = item.getString("className")
                            result.add(Pair(classId, className))
                        }
                    }
                }

                response.close()
                mainHandler.post { callback(result) }
            } catch (e: Exception) {
                Log.e("ApiClient", "getClassesByTeacher error", e)
                mainHandler.post { callback(emptyList()) }
            }
        }.start()
    }

    fun createClass(teacherId: Int, className: String, callback: (Boolean) -> Unit) {
        Thread {
            try {
                val json = JSONObject().apply {
                    put("teacherid", teacherId)
                    put("className", className)
                }

                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$BASE_URL/api/Class")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val success = response.isSuccessful
                response.close()

                mainHandler.post { callback(success) }
            } catch (e: Exception) {
                Log.e("ApiClient", "createClass error: ${e.message}", e)
                mainHandler.post { callback(false) }
            }
        }.start()
    }

    fun addStudentToClass(classId: Int, studentId: Int, callback: (Boolean) -> Unit) {
        Thread {
            try {
                val json = JSONObject().apply {
                    put("classid", classId)
                    put("studentid", studentId)
                }

                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$BASE_URL/api/ClassRoster")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val success = response.isSuccessful
                response.close()

                mainHandler.post { callback(success) }
            } catch (e: Exception) {
                Log.e("ApiClient", "addStudentToClass error: ${e.message}", e)
                mainHandler.post { callback(false) }
            }
        }.start()
    }

    fun deleteStudent(studentId: Int, callback: (Boolean) -> Unit) {
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/User/$studentId")
                    .delete()
                    .build()

                val response = client.newCall(request).execute()
                val success = response.isSuccessful
                response.close()
                mainHandler.post { callback(success) }

            } catch (e: Exception) {
                Log.e("ApiClient", "deleteStudent error: ${e.message}", e)
                mainHandler.post { callback(false) }
            }
        }.start()
    }


    fun createStudent(context: Context, first: String, last: String, callback: (Int?) -> Unit) {
        Thread {
            try {
                val json = JSONObject().apply {
                    put("firstName", first)
                    put("lastName", last)
                    put("userType", "student")  // lowercase
                }

                val requestBody = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$BASE_URL/api/User")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()
                Log.d("ApiClient", "createStudent response: $body")

                if (!response.isSuccessful || body.isNullOrEmpty()) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Failed to create student. Status: ${response.code}", Toast.LENGTH_SHORT).show()
                        callback(null)
                    }
                    return@Thread
                }

                val createdUser = JSONObject(body)
                val id = createdUser.optInt("id", -1)

                if (id != -1) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Student created successfully.", Toast.LENGTH_SHORT).show()
                        callback(id)
                    }
                } else {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Student creation failed — invalid response.", Toast.LENGTH_SHORT).show()
                        callback(null)
                    }
                }

            } catch (e: Exception) {
                Log.e("ApiClient", "createStudent exception: ${e.message}", e)
                val message = if (e.message?.contains("already exists", ignoreCase = true) == true) {
                    "Student already exists."
                } else {
                    "Error creating student: ${e.message}"
                }

                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    callback(null)
                }
            }
        }.start()
    }



    fun getClassRoster(classId: Int, callback: (List<Int>) -> Unit) {
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/ClassRoster")
                    .addHeader("accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()
                val result = mutableListOf<Int>()

                if (!body.isNullOrEmpty()) {
                    val json = JSONArray(body)
                    for (i in 0 until json.length()) {
                        val item = json.getJSONObject(i)
                        if (item.getInt("classid") == classId) {
                            val studentId = item.getInt("studentid")
                            result.add(studentId)
                        }
                    }
                }

                response.close()
                mainHandler.post { callback(result) }
            } catch (e: Exception) {
                Log.e("ApiClient", "getClassRoster error", e)
                mainHandler.post { callback(emptyList()) }
            }
        }.start()
    }

    fun getUserById(userId: Int, callback: (Pair<String, String>?) -> Unit) {
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/User/$userId")
                    .addHeader("accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()
                var result: Pair<String, String>? = null

                if (!body.isNullOrEmpty()) {
                    val json = JSONObject(body)
                    val first = json.getString("firstName")
                    val last = json.getString("lastName")
                    result = Pair(first, last)
                }

                response.close()
                mainHandler.post { callback(result) }
            } catch (e: Exception) {
                Log.e("ApiClient", "getUserById error", e)
                mainHandler.post { callback(null) }
            }
        }.start()
    }



    fun loginTeacher(
        first: String,
        last: String,
        context: Context,
        callback: (Int?) -> Unit
    ) {
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/User")
                    .addHeader("accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()
                Log.d("ApiClient", "loginTeacher response: $body")

                if (body.isNullOrEmpty()) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "No response from server.", Toast.LENGTH_SHORT).show()
                        callback(null)
                    }
                    return@Thread
                }

                val jsonArray = JSONArray(body)
                for (i in 0 until jsonArray.length()) {
                    val user = jsonArray.getJSONObject(i)
                    val fName = user.getString("firstName")
                    val lName = user.getString("lastName")
                    val userType = user.getString("userType")

                    if (fName.equals(first, ignoreCase = true) && lName.equals(last, ignoreCase = true)) {
                        if (userType.equals("teacher", ignoreCase = true)) {
                            val id = user.getInt("id")
                            MainActivity.currentTeacherId = id
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                                callback(id)
                            }
                            return@Thread
                        } else {
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(context, "Account is not a teacher.", Toast.LENGTH_SHORT).show()
                                callback(null)
                            }
                            return@Thread
                        }
                    }
                }

                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Teacher not found.", Toast.LENGTH_SHORT).show()
                    callback(null)
                }

            } catch (e: Exception) {
                Log.e("ApiClient", "loginTeacher exception: ${e.message}", e)
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    callback(null)
                }
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
