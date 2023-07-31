package com.noisefit_commans.interfaces.base

import android.content.Context

abstract class BaseInitializeInterface {
    abstract fun initSdk()
    abstract fun unInitSdk()
    abstract fun <T> callbackListener(callback: T)
    abstract fun removeCallback()
}