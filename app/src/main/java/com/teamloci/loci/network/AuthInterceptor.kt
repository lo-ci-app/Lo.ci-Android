package com.teamloci.loci.network

import com.teamloci.loci.util.PreferenceManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val preferenceManager: PreferenceManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        val accessToken = preferenceManager.getAccessToken()
        if (!accessToken.isNullOrEmpty()) {
            builder.addHeader("Authorization", "Bearer $accessToken")
        }

        builder.addHeader("Content-Type", "application/json")

        return chain.proceed(builder.build())
    }
}