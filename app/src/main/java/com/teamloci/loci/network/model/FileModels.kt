package com.teamloci.loci.network.model

import com.google.gson.annotations.SerializedName

data class PresignedUrlRequest(
    @SerializedName("prefix") val prefix: String,
    @SerializedName("fileName") val fileName: String
)

data class PresignedUrlResponse(
    @SerializedName("presignedUrl") val presignedUrl: String,
    @SerializedName("imageKey") val imageKey: String
)