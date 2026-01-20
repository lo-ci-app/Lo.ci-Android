package com.teamloci.loci.network

import android.content.Context
import android.content.Intent
import android.util.Log
import com.teamloci.loci.MainActivity
import com.teamloci.loci.network.api.AuthApi
import com.teamloci.loci.network.model.AuthResponse
import com.teamloci.loci.network.model.RefreshTokenRequest
import com.teamloci.loci.util.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

class TokenAuthenticator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceManager: PreferenceManager,
    private val authApiProvider: Provider<AuthApi>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d("TokenAuthenticator", "🚨 401 Unauthorized 감지! 토큰 갱신 시도...")

        if (responseCount(response) >= 3) {
            handleLogout("토큰 갱신 3회 실패")
            return null
        }

        val currentRefreshToken = preferenceManager.getRefreshToken()
        if (currentRefreshToken.isNullOrEmpty()) {
            handleLogout("저장된 리프레시 토큰 없음")
            return null
        }

        val newTokens: AuthResponse? = refreshTokens(currentRefreshToken)

        if (newTokens != null && !newTokens.accessToken.isNullOrEmpty() && !newTokens.refreshToken.isNullOrEmpty()) {
            Log.d("TokenAuthenticator", "✅ 토큰 갱신 성공! 요청 재전송.")

            val newAccessToken = newTokens.accessToken!!
            val newRefreshToken = newTokens.refreshToken!!

            preferenceManager.saveTokens(newAccessToken, newRefreshToken)

            return response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        } else {
            handleLogout("토큰 갱신 API 호출 실패 또는 토큰 없음")
            return null
        }
    }

    private fun refreshTokens(refreshToken: String): AuthResponse? {
        return try {
            runBlocking {
                val apiResponse = authApiProvider.get().reissueToken(
                    RefreshTokenRequest(refreshToken = refreshToken)
                )

                if (apiResponse.isSuccessful && apiResponse.body()?.isSuccess == true) {
                    apiResponse.body()?.result
                } else {
                    Log.e("TokenAuthenticator", "갱신 실패: ${apiResponse.code()} ${apiResponse.errorBody()?.string()}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("TokenAuthenticator", "네트워크 오류: ${e.message}")
            null
        }
    }

    private fun handleLogout(reason: String) {
        Log.e("TokenAuthenticator", "❌ 로그아웃 실행: $reason")
        preferenceManager.clear()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}