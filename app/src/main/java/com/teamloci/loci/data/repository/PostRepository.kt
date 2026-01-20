package com.teamloci.loci.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.teamloci.loci.network.api.FileApi
import com.teamloci.loci.network.api.PostApi
import com.teamloci.loci.network.model.CreatePostRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileApi: FileApi,
    private val postApi: PostApi
) {

    suspend fun uploadImage(imageUri: Uri): Result<String> {
        return try {
            val file = uriToFile(imageUri) ?: return Result.failure(Exception("파일 변환 실패"))
            val mimeType = getMimeType(file) ?: "image/jpeg"
            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = fileApi.uploadImage(body)

            file.delete()

            if (response.isSuccessful && response.body()?.isSuccess == true) {
                Result.success(response.body()?.result ?: "")
            } else {
                Result.failure(Exception("이미지 업로드 실패: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPost(content: String, mediaUrl: String, lat: Double, lng: Double): Result<Boolean> {
        return try {
            val request = CreatePostRequest(content, mediaUrl, lat, lng)
            val response = postApi.createPost(request)

            if (response.isSuccessful && response.body()?.isSuccess == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.message ?: "게시물 생성 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            file
        } catch (e: Exception) { null }
    }

    private fun getMimeType(file: File): String? {
        val ext = MimeTypeMap.getFileExtensionFromUrl(file.path)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
    }
}