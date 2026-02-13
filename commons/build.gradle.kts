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
    namespace = "com.noisefit_commans"
}




dependencies {

    implementation(libs.androidx.multidex)
    implementation(libs.elvishew.xlog)
    implementation(libs.retrofit)
    implementation(libs.androidx.navigation.fragment.ktx)
    kapt(libs.room.compiler)
    implementation(libs.room.ktx)
    implementation(libs.maps.utils)



    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.android.joda)
    implementation(project(Modules.mpChartLib))
    implementation(libs.ucrop)
    implementation(libs.lottie)
    implementation(libs.glide)

    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.timber)
    implementation(libs.retrofit)
    implementation(libs.retrofit.logging.interceptor)
    implementation(libs.retrofit.converter.gson)

    implementation(libs.maps.utils)
    implementation(libs.android.material)

    implementation(libs.play.service.auth)
    implementation(libs.play.service.auth.api)
    implementation(libs.play.service.fitness)
    implementation(libs.play.service.location)
    implementation(libs.play.service.maps)


    implementation(libs.facebook.android.sdk)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.config.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.messaging.ktx)
    implementation(platform(libs.firebase.bom))

    implementation(libs.tankery.circularSeekBar)

    implementation(libs.konfetti.xml)
    implementation(libs.konfetti.core)

    implementation(libs.mixpanel.android)


}