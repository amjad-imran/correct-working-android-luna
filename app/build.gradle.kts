import AndroidX.implementation

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-parcelize")
    id("com.huawei.agconnect")

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
        testInstrumentationRunner = AndroidXTest.instrumentationRunner
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = Java.java
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
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
            buildConfigField("String", "STEPS_URL", "\"https://uat-step.gonoise.com\"")
            buildConfigField("String", "TEMPERATURE_URL", "\"https://uat-temp.gonoise.com\"")
            buildConfigField("String", "SHOP_API_URL", "\"https://stage-pre-order.gonoise.in\"")
            buildConfigField("String", "API_URL_WEATHER", "\"http://api.openweathermap.org\"")
            buildConfigField("String", "API_URL_STOCK", "\"https://api.twelvedata.com\"")
            buildConfigField("String", "BASE_URL_NOISE_CONNECT", "\"https://noise-connect-stage.gonoise.in\"")
            buildConfigField("String", "NOISE_CONNECT_KEY", "\"d8f021fd65e25982003ddce3cac2225e\"")
            buildConfigField("String", "INSIDER_PARTNER", "\"gonoiselunaapp\"")

            //Oreo URLs
            buildConfigField("String", "OREO_BASE_URL", "\"https://uat-app.gonoise.com/luna\"")


            versionNameSuffix = ".uat"
            manifestPlaceholders["crashlyticsCollectionEnabled"] = "true"
            manifestPlaceholders["partner"] = "gonoiselunaapp"
        }
        create("staging") {
            buildConfigField("String", "BASE_URL", "\"https://app-micro-staging.gonoise.com\"")
            buildConfigField("String", "BASE_URL_NEW", "\"https://stage-app.gonoise.com\"")
            buildConfigField("String", "STEPS_URL", "\"https://stage-step.gonoise.com\"")
            buildConfigField("String", "TEMPERATURE_URL", "\"https://stage-temp.gonoise.com\"")
            buildConfigField("String", "SHOP_API_URL", "\"https://stage-pre-order.gonoise.in\"")
            buildConfigField("String", "API_URL_WEATHER", "\"http://api.openweathermap.org\"")
            buildConfigField("String", "API_URL_STOCK", "\"https://api.twelvedata.com\"")
            buildConfigField("String", "BASE_URL_NOISE_CONNECT", "\"https://noise-connect-stage.gonoise.in\"")
            buildConfigField("String", "NOISE_CONNECT_KEY", "\"d8f021fd65e25982003ddce3cac2225e\"")
            buildConfigField("String", "INSIDER_PARTNER", "\"gonoiselunaapp\"")

            //Oreo URLs
            buildConfigField("String", "OREO_BASE_URL", "\"https://stage-app.gonoise.com/luna\"")


            versionNameSuffix = ".staging.luna"
            manifestPlaceholders["crashlyticsCollectionEnabled"] = "false"
            manifestPlaceholders["partner"] = "gonoiselunaapp"

        }
        create("live") {
            buildConfigField("String", "STEPS_URL", "\"https://step-activities.gonoise.com\"")
            buildConfigField("String", "BASE_URL_NEW", "\"https://app.gonoise.com\"")
            buildConfigField("String", "BASE_URL", "\"https://backend.gonoise.com\"")
            buildConfigField("String", "TEMPERATURE_URL", "\"https://temp.gonoise.com\"")
            buildConfigField("String", "SHOP_API_URL", "\"https://pre-order.gonoise.com\"")
            buildConfigField("String", "API_URL_WEATHER", "\"http://api.openweathermap.org\"")
            buildConfigField("String", "API_URL_STOCK", "\"https://api.twelvedata.com\"")
            buildConfigField("String", "INSIDER_PARTNER", "\"gonoiselunaapp\"")
            buildConfigField("String", "NOISE_CONNECT_KEY", "\"d8f021fd65e25982003ddce3cac2225e\"")
            buildConfigField("String", "BASE_URL_NOISE_CONNECT", "\"https://noise-connect.gonoise.com\"")

            //Oreo URLs
            buildConfigField("String", "OREO_BASE_URL", "\"https://app.gonoise.com/luna\"")


            manifestPlaceholders["crashlyticsCollectionEnabled"] = "true"
            manifestPlaceholders["partner"] = "gonoiselunaapp"
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

    implementation(AndroidX.libraries)

    implementation(CalenderView.calendarView)
//    implementation(CleverTap.sdk)

    implementation(Exoplayer.core)
    implementation(Exoplayer.dash)
    implementation(Exoplayer.ui)

    implementation(Fb.sdk)
    implementation(Firebase.analytics)
    implementation(Firebase.config)
    implementation(Firebase.crashlytics)
    implementation(Firebase.messagining)


//    //Custom Calendar for Activity
    implementation(platform(Firebase.bom))
    implementation("androidx.paging:paging-common-ktx:3.1.1")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.2")
    implementation("com.github.grapesnberries:curlloggerinterceptor:0.1")
    implementation("com.leinardi.android:speed-dial:3.3.0")


    implementation(Glide.glide)
    implementation("com.github.yalantis:ucrop:2.2.6")

    implementation(Google.gson)
    implementation(Google.playCore)
    implementation(Google.material)
    implementation(Google.collapsingToolbarLayout)

    implementation(PinView.PinView)

    implementation(Hilt.android)
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.intuit.sdp:sdp-android:1.1.0")
    implementation("androidx.test:core-ktx:1.4.0")

    implementation("com.google.android.gms:play-services-fitness:21.1.0")


    kapt(Hilt.compiler)
    kapt(Hilt.hiltCompiler)

    implementation(Kotlinx.androidCore)
    implementation(Kotlinx.coroutinesCore)

//    implementation(InstallReferral.installReferrer)

    implementation(PlayService.playServiceAuth)
    implementation(PlayService.playServicePhone)
    implementation(PlayService.playServiceFitness)
    implementation(PlayService.playServiceLocation)
    implementation(PlayService.playServiceMaps)

    implementation(Lottie.library)

    implementation(SwipeRefresh.swipeRefreshLibraryCore)

    implementation(Timber.timber)
    implementation(Retrofit.retrofit)
    implementation(Retrofit.converter)
    implementation(Retrofit.okttp3Interceptor)

    /*implementation(Retrofit.brcypt)*/

    kapt(Room.Compiler)
    implementation(Room.ktx)
    implementation(RxPermissions.rxPermission)
    implementation(project(Modules.commons))
//    implementation(project(Modules.oreo))
    implementation(project(Modules.mpChartLib))
    implementation(project(Modules.customProgress))
    implementation(WatchDog.watchDog)
    implementation(project(Modules.noisefit_zh_sdk))

    implementation(AndroidX.lifecycleProcess)
    implementation(Insider.plugin)
    implementation(PlayService.playServiceAdmob)

    implementation(Insider.push)
    implementation(Insider.identifier)
    implementation(Insider.location)
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("net.danlew:android.joda:2.12.1")

    implementation("com.github.Dimezis:BlurView:version-2.0.3")

    implementation("com.github.bmarrdev:android-DecoView-charting:v1.2")
    implementation("com.github.alirezat775:carousel-view:1.1.1")

    testImplementation("org.mockito:mockito-core:3.10.0")
    androidTestImplementation("org.mockito:mockito-android:3.10.0")
    /*implementation(Test.testCore)*/
    testImplementation(Test.junit)
    testImplementation(Test.googleTruth)
    testImplementation(Test.androidXArchCore)
    testImplementation(Test.textCoroutines)
    kaptTest(Test.hiltCompiler)
    kaptTest(Test.androidxHiltCompiler)
    kaptAndroidTest(Test.hiltCompiler)
    kaptAndroidTest(Test.androidxHiltCompiler)

    androidTestImplementation(InstrumentTest.androidJUnit)
    androidTestImplementation(InstrumentTest.truth)
    androidTestImplementation(InstrumentTest.jUnit)
    androidTestImplementation(InstrumentTest.archCore)
    androidTestImplementation(InstrumentTest.hiltTesting)
    implementation(Test.androidxTestRunner)
    //moengage
    implementation(moengage.core)
    implementation(moengage.inapp)
    implementation(moengage.pushAmpPlus)
    implementation(moengage.pushKit)
    implementation(moengage.pushAmp)
    implementation(moengage.geofence)
    implementation(moengage.inboxCore)


}