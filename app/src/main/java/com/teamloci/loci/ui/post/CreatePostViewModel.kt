package com.teamloci.loci.ui.post

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamloci.loci.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PostUiState>(PostUiState.Idle)
    val uiState = _uiState.asStateFlow()

    val capturedImageUri = MutableStateFlow<Uri?>(null)

    var tempPhotoUri: Uri? = null

    fun createTempUri(): Uri {
        val tempFile = File.createTempFile("loci_cam_", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider", // AndroidManifest authorities와 일치해야 함
            tempFile
        ).also { tempPhotoUri = it }
    }

    fun uploadPost(content: String, lat: Double, lng: Double) {
        val uri = capturedImageUri.value ?: return

        viewModelScope.launch {
            _uiState.value = PostUiState.Loading

            val imageResult = postRepository.uploadImage(uri)

            imageResult.onSuccess { imageUrl ->
                val postResult = postRepository.createPost(content, imageUrl, lat, lng)

                if (postResult.isSuccess) {
                    _uiState.value = PostUiState.Success
                } else {
                    _uiState.value = PostUiState.Error(postResult.exceptionOrNull()?.message ?: "게시물 생성 실패")
                }
            }.onFailure {
                _uiState.value = PostUiState.Error("이미지 업로드 실패: ${it.message}")
            }
        }
    }
}

sealed interface PostUiState {
    object Idle : PostUiState
    object Loading : PostUiState
    object Success : PostUiState
    data class Error(val msg: String) : PostUiState
}