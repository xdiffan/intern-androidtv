package com.project.twopointo

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.time.measureTime

object APIConfig {
    const val baseURL="https://api.ryujin.top/api/"
    fun getRetrofit():Retrofit{
         return Retrofit.Builder()
             .baseUrl(baseURL)
             .addConverterFactory(GsonConverterFactory.create())
             .build()
    }
    fun getService():APIServiceSchedule{
        return getRetrofit().create(APIServiceSchedule::class.java)
    }
}