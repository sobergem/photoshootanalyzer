package com.sobergem.photoshootanalyser.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getCameraGear(): Flow<String>
    fun getOutputFormat(): Flow<String>
    fun getCameraLens(): Flow<String>

    suspend fun saveCameraGear(gear: String)
    suspend fun saveOutputFormat(format: String)
    suspend fun saveCameraLens(lens: String)
}