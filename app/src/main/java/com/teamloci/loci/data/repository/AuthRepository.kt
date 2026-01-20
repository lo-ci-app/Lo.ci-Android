package com.teamloci.loci.data.repository

import com.teamloci.loci.network.api.AuthApi
import com.teamloci.loci.network.model.AuthRequest
import com.teamloci.loci.network.model.AuthResponse
import com.teamloci.loci.util.PreferenceManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val preferenceManager: PreferenceManager
) {
    suspend fun login(idToken: String): Result<AuthResponse> {
        return try {
            val request = AuthRequest(idToken = idToken)
            val response = authApi.loginPhone(request)

            if (response.isSuccessful && response.body()?.isSuccess == true) {
                val result = response.body()?.result!!

                if (!result.accessToken.isNullOrEmpty() && !result.refreshToken.isNullOrEmpty()) {
                    preferenceManager.saveTokens(result.accessToken, result.refreshToken)
                }

                Result.success(result)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(idToken: String, nickname: String, handle: String): Result<Boolean> {
        return try {
            val request = AuthRequest(idToken, nickname, handle)
            val response = authApi.signUpPhone(request)

            if (response.isSuccessful && response.body()?.isSuccess == true) {
                Result.success(true)
            } else {
                val errorMsg = response.body()?.message ?: "Sign up failed with unknown error"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        preferenceManager.clear()
    }

    fun isLoggedIn(): Boolean {
        return !preferenceManager.getAccessToken().isNullOrEmpty()
    }
}