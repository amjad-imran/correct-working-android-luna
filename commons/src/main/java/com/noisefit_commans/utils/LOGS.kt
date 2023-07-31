package com.noisefit_commans.utils

import com.google.gson.Gson
import timber.log.Timber

object LOGS : Timber.DebugTree() {

    private val gson = Gson()

    private val DEFAULT_TAG = "NOISE_APP_LOGS"

    fun e(error: String) {
        Timber.e(error)
    }

    fun e(tag: String, log: String) {
        Timber.tag(tag).e(log)
    }

    fun t(e: Throwable) {
        Timber.e(e)
    }

    fun d(log: String) {
        Timber.d(log)
    }

    fun d(tag: String, log: String) {
        Timber.tag(tag).d(log)
    }

    fun <T> d(logClass: T) {
        Timber.tag(DEFAULT_TAG).d(gson.toJson(logClass))
    }

    fun i(log: String) {
        Timber.i(log)
    }

    fun i(tag: String, log: String) {
        Timber.tag(tag).i(log)
    }

    fun v(log: String) {
        Timber.v(log)
    }

    fun w(log: String) {
        Timber.w(log)
    }

    fun w(tag: String, log: String) {
        Timber.tag(tag).w(log)
    }


}