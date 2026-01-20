package com.teamloci.loci.network.api

import com.teamloci.loci.network.model.ApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Url

interface FileApi {

    @Multipart
    @POST("/api/v1/files/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<String>>

    @PUT
    suspend fun uploadImageToS3(
        @Url url: String,
        @Body image: RequestBody
    ): Response<Unit>
}