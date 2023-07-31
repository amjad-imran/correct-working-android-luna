apply {
    from("$rootDir/android-library-build.gradle")
}



dependencies {

    "implementation"(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    "implementation"(project(Modules.commons))
    "implementation"(Rxjava3.rxAndroid)
    "implementation"(Rxjava3.rxKotlin)
}


