package com.oreo.di

import android.content.Context
import com.oreo.util.SpeechRecognizerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun providesSpeechRecognizerManager(@ApplicationContext appContext: Context): SpeechRecognizerManager {
        return SpeechRecognizerManager(appContext)
    }

}