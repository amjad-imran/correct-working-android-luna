package com.noisefit_commans.interfaces.qrscan

import com.noisefit_commans.models.ColorFitDevice

interface QRCallbacks {
    fun onConnectStart(noiseFitDevice: ColorFitDevice?)
    fun onConnecting(noiseFitDevice: ColorFitDevice?)
    fun onDisconnectSuccess(noiseFitDevice: ColorFitDevice?)
    fun onDisconnectFailed(noiseFitDevice: ColorFitDevice?)
    fun onConnectSuccess(noiseFitDevice: ColorFitDevice?)
    fun onConnectFailed(noiseFitDevice: ColorFitDevice?)
    fun onInitCompleted(noiseFitDevice: ColorFitDevice?)
    fun onBindSuccess(noiseFitDevice: ColorFitDevice?)
    fun onBindFailure(noiseFitDevice: ColorFitDevice?)
    fun onUnbindSuccess(noiseFitDevice: ColorFitDevice?)
    fun onUnbindFailure(noiseFitDevice: ColorFitDevice?)
    fun onDeviceReady(noiseFitDevice: ColorFitDevice?)
}