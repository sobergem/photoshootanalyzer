package com.sobergem.photoshootanalyser.domain.repository

import com.sobergem.photoshootanalyser.domain.model.PhotographyBlueprint

/**
 * The architectural contract for managing photography analysis data operations.
 * The domain layer owns this interface, dictating the rules to whoever implements it.
 */

interface PhotographyRepository {
    /**
     * Captures and analyzes a raw frame to return a photography blueprint.
     *
     * @param imageBytes The raw frame data captured from the camera preview.
     * @return A [Result] wrapping the successful [PhotographyBlueprint] or a failure exception.
     */
    suspend fun analyzeScene(imageBytes: ByteArray): Result<PhotographyBlueprint>
}