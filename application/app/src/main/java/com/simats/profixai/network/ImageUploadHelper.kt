package com.simats.profixai.network

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

object ImageUploadHelper {
    
    fun createImagePart(context: Context, uri: Uri, partName: String = "image"): MultipartBody.Part? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()
            
            val requestBody = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            return MultipartBody.Part.createFormData(partName, tempFile.name, requestBody)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    fun createIdPart(id: Int): okhttp3.RequestBody {
        return id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
    }
}
