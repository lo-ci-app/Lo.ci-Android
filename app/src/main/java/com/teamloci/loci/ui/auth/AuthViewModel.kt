package com.teamloci.loci.ui.auth

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.teamloci.loci.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val phoneNumber = MutableStateFlow("")
    val verificationCode = MutableStateFlow("")
    val nickname = MutableStateFlow("")
    val handle = MutableStateFlow("")

    private val auth = FirebaseAuth.getInstance()
    private var storedVerificationId: String? = null

    fun sendVerificationCode(activity: Activity) {
        val phone = phoneNumber.value
        if (phone.isBlank()) return

        _uiState.value = AuthUiState.Loading

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _uiState.value = AuthUiState.Error("SMS 발송 실패: ${e.message}")
                    Log.e("AuthViewModel", "Verification Failed", e)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Log.d("AuthViewModel", "SMS sent successfully")
                    storedVerificationId = verificationId
                    _uiState.value = AuthUiState.CodeSent
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyCode() {
        val code = verificationCode.value
        val verificationId = storedVerificationId

        if (verificationId == null || code.isBlank()) return

        _uiState.value = AuthUiState.Loading
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user ?: throw Exception("User is null")
                val idTokenResult = user.getIdToken(true).await()
                val idToken = idTokenResult.token ?: throw Exception("Token is null")

                Log.d("AuthViewModel", "ID Token acquired. Requesting Login...")

                val result = authRepository.login(idToken)

                if (result.isSuccess) {
                    val data = result.getOrNull()
                    if (data?.isNewUser == true) {
                        _uiState.value = AuthUiState.NeedSignUp
                    } else {
                        _uiState.value = AuthUiState.Success
                    }
                } else {
                    _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "서버 로그인 실패")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("인증 프로세스 실패: ${e.message}")
                Log.e("AuthViewModel", "SignIn Process Failed", e)
            }
        }
    }

    fun signUp() {
        val nick = nickname.value
        val userHandle = handle.value

        if (nick.isBlank() || userHandle.isBlank()) {
            _uiState.value = AuthUiState.Error("닉네임과 핸들을 모두 입력해주세요.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val user = auth.currentUser
                if (user == null) {
                    _uiState.value = AuthUiState.Error("인증 정보가 만료되었습니다. 다시 로그인해주세요.")
                    return@launch
                }

                val idToken = user.getIdToken(true).await().token ?: ""

                val signUpResult = authRepository.signUp(idToken, nick, userHandle)

                if (signUpResult.isSuccess) {
                    Log.d("AuthViewModel", "🎉 SignUp Success! Requesting Login to get tokens...")

                    val loginResult = authRepository.login(idToken)

                    if (loginResult.isSuccess) {
                        Log.d("AuthViewModel", "✅ Login Success after SignUp.")
                        _uiState.value = AuthUiState.Success
                    } else {
                        _uiState.value = AuthUiState.Error("가입은 되었으나 로그인에 실패했습니다.")
                    }
                } else {
                    val errorMsg = signUpResult.exceptionOrNull()?.message ?: "회원가입 실패"
                    Log.e("AuthViewModel", "SignUp Failed: $errorMsg")
                    _uiState.value = AuthUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "SignUp Exception", e)
                _uiState.value = AuthUiState.Error("오류 발생: ${e.message}")
            }
        }
    }
}

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    object CodeSent : AuthUiState
    object Success : AuthUiState
    object NeedSignUp : AuthUiState
    data class Error(val message: String) : AuthUiState
}