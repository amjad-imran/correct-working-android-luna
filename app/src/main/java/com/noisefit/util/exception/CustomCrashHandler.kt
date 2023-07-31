package com.noisefit.util.exception

import com.noisefit_commans.utils.LOGS
import android.content.Context
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.DateFormats
import java.lang.StringBuilder


class CustomCrashHandler(
    private val context: Context?,
    private val defaultHandler: Thread.UncaughtExceptionHandler?,
    private val localDataStore: DataStoredInterface
) :
    Thread.UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread, e: Throwable) {
        try {
            //do your own thing.
            LOGS.d("MyCustomCrashHandler", "In custom crash handler try")
            saveCrashLog(e)

        } catch (e: Exception) {
            LOGS.d("MyCustomCrashHandler", "${e.message}")
            e.printStackTrace()
        } finally {
            LOGS.d("MyCustomCrashHandler", "${e.message}")
            defaultHandler?.uncaughtException(t, e)
        }
    }

    private fun saveCrashLog(e: Throwable) {
        val crashStringBuilder = StringBuilder()
        crashStringBuilder.append("Crash Log\n")
        crashStringBuilder.append("Date : ${DateFormats.getTodaysDateString(8)}\n")
        val user = localDataStore.getUser()
        if(user==null){
            crashStringBuilder.append("User Data : Not Logged In \n\n")
        }else{
            crashStringBuilder.append("User Data : $user \n\n")
        }
        val connectedDevice = localDataStore.getConnectedDevice()
        if(connectedDevice==null){
            crashStringBuilder.append("Connected Device : No Device Connected \n\n")
        }else{
            crashStringBuilder.append("Connected Device : $connectedDevice \n\n")
        }
        crashStringBuilder.append("Crash Message : ${e.message}\n\n")
        crashStringBuilder.append("Stack Trace : ${e.message}\n")
        e.stackTrace.forEach {
            crashStringBuilder.append("$it\n")
        }

        localDataStore.saveCrashLog(crashStringBuilder.toString())
    }
}