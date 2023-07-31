package com.noisefit_commans.interfaces.base

abstract class BaseActions {
    open fun init() {}
    open fun attachCallbacks() {}
    open fun removeCallbacks() {}
    abstract fun <T> callbackListener(callback: T)
    abstract fun <T> callbackListenerNew(callback: T)
}