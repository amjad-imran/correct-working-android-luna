apply {
    from("$rootDir/android-library-build.gradle")
}



dependencies {

//    "implementation"(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    "implementation"(project(Modules.commons))
    "implementation"(TextDrawable.textDrawable)
    "implementation"(AndroidX.concurrentFuturesKtx)
    "implementation"(project(Modules.noisefit_libraries, configuration = Modules.colorfit_pro))
}


