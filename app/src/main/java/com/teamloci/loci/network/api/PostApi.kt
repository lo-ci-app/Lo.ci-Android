package com.teamloci.loci.network.api

import com.teamloci.loci.network.model.ApiResponse
import com.teamloci.loci.network.model.CreatePostRequest
import com.teamloci.loci.network.model.CreatePostResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PostApi {
    @POST("/api/v1/posts")
    suspend fun createPost(
        @Body request: CreatePostRequest
    ): Response<ApiResponse<CreatePostResponse>>
}