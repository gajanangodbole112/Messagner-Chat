package com.gajanan.messenger_chat.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://fcm.googleapis.com/fcm/"
private  val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

object GetApi {

    val retrofitService : ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

}