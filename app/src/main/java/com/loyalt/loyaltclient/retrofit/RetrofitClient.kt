package com.loyalt.loyaltclient.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
 private const val BASE_URL = "https://prowess-grove-enroll.ngrok-free.dev"

    val api: LoyaltyApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LoyaltyApi::class.java)
    }
}