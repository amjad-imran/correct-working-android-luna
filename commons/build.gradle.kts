import AndroidX.implementation

apply {
    from("$rootDir/android-library-build.gradle")
}

plugins {
    id("com.android.library")
    id("kotlin-android")
    kotlin("android")
    id("kotlin-kapt")
}
android {
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}




dependencies {
    "implementation"(AndroidX.navigationFragmentKtx)
    "implementation"(AndroidX.navigationUiKtx)
    "implementation"(AndroidX.multidex)
    "implementation"(XLog.log)
    "implementation"(Retrofit.retrofit)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    kapt(Room.Compiler)
    "implementation"(Room.ktx)
    "implementation"(Maps.mapUtils)
    "implementation"("net.danlew:android.joda:2.12.1")
    "implementation"(project(Modules.mpChartLib))

    "implementation"("com.github.yalantis:ucrop:2.2.6")
    "implementation"(Lottie.library)
    "implementation"(Glide.glide)
    "implementation"(project(Modules.mpChartLib))
    "implementation"(AndroidX.workRuntimeKtx)


    "implementation"(Timber.timber)
    "implementation"(Retrofit.retrofit)
    "implementation"(Retrofit.converter)
    "implementation"(Retrofit.okttp3Interceptor)

    "implementation"(Maps.mapUtils)
    "implementation"(Google.material)
    "implementation"(PlayService.playServiceAuth)
    "implementation"(PlayService.playServicePhone)
    "implementation"(PlayService.playServiceFitness)
    "implementation"(PlayService.playServiceLocation)
    "implementation"(PlayService.playServiceMaps)

    "implementation"(Fb.sdk)
    "implementation"(Firebase.analytics)
    "implementation"(Firebase.config)
    "implementation"(Firebase.crashlytics)
    "implementation"(Firebase.messagining)

    "implementation"(SeekBar.circularSeekBar)

    "implementation"(Konfetti.konfetti)
    "implementation"(Konfetti.konfettiCore)

//    //Custom Calendar for Activity
    "implementation"(platform(Firebase.bom))


}