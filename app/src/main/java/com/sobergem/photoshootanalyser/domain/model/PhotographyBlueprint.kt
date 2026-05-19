package com.sobergem.photoshootanalyser.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PhotographyBlueprint(
    val styleDetected: String,
    val recommendedSettings: CameraSettings,
    val stylistTips: List<String>
)

@Serializable
data class CameraSettings(
    val aperture: String,
    val shutterSpeed: String,
    val iso: String
)
