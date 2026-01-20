package com.teamloci.loci.network.model

import com.google.gson.annotations.SerializedName

data class AuthRequest(
    @SerializedName("idToken") val idToken: String,
    @SerializedName("nickname") val nickname: String? = null,
    @SerializedName("handle") val handle: String? = null
)

data class AuthResponse(
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("refreshToken") val refreshToken: String?,
    @SerializedName("isNewUser") val isNewUser: Boolean
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)