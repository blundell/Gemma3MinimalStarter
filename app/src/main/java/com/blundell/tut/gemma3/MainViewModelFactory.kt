package com.blundell.tut.gemma3

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import okhttp3.OkHttpClient

class MainViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val gemmaDownload = GemmaDownload(
                BuildConfig.HUGGINGFACE_DOWNLOAD_API_KEY,
                OkHttpClient(),
            )
            return MainViewModel(gemmaDownload, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
