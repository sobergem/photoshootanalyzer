package com.sobergem.photoshootanalyser.domain.usecase

import com.sobergem.photoshootanalyser.domain.model.PhotographyBlueprint
import com.sobergem.photoshootanalyser.domain.repository.PhotographyRepository
import javax.inject.Inject

class AnalyzeSceneUseCase @Inject constructor(
    private val photographyRepository: PhotographyRepository
) {
    /**
     * Executes the scene analysis operation.
     *
     * By utilizing the 'invoke' operator, this class can be called directly
     * like a function (e.g., analyzeSceneUseCase(bytes)).
     *
     * @param imageBytes The raw image frame captured from CameraX.
     * @return A [Result] containing the [PhotographyBlueprint] or an error.
     */
    suspend operator fun invoke(imageBytes: ByteArray): Result<PhotographyBlueprint>{
        if(imageBytes.isEmpty()){
            return Result.failure(IllegalArgumentException("Captured frame data is completely empty."))
        }
        return photographyRepository.analyzeScene(imageBytes)
    }
}