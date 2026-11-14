
package com.example.diarioespacial.data.remote

import com.example.diarioespacial.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NasaRetrofitClient {

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.nasa.gov/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: NasaApiService by lazy {
        retrofit.create(NasaApiService::class.java)
    }

    val apiKey: String
        get() = BuildConfig.NASA_API_KEY
}
