
buildscript {

    dependencies {
        classpath(libs.googleProtobufGradlePlugin)
        classpath(libs.androidxNavigationSafeArgsGradlePlugin)
        classpath(libs.googleDaggerHiltAndroidGradlePlugin)
        classpath(libs.googleFirebaseCrashlyticsGradle)
        classpath(libs.google.service)
        classpath(libs.gradle)
    }

}
plugins {
    alias(libs.plugins.androidApplication) apply false
    id("com.android.library") version "8.11.0" apply false
    id("org.jetbrains.kotlin.jvm") version "2.2.0" apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
}


tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}