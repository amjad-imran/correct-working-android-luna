package com.noisefit.data.repository.abstraction

import com.noisefit_commans.utils.bleUtils.DeviceEntity

interface IBluetoothScan {

    fun onScanStarted()

    fun onScanFinished()

    fun onDeviceFound(deviceEntity: DeviceEntity)
}