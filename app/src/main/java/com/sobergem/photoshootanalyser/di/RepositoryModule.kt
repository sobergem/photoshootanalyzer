package com.sobergem.photoshootanalyser.di

import com.sobergem.photoshootanalyser.data.repository.PhotographyRepositoryImpl
import com.sobergem.photoshootanalyser.data.repository.UserPreferencesRepositoryImpl
import com.sobergem.photoshootanalyser.domain.repository.PhotographyRepository
import com.sobergem.photoshootanalyser.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule{
    @Binds
    abstract fun bindPhotographyRepository(
        photographyRepositoryImpl: PhotographyRepositoryImpl
    ) : PhotographyRepository

    @Binds
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl
    ) : UserPreferencesRepository
}