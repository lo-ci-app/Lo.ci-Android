package com.teamloci.loci.network.model

import com.google.gson.annotations.SerializedName

data class CreatePostRequest(
    @SerializedName("content") val content: String,
    @SerializedName("mediaUrl") val mediaUrl: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)

data class CreatePostResponse(
    @SerializedName("postId") val postId: Long
)