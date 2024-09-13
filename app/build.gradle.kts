

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.compose.compiler)

    kotlin("android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-parcelize")
//    id("com.huawei.agconnect")

}


android {
    compileSdk = Android.compileSdk
    ndkVersion = Android.ndk

    defaultConfig {
        applicationId = Android.appId
        minSdk = Android.minSdk
        targetSdk = Android.targetSdk
        versionCode = Android.versionCode
        versionName = Android.versionName

        multiDexEnabled = true
        vectorDrawables {
            useSupportLibrary = true
        }
        // testInstrumentationRunner = AndroidXTest.instrumentationRunner

    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
        compose = true
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("release") {
            storeFile = file("luna.keystore")
            storePassword = "luna@noise"
            keyAlias = "luna@noise"
            keyPassword = "luna@noise"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            ndk {
                debugSymbolLevel = "FULL"
            }
            //isDebuggable = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
//            buildConfigField("String", "CLEVERTAP_ACCOUNT_ID", "\"694-RRK-595Z\"")
//            buildConfigField("String", "CLEVERTAP_TOKEN", "\"322-4c6\"")


            buildConfigField(
                "String",
                "GOOGLE_CLIENT_ID",
                "\"159998142649-h616h07i88lilgc45cv1rbengmv07ias.apps.googleusercontent.com\""
            )
//            manifestPlaceholders["cleverTapToken"] = "322-4c6"
//            manifestPlaceholders["cleverTapId"] = "694-RRK-595Z"
            manifestPlaceholders["google_client_id"] = "AIzaSyD-zRQCQPly-EA4jawx6YZRLMprV4bwf9w"

            signingConfig = signingConfigs.getByName("release")
//            isDebuggable = true
            //versionNameSuffix ".release"
        }
        getByName("debug") {
            /*isMinifyEnabled = true
             isShrinkResources = true
           isDebuggable = false
           proguardFiles(
               getDefaultProguardFile("proguard-android-optimize.txt"),
               "proguard-rules.pro"
           )*/
//            buildConfigField("String", "CLEVERTAP_ACCOUNT_ID", "\"TEST-794-RRK-595Z98\"")
//            buildConfigField("String", "CLEVERTAP_TOKEN", "\"TEST-322-4ca89\"")


            buildConfigField(
                "String",
                "GOOGLE_CLIENT_ID",
                "\"159998142649-h616h07i88lilgc45cv1rbengmv07ias.apps.googleusercontent.com\""
            )
//            manifestPlaceholders["cleverTapToken"] = "322-4c690"
//            manifestPlaceholders["cleverTapId"] = "694-RRK-595Z07087"
            manifestPlaceholders["google_client_id"] = "AIzaSyD-zRQCQPly-EA4jawx6YZRLMprV4bwf9w"

            signingConfig = signingConfigs.getByName("debug")

            applicationIdSuffix = ".dev"

        }
    }

    flavorDimensions.add("default")

    productFlavors {
        create("uat") {
            buildConfigField("String", "BASE_URL", "\"https://app-micro-uat.gonoise.com\"")
            buildConfigField("String", "BASE_URL_NEW", "\"https://uat-app.gonoise.com\"")
            buildConfigField("String", "API_URL_WEATHER", "\"http://api.openweathermap.org\"")

            //Oreo URLs
            buildConfigField("String", "OREO_BASE_URL", "\"https://uat-app.gonoise.com/luna\"")

            versionNameSuffix = ".uat"
            manifestPlaceholders["crashlyticsCollectionEnabled"] = "true"
        }
        create("staging") {
            buildConfigField("String", "BASE_URL", "\"https://app-micro-staging.gonoise.com\"")
            buildConfigField("String", "BASE_URL_NEW", "\"https://stage-app.gonoise.com\"")
            buildConfigField("String", "API_URL_WEATHER", "\"http://api.openweathermap.org\"")

            //Oreo URLs
            buildConfigField("String", "OREO_BASE_URL", "\"https://stage-app.gonoise.com/luna\"")

            versionNameSuffix = ".staging.luna"
            manifestPlaceholders["crashlyticsCollectionEnabled"] = "false"

        }
        create("live") {
            buildConfigField("String", "BASE_URL", "\"https://backend.gonoise.com\"")
            buildConfigField("String", "BASE_URL_NEW", "\"https://app.gonoise.com\"")
            buildConfigField("String", "API_URL_WEATHER", "\"http://api.openweathermap.org\"")

            //Oreo URLs
            buildConfigField("String", "OREO_BASE_URL", "\"https://app.gonoise.com/luna\"")

            manifestPlaceholders["crashlyticsCollectionEnabled"] = "true"
        }


    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDir("libs")
        }
        getByName("test") {
            java.srcDir("src/test/res")
        }
    }
    namespace = "com.noisefit.luna"

    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val outputFileName =
                    "Luna_${variant.versionName}_${variant.versionCode}.apk"
                output.outputFileName = outputFileName
            }
    }


    //Make sure you add this in your gradle inside android {}

//    testOptions{
//        unitTests.all {
//            useJUnitPlatform()
//        }
//    }


}
//tasks.register("test", Test::class) {
//    useJUnitPlatform()
//}
////tasks.test {
////    useJUnitPlatform()
////    testLogging {
////        events("passed", "skipped", "failed")
////    }
////}
//
//tasks.withType<Test> {
//    useJUnitPlatform()
//}
dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.multidex)
    implementation(libs.androidx.localbroadcastmanager)
    implementation(libs.androidx.vectordrawable)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.camera)
    implementation(libs.androidx.camera.lifestyle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.androidx.concurrent.futures.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.legacy.support)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.paging.runtime)

    implementation(libs.mindinventory.shimmertextview)


//    implementation(CalenderView.calendarView)
    implementation(libs.calendarView)
//    implementation(CleverTap.sdk)

    implementation(libs.exoplayer.core)
    implementation(libs.exoplayer.dash)
    implementation(libs.exoplayer.ui)

    implementation(libs.facebook.android.sdk)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.config.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.messaging.ktx)
    implementation(platform(libs.firebase.bom))

    implementation(libs.androidx.paging.common.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.curlloggerinterceptor)

    implementation(libs.core)
    //implementation("io.noties.markwon:image:4.6.2")

    implementation(libs.glide)
    implementation(libs.ucrop)
    implementation(libs.gson)
    //  implementation(libs.play.core.ktx)
    implementation(libs.android.material)
    implementation(libs.material.collapsingtoolbarlayout)
    implementation(libs.review.ktx)
//    implementation(Google.playCore)


    implementation(libs.goodiebag.pinview)

    implementation(libs.dagger.hilt.android)
    implementation(libs.androidx.legacy.support)
    implementation(libs.androidx.appcompat)
    implementation(libs.material.v161)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.sdp.android)



    implementation(libs.oksse)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    kapt(libs.android.material)
    kapt(libs.dagger.hilt.compiler)
    kapt(libs.hilt.compiler)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)


//    implementation(InstallReferral.installReferrer)
    implementation(libs.play.service.auth)
    implementation(libs.play.service.auth.api)
    implementation(libs.play.service.fitness)
    implementation(libs.play.service.location)
    implementation(libs.play.service.maps)


    implementation(libs.lottie)
    implementation(libs.swipe.refresh.library.core)

    implementation(libs.timber)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.logging.interceptor)

    /*implementation(Retrofit.brcypt)*/

    kapt(libs.room.compiler)
    implementation(libs.room.ktx)

    implementation(libs.tbruyelle.rxpermissions)
    implementation(project(Modules.commons))
//    implementation(project(Modules.oreo))
    implementation(project(Modules.mpChartLib))
    implementation(project(Modules.noisefit_zh_sdk))

    implementation(libs.anrwatchdog)

    implementation(platform(libs.kotlin.bom))

    implementation(libs.androidx.lifecycle.process)



    implementation(libs.flexbox)
    implementation(libs.android.joda)

    implementation(libs.blurview)

    implementation(libs.android.decoview.charting)
    implementation(libs.carousel.view)



    //moengage
    implementation(moengage.core)
    implementation(moengage.inapp)
    implementation(moengage.pushAmpPlus)
    implementation(moengage.pushKit)
    implementation(moengage.pushAmp)
    implementation(moengage.geofence)
    implementation(moengage.inboxCore)

}