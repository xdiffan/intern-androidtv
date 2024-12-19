package com.project.twopointo
import androidx.annotation.Keep

import com.google.gson.annotations.SerializedName


class UKM : ArrayList<UKMItem>()

@Keep
data class UKMItem(
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("url")
    val url: String?
)