package com.project.twopointo.ui
import androidx.annotation.Keep

import com.google.gson.annotations.SerializedName


@Keep
data class Schedule(
    @SerializedName("data")
    val `data`: List<Data?>?,
    @SerializedName("success")
    val success: Boolean?
)

@Keep
data class Data(
    @SerializedName("class_id")
    val classId: Int?,
    @SerializedName("class")
    val classX: Class?,
    @SerializedName("course")
    val course: Course?,
    @SerializedName("course_id")
    val courseId: Int?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("day")
    val day: String?,
    @SerializedName("end_time")
    val endTime: String?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("lecturer")
    val lecturer: Lecturer?,
    @SerializedName("lecturer_id")
    val lecturerId: Int?,
    @SerializedName("room")
    val room: String?,
    @SerializedName("start_time")
    val startTime: String?,
    @SerializedName("status")
    val status: Status?,
    @SerializedName("status_id")
    val statusId: Int?,
    @SerializedName("updated_at")
    val updatedAt: String?
)

@Keep
data class Class(
    @SerializedName("created_at")
    val createdAt: Any?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("updated_at")
    val updatedAt: Any?
)

@Keep
data class Course(
    @SerializedName("code")
    val code: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)

@Keep
data class Lecturer(
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("department")
    val department: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)

@Keep
data class Status(
    @SerializedName("created_at")
    val createdAt: Any?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("updated_at")
    val updatedAt: Any?
)