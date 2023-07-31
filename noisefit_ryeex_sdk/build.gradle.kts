apply {
    from("$rootDir/android-library-build.gradle")
}



dependencies {

    "implementation"(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    "implementation"(project(Modules.commons))
    "implementation"(Protobuf.proto)
    /*"implementation"(project(Modules.noisefit_libraries, configuration = Modules.ryeex_ble))
    "implementation"(project(Modules.noisefit_libraries, configuration = Modules.ryeex_common))
    "implementation"(project(Modules.noisefit_libraries, configuration = Modules.ryeex_watch))*/
}
