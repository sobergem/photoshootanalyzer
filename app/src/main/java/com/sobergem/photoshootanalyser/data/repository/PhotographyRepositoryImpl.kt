package com.sobergem.photoshootanalyser.data.repository

import android.graphics.BitmapFactory
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.sobergem.photoshootanalyser.di.IoDispatcher
import com.sobergem.photoshootanalyser.domain.model.PhotographyBlueprint
import com.sobergem.photoshootanalyser.domain.repository.PhotographyRepository
import com.sobergem.photoshootanalyser.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject

class PhotographyRepositoryImpl @Inject constructor(
    private val generativeModel : GenerativeModel,
    private val userPreferencesRepository: UserPreferencesRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PhotographyRepository {
    private val jsonParser = Json {ignoreUnknownKeys = true}
    override suspend fun analyzeScene(imageBytes: ByteArray): Result<PhotographyBlueprint> {
        return try {
            val cameraGear = userPreferencesRepository.getCameraGear().first()
            val outputFormat = userPreferencesRepository.getOutputFormat().first()
            val cameraLens = userPreferencesRepository.getCameraLens().first()

            val systemPrompt = """
            You are an expert photography mentor. Analyze the provided image frame. 
                The user is shooting with a $cameraGear camera, lens is $cameraLens and will process the images in $outputFormat. 
                Determine the visual style of the scene and provide the optimal manual camera settings.
                CRITICAL INSTRUCTION: You MUST return the response EXCLUSIVELY as a valid JSON object matching the exact schema below.
                {
                  "styleDetected": "string",
                  "recommendedSettings": {
                    "aperture": "string",
                    "shutterSpeed": "string",
                    "iso": "string"
                  },
                  "stylistTips": ["string"]
                }
        """.trimIndent()

            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

            val inputContent = content {
                image(bitmap)
                text(systemPrompt)
            }

            val response = generativeModel.generateContent(inputContent)
            val jsonString = response.text ?: throw Exception("Gemini returned an empty response")

            val cleanJson = jsonString.removePrefix("'''json").removeSuffix("'''")

            val blueprint = jsonParser.decodeFromString<PhotographyBlueprint>(cleanJson)
            Result.success(blueprint)
        }catch (e : Exception){
            Result.failure(e)
        }
    }
}