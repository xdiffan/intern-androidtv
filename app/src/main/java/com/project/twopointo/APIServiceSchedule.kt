package com.project.twopointo

import com.project.twopointo.ui.Schedule
import retrofit2.Call
import retrofit2.http.GET

interface APIServiceSchedule {
    @GET("schedules")
    fun getSchedules():Call<Schedule>

    @GET("ukms")
    fun getUkm():Call<UKM>
}