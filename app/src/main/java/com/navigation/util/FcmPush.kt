package com.navigation.util

import android.util.Log
import com.google.common.net.MediaType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.navigation.model.PushDTO
import com.squareup.okhttp.*
import java.io.IOException


class FcmPush {
    var JSON = com.squareup.okhttp.MediaType.parse("application/json; charset=utf-8")
    var url = "https://fcm.googleapis.com/fcm/send"
    var serverKey = "AIzaSyCMmeXGtb1gPpNRRxtzUyOuWwrdiKTIuJ4"
    var gson : Gson? = null
    var okHttpClient : OkHttpClient? = null
    companion object {
        var instance = FcmPush()
    }
    init {
        gson = Gson()
        okHttpClient = OkHttpClient()
    }
    fun sendMessage(destinationUid : String, title : String, message : String) {
        FirebaseFirestore.getInstance().collection("pushtoken").document(destinationUid).get().addOnCompleteListener { task ->
            if(task.isSuccessful) {
                var token = task?.result?.get("pushToken").toString()

                var pushDTO = PushDTO()
                pushDTO.to = token
                pushDTO.notification.title = title
                pushDTO.notification.body = message

                var body = RequestBody.create(JSON,gson?.toJson(pushDTO))
                var request = Request.Builder()
                    .addHeader("Content-Type","application/json")
                    .addHeader("Authorization","key="+serverKey)
                    .url(url)
                    .post(body)
                    .build()

                okHttpClient?.newCall(request)?.enqueue(object : Callback {
                    override fun onFailure(request: Request?, e: IOException?) {
                        Log.i("0","failed..")

                    }

                    override fun onResponse(response: Response?) {
                        Log.i("1","success..")
                        println(response?.body()?.string())

                    }

                })
            }
        }
    }
}