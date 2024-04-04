// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {

    buildscript {
        dependencies {
            classpath(Build.protobufPlugin)
            classpath(Build.navSafeHiltPlugin)
            classpath(PlayService.googleServices)
            classpath(Build.crashlyticsGradlePlugin)
            classpath(Build.daggerHiltPlugin)
//            classpath(Build.huaweiAgc)
//            classpath(Insider.agcpClassPath)
            classpath("com.android.tools.build:gradle:7.3.1")
//            classpath Build.daggerHiltPlugin
//            classpath Build.kotlinGradlePlugin
        //    classpath("org.junit.platform:junit-platform-gradle-plugin:1.2.0")
        }
    }

}
plugins {
    id("com.android.application") version "7.4.1" apply false
    id("com.android.library") version "7.4.1" apply false
    id("org.jetbrains.kotlin.android") version "1.7.20" apply false
    id("org.jetbrains.kotlin.jvm") version "1.7.20" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}