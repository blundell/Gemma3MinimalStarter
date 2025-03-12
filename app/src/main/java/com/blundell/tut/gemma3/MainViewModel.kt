package com.blundell.tut.gemma3

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class MainViewModel(
    gemmaDownload: GemmaDownload,
    application: Application,
) : ViewModel() {
    private val _mainState: MutableStateFlow<MainState> = MutableStateFlow(MainState.Idle)
    val mainState: StateFlow<MainState> = _mainState

    init {
        viewModelScope.launch {
            _mainState.value = MainState.LoadingModel
            gemmaDownload
                .downloadGemmaModel(application.filesDir)
                .flowOn(Dispatchers.IO)
                .collect { result ->
                    when (result) {
                        is DownloadResult.Error -> {
                            Log.e("TUT", "Model download error ${result.message}.")
                            _mainState.value = MainState.Error
                        }
                        is DownloadResult.Success -> {
                            Log.d("TUT", "Model downloaded successfully to ${result.file}.")
                            // Set the configuration options for the LLM Inference task
                            val interfaceOptions = LlmInference.LlmInferenceOptions.builder()
                                .setModelPath(result.file.path)
                                .setMaxTokens(1000)
                                .setPreferredBackend(LlmInference.Backend.CPU) // To work on the emulator
                                .build()
                            // Create an instance of the LLM Inference task
                            val llmInference = LlmInference.createFromOptions(application, interfaceOptions)
                            val sessionOptions = LlmInferenceSession.LlmInferenceSessionOptions.builder()
                                .setTemperature(0.8f)
                                .setTopK(40)
                                .setTopP(0.95f)
                                .build()
                            val llmInferenceSession = LlmInferenceSession.createFromOptions(llmInference, sessionOptions)
                            _mainState.emit(
                                MainState.LoadedModel(
                                    llmSession = llmInferenceSession,
                                    latestResponse = "",
                                    responding = false,
                                )
                            )
                        }
                    }
                }
        }
    }

    fun sendQuery(inputPrompt: String) {
        val state = _mainState.value
        if (state !is MainState.LoadedModel) {
            throw IllegalStateException("Cannot send query without a loaded model. Handle this better in a 'real' app.")
        }
        // Clear the previous answer
        _mainState.value = state.copy(
            latestResponse = "",
            responding = true,
        )
        val llmInferenceSession = state.llmSession
        llmInferenceSession.addQueryChunk(inputPrompt)
        llmInferenceSession.generateResponseAsync { partialResult, done ->
            val currentState = _mainState.value
            if (currentState !is MainState.LoadedModel) {
                throw IllegalStateException("Cannot send query without a loaded model. Handle this better in a 'real' app.")
            }
            val response = currentState.latestResponse + partialResult
            if (done) {
                Log.d("TUT", "Full response: $response")
                _mainState.value = currentState.copy(
                    latestResponse = response,
                    responding = false,
                )
            } else {
                _mainState.value = currentState.copy(
                    latestResponse = response,
                )
            }
        }
    }
}

sealed interface MainState {
    data object Error : MainState
    data object Idle : MainState
    data object LoadingModel : MainState
    data class LoadedModel(
        val llmSession: LlmInferenceSession,
        val responding: Boolean,
        val latestResponse: String,
    ) : MainState
}
