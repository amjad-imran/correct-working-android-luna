import org.gradle.api.artifacts.dsl.DependencyHandler

object AndroidX {
    private const val coreKtxVersion = "1.7.0"
    const val coreKtx = "androidx.core:core-ktx:$coreKtxVersion"

    private const val appCompatVersion = "1.3.0"
    const val appCompat = "androidx.appcompat:appcompat:$appCompatVersion"

    private const val lifecycleVmKtxVersion = "2.6.1"
    const val lifecycleVmKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVmKtxVersion"
    const val livedataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVmKtxVersion"

    private const val multidexVersion = "2.0.1"
    const val multidex = "androidx.multidex:multidex:$multidexVersion"

    private const val localBroadcastManagerVersion = "1.1.0"
    const val localBroadcastManager ="androidx.localbroadcastmanager:localbroadcastmanager:$localBroadcastManagerVersion"

    private const val vectorDrawableVersion = "1.1.0"
    const val vectorDrawable = "androidx.vectordrawable:vectordrawable:$vectorDrawableVersion"

    private const val navigationKtxVersion = "2.4.2"
    const val navigationFragmentKtx =
        "androidx.navigation:navigation-fragment-ktx:$navigationKtxVersion"
    const val navigationUiKtx = "androidx.navigation:navigation-ui-ktx:$navigationKtxVersion"


    private const val lifecycleExtensionVersion = "2.2.0"
    const val lifecycleExtension =
        "androidx.lifecycle:lifecycle-extensions:$lifecycleExtensionVersion"

    private const val hiltWorkVersion = "1.0.0"
    const val hiltWork = "androidx.hilt:hilt-work:$hiltWorkVersion"

    private const val workRuntimeKtxVersion = "2.7.1"
    const val workRuntimeKtx = "androidx.work:work-runtime-ktx:$workRuntimeKtxVersion"

    private const val concurrentFuturesKtxVersion = "1.1.0"
    const val concurrentFuturesKtx =
        "androidx.concurrent:concurrent-futures-ktx:$concurrentFuturesKtxVersion"


    private const val hiltViewModelVersion = "1.0.0-alpha03"
    const val hiltLifecycleViewModel =
        "androidx.hilt:hilt-lifecycle-viewmodel:$hiltViewModelVersion"

    private const val fragmentKtxVersion = "1.5.6"
    const val fragmentKtx = "androidx.fragment:fragment-ktx:$fragmentKtxVersion"

    private const val constraintLayoutVersion = "2.1.3"
    const val constraintLayout =
        "androidx.constraintlayout:constraintlayout:$constraintLayoutVersion"

    private const val viewpager2Version = "1.0.0"
    const val viewPager = "androidx.viewpager2:viewpager2:$viewpager2Version"

    private const val legacySupportVersion = "1.0.0"
    const val legacySupport = "androidx.legacy:legacy-support-v4:$legacySupportVersion"
    val lifecycleProcess="androidx.lifecycle:lifecycle-process:2.5.0"

    val pagination = "androidx.paging:paging-runtime:3.0.1"

    val libraries = arrayListOf<String>().apply {
        add(coreKtx)
        add(concurrentFuturesKtx)
        add(constraintLayout)
        add(fragmentKtx)
        add(pagination)
        //add(hiltLifecycleViewModel)
        add(hiltWork)
        add(legacySupport)
        add(lifecycleExtension)
        add(lifecycleVmKtx)
        add(vectorDrawable)
        add(navigationUiKtx)
        add(livedataKtx)
        add(multidex)
        add(navigationFragmentKtx)
        add(viewPager)
        add(workRuntimeKtx)
    }

    fun DependencyHandler.implementation(list: List<String>) {
        list.forEach { dependency ->
            add("implementation", dependency)
        }
    }
}

object AndroidXTest {
    private const val version = "1.3.0"
    const val runner = "androidx.test:runner:$version"


    private const val espressoCoreVersion = "3.4.0"
    const val espressoCore = "androidx.test.espresso:espresso-core:$espressoCoreVersion"


    private const val androidxTestExtVersion = "1.1.3"
    const val androidxTestExt = "androidx.test.ext:junit:$androidxTestExtVersion"

    const val instrumentationRunner = "com.noisefit.HiltTestRunner"

}