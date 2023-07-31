

apply {
    from("$rootDir/android-library-build.gradle")
//    sourceSets {
//        main {
//            jniLibs.srcDirs = ['libs']
//        }
//    }
//
//    java.sourceSets["main"].java {
//        srcDir("src/gen/java")
//    }

}


//android {
//    sourceSets {
//        main {
//            jniLibs.srcDirs = ['libs']
//        }
//    }
//}

container {

}
dependencies {

    "implementation"(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    "implementation"(project(Modules.commons))
    "implementation"(GreenRobot.greendao)
    "api"(GreenRobot.eventBus)
    "implementation"(Apache.apache)
    "implementation"(files("libs/IDoBLELib-VeryFit-2.67.31.jar"))
    "implementation"(project(Modules.noisefit_libraries, configuration = Modules.colorfit2))
}



