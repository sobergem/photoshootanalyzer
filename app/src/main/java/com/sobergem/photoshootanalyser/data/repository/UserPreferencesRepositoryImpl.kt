package com.sobergem.photoshootanalyser.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sobergem.photoshootanalyser.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore : DataStore<Preferences>
) : UserPreferencesRepository {
    private companion object {
        val KEY_CAMERA_GEAR = stringPreferencesKey("camera_gear")
        val KEY_OUTPUT_FORMAT = stringPreferencesKey("output_format")
        val KEY_CAMERA_LENS = stringPreferencesKey("camera_lens")
    }
    override fun getCameraGear(): Flow<String>  = dataStore.data
        .catch { exception ->
            if(exception is IOException) {
                emit(emptyPreferences())
            }else{
                throw exception
            }
        }
        .map{ preferences ->
            preferences[KEY_CAMERA_GEAR]?: "Nikon D5600"
        }


    override fun getOutputFormat(): Flow<String>  = dataStore.data
        .catch { exception ->
            if(exception is IOException) {
                emit(emptyPreferences())
            }else{
                throw exception
            }
        }
        .map{ preferences ->
            preferences[KEY_OUTPUT_FORMAT]?: "RAW"
        }

    override fun getCameraLens(): Flow<String>  = dataStore.data
        .catch { exception ->
            if(exception is IOException) {
                emit(emptyPreferences())
            }else{
                throw exception
            }
        }
        .map{ preferences ->
            preferences[KEY_CAMERA_LENS]?: "AF-P DX NIKKOR 18-55mm f/3.5-5.6G VR"
        }

    override suspend fun saveCameraGear(gear: String) {
        dataStore.edit { preferences ->
            preferences[KEY_CAMERA_GEAR] = gear
        }
    }

    override suspend fun saveOutputFormat(format: String) {
        dataStore.edit { preferences ->
            preferences[KEY_OUTPUT_FORMAT] = format
        }
    }

    override suspend fun saveCameraLens(lens: String) {
        dataStore.edit { preferences ->
            preferences[KEY_CAMERA_LENS] = lens
        }
    }

}