package com.example.goin2.API_and_location

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.goin2.main.MainActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException


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

    fun getAllClassEvents(callback: (String) -> Unit) {
        Thread {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("$BASE_URL/api/ClassEvent") // <-- fixed to use your correct BASE_URL
                    .addHeader("accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: "[]"

                callback(body)
            } catch (e: Exception) {
                callback("[]")
            }
        }.start()
    }


    fun getAllClasses(callback: (String) -> Unit) {
        val url = "$BASE_URL/api/Class"  // <- BASE_URL is already correctly set

        val request = Request.Builder()
            .url(url)
            .addHeader("accept", "application/json")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
                callback("[]")  // fallback to empty list on failure to avoid freezing
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val body = response.body?.string() ?: "[]"
                println("DEBUG: getAllClasses received body: $body")  // <<< ADD massive debug print
                callback(body)
            }
        })
    }

    fun updateEvent(eventId: Int, updatedData: JSONObject, callback: (Boolean) -> Unit) {
        Thread {
            try {
                val body = updatedData.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$BASE_URL/api/Event/$eventId")
                    .put(body)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val success = response.isSuccessful
                response.close()

                mainHandler.post { callback(success) }
            } catch (e: Exception) {
                Log.e("ApiClient", "updateEvent error: ${e.message}", e)
                mainHandler.post { callback(false) }
            }
        }.start()
    }

    fun sendNotification(notificationData: JSONObject, callback: (Boolean) -> Unit) {
        Thread {
            try {
                val body = notificationData.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$BASE_URL/api/Notification")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val success = response.isSuccessful
                response.close()

                mainHandler.post { callback(success) }
            } catch (e: Exception) {
                Log.e("ApiClient", "sendNotification error: ${e.message}", e)
                mainHandler.post { callback(false) }
            }
        }.start()
    }

    fun getActivePair(eventId: Int, studentId: Int, callback: (JSONObject?) -> Unit) {
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/Pair/Event/$eventId/Student/$studentId/Active")
                    .addHeader("accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (response.isSuccessful && !body.isNullOrEmpty()) {
                    callback(JSONObject(body))
                } else {
                    callback(null)
                }

                response.close()
            } catch (e: Exception) {
                Log.e("ApiClient", "getActivePair error: ${e.message}", e)
                callback(null)
            }
        }.start()
    }

    fun getBuddyName(userId: Int, callback: (String?) -> Unit) {
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/User/$userId")
                    .addHeader("accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (!body.isNullOrEmpty()) {
                    val obj = JSONObject(body)
                    val name = "${obj.optString("firstName", "")} ${obj.optString("lastName", "")}".trim()
                    callback(name)
                } else {
                    callback(null)
                }

                response.close()
            } catch (e: Exception) {
                Log.e("ApiClient", "getBuddyName error: ${e.message}", e)
                callback(null)
            }
        }.start()
    }


    fun getAllClassRosters(callback: (String) -> Unit) {
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/ClassRoster")
                    .get()
                    .addHeader("accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: "[]"
                response.close()

                mainHandler.post { callback(body) }
            } catch (e: Exception) {
                Log.e("ApiClient", "getAllClassRosters error: ${e.message}", e)
                mainHandler.post { callback("[]") }
            }
        }.start()
    }

    fun postLocation(payload: JSONObject) {
        Thread {
            try {
                val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$BASE_URL/api/Location")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                Log.d("ApiClient", "postLocation response: ${response.code}")
                response.close()
            } catch (e: Exception) {
                Log.e("ApiClient", "postLocation exception: ${e.message}", e)
            }
        }.start()
    }

    fun getNotificationsByUser(userId: Int, callback: (List<NotificationResult>) -> Unit) {
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/Notification/user/$userId")
                    .get()
                    .addHeader("accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()
                response.close()

                if (body.isNullOrEmpty()) {
                    mainHandler.post { callback(emptyList()) }
                    return@Thread
                }

                val resultList = mutableListOf<NotificationResult>()
                val jsonArray = JSONArray(body)

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    resultList.add(
                        NotificationResult(
                            id = obj.getInt("id"),
                            userid = obj.getInt("userid"),
                            eventid = obj.getInt("eventid"),
                            notificationDescription = obj.getString("notificationDescription"),
                            notificationTimestamp = obj.getString("notificationTimestamp")
                        )
                    )
                }

                mainHandler.post { callback(resultList) }

            } catch (e: Exception) {
                Log.e("ApiClient", "getNotificationsByUser exception: ${e.message}", e)
                mainHandler.post { callback(emptyList()) }
            }
        }.start()
    }

    fun sendLocation(location: LocationPayload) {
        Thread {
            try {
                val json = JSONObject().apply {
                    put("userid", location.userid)
                    put("latitude", location.latitude)
                    put("longitude", location.longitude)
                    put("locAccuracy", location.locAccuracy)
                    put("locAltitude", location.locAltitude)
                    put("locSpeed", location.locSpeed)
                    put("locBearing", location.locBearing)
                    put("locProvider", location.locProvider)
                    put("timestampMs", location.timestampMs)
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

    fun getActivePairsForEvent(eventId: Int, callback: (String) -> Unit) {
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/Pair/Event/$eventId/Active")
                    .addHeader("accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""
                response.close()
                mainHandler.post { callback(body) }
            } catch (e: Exception) {
                mainHandler.post { callback("") }
            }
        }.start()
    }

    fun updateGoIn2Pair(pairId: Int, payload: JSONObject, callback: (Boolean) -> Unit) {
        Thread {
            try {
                val body = payload.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$BASE_URL/api/Pair/$pairId")
                    .put(body)
                    .addHeader("accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val success = response.isSuccessful
                response.close()
                mainHandler.post { callback(success) }
            } catch (e: Exception) {
                mainHandler.post { callback(false) }
            }
        }.start()
    }



    fun createGoIn2Pair(student1Id: Int, student2Id: Int, eventId: Int, callback: (Boolean) -> Unit) {
        Thread {
            try {
                val json = JSONObject()
                json.put("student1id", student1Id)
                json.put("student2id", student2Id)
                json.put("eventid", eventId)
                json.put("status", true)

                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$BASE_URL/api/Pair")
                    .post(body)
                    .addHeader("accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val success = response.isSuccessful
                response.close()

                mainHandler.post { callback(success) }
            } catch (e: Exception) {
                Log.e("ApiClient", "createGoIn2Pair failed: ${e.message}", e)
                mainHandler.post { callback(false) }
            }
        }.start()
    }


    fun getLastKnownLocation(userId: Int, callback: (Double, Double) -> Unit) {
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/Location/latest/$userId")
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


    fun createGeoFence(
        eventRadius: Int,
        teacherRadius: Int,
        pairDistance: Int,
        latitude: Double,
        longitude: Double,
        callback: (Int?) -> Unit
    ) {
        Thread {
            try {
                val json = JSONObject().apply {
                    put("eventRadius", eventRadius)
                    put("teacherRadius", teacherRadius)
                    put("pairDistance", pairDistance)
                    put("latitude", latitude)
                    put("longitude", longitude)
                }

                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$BASE_URL/api/GeoFence")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val resBody = response.body?.string()
                response.close()

                val id = resBody?.let { JSONObject(it).getInt("id") }
                mainHandler.post { callback(id) }
            } catch (e: Exception) {
                Log.e("ApiClient", "createGeoFence error: ${e.message}", e)
                mainHandler.post { callback(null) }
            }
        }.start()
    }

    fun getAllEvents(callback: (String) -> Unit) {
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/Event")
                    .get()
                    .addHeader("accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()
                response.close()

                mainHandler.post {
                    callback(body ?: "[]")
                }
            } catch (e: Exception) {
                Log.e("ApiClient", "getAllEvents error: ${e.message}", e)
                mainHandler.post { callback("[]") }
            }
        }.start()
    }

    fun getGeoFence(id: Int, callback: (String) -> Unit) {
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/GeoFence/$id")
                    .get()
                    .addHeader("accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()
                response.close()

                mainHandler.post {
                    callback(body ?: "{}")
                }
            } catch (e: Exception) {
                Log.e("ApiClient", "getGeoFence error: ${e.message}", e)
                mainHandler.post { callback("{}") }
            }
        }.start()
    }

    fun getClassEventsByEvent(eventId: Int, callback: (String) -> Unit) {
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/ClassEvent/Event/$eventId")
                    .get()
                    .addHeader("accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()
                response.close()

                mainHandler.post {
                    callback(body ?: "[]")
                }
            } catch (e: Exception) {
                Log.e("ApiClient", "getClassEventsByEvent error: ${e.message}", e)
                mainHandler.post { callback("[]") }
            }
        }.start()
    }

    fun getClassById(id: Int, callback: (String) -> Unit) {
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/Class/$id")
                    .get()
                    .addHeader("accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()
                response.close()

                mainHandler.post {
                    callback(body ?: "{}")
                }
            } catch (e: Exception) {
                Log.e("ApiClient", "getClassById error: ${e.message}", e)
                mainHandler.post { callback("{}") }
            }
        }.start()
    }



    fun createEvent(
        name: String,
        date: String,
        location: String,
        teacherId: Int,
        geoFenceId: Int,
        callback: (Int?) -> Unit
    ) {
        Thread {
            try {
                val json = JSONObject().apply {
                    put("eventName", name)
                    put("eventDate", date)
                    put("eventLocation", location)
                    put("status", false)
                    put("teacherid", teacherId)
                    put("geofenceid", geoFenceId)
                }

                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$BASE_URL/api/Event")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val resBody = response.body?.string()
                response.close()

                val id = resBody?.let { JSONObject(it).getInt("id") }
                mainHandler.post { callback(id) }
            } catch (e: Exception) {
                Log.e("ApiClient", "createEvent error: ${e.message}", e)
                mainHandler.post { callback(null) }
            }
        }.start()
    }

    fun deleteClass(classId: Int, callback: (Boolean) -> Unit) {
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/Class/$classId")
                    .delete()
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                val statusCode = response.code

                Log.d("DELETE_CLASS", "DELETE /api/Class/$classId → HTTP $statusCode, body: $responseBody")

                val success = response.isSuccessful
                response.close()
                mainHandler.post { callback(success) }
            } catch (e: Exception) {
                Log.e("DELETE_CLASS", "Error: ${e.message}", e)
                mainHandler.post { callback(false) }
            }
        }.start()
    }



    fun linkClassToEvent(classId: Int, eventId: Int, callback: (Boolean) -> Unit) {
        Thread {
            try {
                val json = JSONObject().apply {
                    put("classid", classId)
                    put("eventid", eventId)
                }

                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$BASE_URL/api/ClassEvent")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val success = response.isSuccessful
                response.close()

                mainHandler.post { callback(success) }
            } catch (e: Exception) {
                Log.e("ApiClient", "linkClassToEvent error: ${e.message}", e)
                mainHandler.post { callback(false) }
            }
        }.start()
    }
}
