package com.noisefit.di

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.noisefit.data.googleFit.GoogleFitDataObservers
import com.noisefit.luna.BuildConfig
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Singleton
    @Provides
    fun provideGoogleSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
            .build()
    }

    @Singleton
    @Provides
    fun provideGoogleSignInClient(
        @ApplicationContext context: Context,
        googleSignInOptions: GoogleSignInOptions
    ): GoogleSignInClient {
        return GoogleSignIn.getClient(context, googleSignInOptions)
    }

    @Singleton
    @Provides
    fun provideGoogleFitDataObservers(
        @ApplicationContext context: Context,
        googleSignInAccount: GoogleSignInAccount,
        localDataStore: DataStoredInterface
    ): GoogleFitDataObservers {
        return GoogleFitDataObservers(context, googleSignInAccount, localDataStore)
    }

//    @Singleton
//    @Provides
//    fun provideGoogleFit(@ApplicationContext context: Context): GoogleFitHandler {
//        return GoogleFitHandler(context)
//    }

    @Singleton
    @Provides
    fun provideGoogleAccountExtension(
        @ApplicationContext appContext: Context,
        fitnessOptions: FitnessOptions
    ): GoogleSignInAccount {
        return GoogleSignIn.getAccountForExtension(appContext, fitnessOptions)
    }

    @Singleton
    @Provides
    fun fitnessOptions(): FitnessOptions {
        return FitnessOptions.builder()
            .accessSleepSessions(FitnessOptions.ACCESS_WRITE)
            .accessActivitySessions(FitnessOptions.ACCESS_WRITE)
            .accessActivitySessions(FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_WORKOUT_EXERCISE, FitnessOptions.ACCESS_WRITE)
            .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_WRITE)
            .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_WRITE)
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
            .addDataType(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_WRITE)
            .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_WRITE)
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_WRITE)
            .build()

    }
}