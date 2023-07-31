object Build {


    private const val protobufVersion = "0.8.8"
    const val protobufPlugin = "com.google.protobuf:protobuf-gradle-plugin:$protobufVersion"

    private const val navVersion = "2.3.5"
    const val navSafeHiltPlugin =
        "androidx.navigation:navigation-safe-args-gradle-plugin:$navVersion"

    const val daggerHiltPlugin = "com.google.dagger:hilt-android-gradle-plugin:${Hilt.hiltVersion}"

    const val kotlinGradlePlugin =
        "org.jetbrains.kotlin:kotlin-gradle-plugin:${Kotlin.kotlinStandardLibrary}"

    private const val crashlyticsGradleVersion = "2.8.1"
    const val crashlyticsGradlePlugin =
        "com.google.firebase:firebase-crashlytics-gradle:$crashlyticsGradleVersion"

    const val huaweiAgc="com.huawei.agconnect:agcp:1.6.0.300"
}