apply {
    from("$rootDir/android-library-build.gradle")
}



dependencies {

//    "implementation"(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    "implementation"(project(Modules.commons))
    "implementation"(Protobuf.proto)
    "implementation"(project(Modules.noisefit_libraries, configuration = Modules.evolve2))
}
