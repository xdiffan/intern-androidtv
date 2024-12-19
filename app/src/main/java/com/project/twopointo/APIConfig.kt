package com.project.twopointo

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object APIConfig {
    private const val BASEURL="https://api.dickypurnomo.com/api/"
    private fun getRetrofit():Retrofit{
         return Retrofit.Builder()
             .baseUrl(BASEURL)
             .addConverterFactory(GsonConverterFactory.create())
             .build()
    }
    fun getService():APIServiceSchedule{
        return getRetrofit().create(APIServiceSchedule::class.java)
    }
}