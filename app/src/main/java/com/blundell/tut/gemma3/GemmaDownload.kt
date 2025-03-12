package com.blundell.tut.gemma3

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException

/**
 * /data/user/0/com.blundell.tut.gemma3/files/gemma3-1b-it-int4.task
 */
class GemmaDownload(
    private val huggingFaceToken: String,
    private val okHttpClient: OkHttpClient,
) {

    fun downloadGemmaModel(directory: File): Flow<DownloadResult> = flow {
        val url = "https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/gemma3-1b-it-int4.task?download=true"
        val fileName = "gemma3-1b-it-int4.task"
        val file = File(directory, fileName)

        if (file.exists()) {
            Log.d("TUT", "File already exists, skipping download.")
            emit(DownloadResult.Success(file))
            return@flow // Skip the download
        }

        Log.d("TUT", "Download starting!")
        try {
            val response = okHttpClient
                .newCall(
                    Request.Builder()
                        .url(url)
                        .header("Authorization", "Bearer $huggingFaceToken")
                        .build()
                )
                .execute()
            Log.d("TUT", "Download ended!")
            if (!response.isSuccessful) {
                Log.e("TUT", "Download Not successful.")
                emit(DownloadResult.Error("Download failed: ${response.code}"))
                return@flow
            }

            val source = response.body?.source()
            if (source == null) {
                emit(DownloadResult.Error("Empty response body"))
            } else {
                file.sink().buffer().use { sink ->
                    source.readAll(sink)
                }
                Log.d("TUT", "Success!")
                emit(DownloadResult.Success(file))
            }
        } catch (e: IOException) {
            Log.e("TUT", "Download IO Error", e)
            emit(DownloadResult.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            Log.e("TUT", "Download General Error", e)
            emit(DownloadResult.Error("An unexpected error occurred: ${e.message}"))
        }
    }
}

sealed class DownloadResult {
    data class Success(val file: File) : DownloadResult()
    data class Error(val message: String) : DownloadResult()
}
