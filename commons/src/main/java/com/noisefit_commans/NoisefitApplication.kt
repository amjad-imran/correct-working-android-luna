package com.noisefit_commans


import android.app.Application
import androidx.multidex.MultiDexApplication
import com.google.gson.Gson
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.FileLogsUtils
import timber.log.Timber
import javax.inject.Inject


open class NoisefitApplication : MultiDexApplication() {

    @Inject
    lateinit var gson: Gson

    companion object {
        var context: Application? = null
        var localDataStore: DataStoredInterface? = null
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        if (BuildConfig.DEBUG) {
            Timber.plant(LOGS)
        }


    }


}