import org.gradle.api.artifacts.dsl.DependencyHandler

object Test {

    private const val junitJupiterVersion = "5.9.1"
    private const val junit4Version = "4.13.2"
    private const val googleTruthV = "1.0.1"
    private const val testCoreV = "1.2.0"
    private const val hamcrestV = "1.3"
    private const val androidXArchCoreV = "2.1.0"
    private const val roboElectricV = "4.3.1"
    private const val textCoroutinesV = "1.2.1"
    private const val mockitoV = "2.21.0"
    private const val mockTest = "1.9.2"
    const val junit = "junit:junit:$junit4Version"
    const val googleTruth = "com.google.truth:truth:$googleTruthV"
    const val hamcrest = "org.hamcrest:hamcrest-all:$hamcrestV"
    const val androidXArchCore = "androidx.arch.core:core-testing:$androidXArchCoreV"
    const val roboElectric = "org.robolectric:robolectric:$roboElectricV"
    const val textCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$textCoroutinesV"
    const val mockito = "org.mockito:mockito-core:$mockitoV"
    const val testCore = "androidx.test:core:$testCoreV"

    const val hiltCompiler = "com.google.dagger:hilt-android-compiler:2.38.1"
    const val androidxHiltCompiler = "androidx.hilt:hilt-compiler:1.0.0"

    const val androidxTestRunner = "androidx.test:runner:1.4.0"


    val mockk = "io.mockk:mockk:$mockTest"
    val jupiterApi = "org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion"
    val jupiterParams = "org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion"
    val jupiterEngine = "org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion"
//    val mockk = "io.mockk:mockk:${Versions.mockk_version}"


}