package com.sobergem.photoshootanalyser.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sobergem.photoshootanalyser.domain.usecase.AnalyzeSceneUseCase
import com.sobergem.photoshootanalyser.presentation.screen.uistate.PhotographyUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotographyViewModel @Inject constructor(
    private val analyzeSceneUseCase: AnalyzeSceneUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<PhotographyUiState>(PhotographyUiState.Idle)
    val uiState : StateFlow<PhotographyUiState> = _uiState.asStateFlow()

    private var lastImageBytes: ByteArray? = null

    fun analyzeFrame(imageBytes: ByteArray){
        lastImageBytes = imageBytes
        viewModelScope.launch {
            _uiState.update { PhotographyUiState.Loading }

            analyzeSceneUseCase(imageBytes).onSuccess{ blueprint ->
                _uiState.update { PhotographyUiState.Success(blueprint) }
            }.onFailure { error ->
                _uiState.update { PhotographyUiState.Error(error.message ?: "Unknown error") }
            }
        }
    }

    fun reRunAnalysis(){
        lastImageBytes?.let{ bytes ->
            analyzeFrame(bytes)
        }
    }
    fun resetToIdle(){
        lastImageBytes = null
        _uiState.update { PhotographyUiState.Idle }
    }
}