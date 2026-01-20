package com.teamloci.loci.network.api

import com.teamloci.loci.network.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("/api/v1/auth/signup/phone")
    suspend fun signUpPhone(
        @Body request: AuthRequest
    ): Response<ApiResponse<AuthResponse>>

    @POST("/api/v1/auth/login/phone")
    suspend fun loginPhone(
        @Body request: AuthRequest
    ): Response<ApiResponse<AuthResponse>>

    @POST("/api/v1/auth/reissue")
    suspend fun reissueToken(
        @Body request: RefreshTokenRequest
    ): Response<ApiResponse<AuthResponse>>
}