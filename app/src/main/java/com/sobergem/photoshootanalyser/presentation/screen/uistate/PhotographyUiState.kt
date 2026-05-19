package com.sobergem.photoshootanalyser.presentation.screen.uistate

import com.sobergem.photoshootanalyser.domain.model.PhotographyBlueprint

sealed interface PhotographyUiState {
    data object Idle: PhotographyUiState
    data object Loading: PhotographyUiState
    data class Success(val blueprint: PhotographyBlueprint): PhotographyUiState
    data class Error(val message: String): PhotographyUiState
}