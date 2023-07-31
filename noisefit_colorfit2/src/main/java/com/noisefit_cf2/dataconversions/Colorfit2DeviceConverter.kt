package com.noisefit_cf2.dataconversions

import com.ido.ble.bluetooth.device.BLEDevice
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType

class Colorfit2DeviceConverter {

    companion object {

//        fun getColorfit2FromBle(bleDevice: BLEDevice): ColorFitDevice {
//            return ColorFitDevice(bluetoothName = bleDevice.mDeviceName,
//                    address = bleDevice.mDeviceAddress,
//                    rssi = bleDevice.mRssi,
//                    deviceId = bleDevice.mDeviceId,
//                    id = bleDevice.mId,
//                    isInDuffMode = bleDevice.mIsInDfuMode,
//                    mIs = bleDevice.mIs,
//                    len = bleDevice.mLen,
//                    deviceType = DeviceType.COLORFIT_2.deviceType)
//        }
//
//        fun getColorfitPro2FromBle(bleDevice: BLEDevice): ColorFitDevice {
//            return ColorFitDevice(bluetoothName = bleDevice.mDeviceName,
//                    address = bleDevice.mDeviceAddress,
//                    rssi = bleDevice.mRssi,
//                    deviceId = bleDevice.mDeviceId,
//                    id = bleDevice.mId,
//                    isInDuffMode = bleDevice.mIsInDfuMode,
//                    mIs = bleDevice.mIs,
//                    len = bleDevice.mLen,
//                    deviceType = DeviceType.COLORFIT_PRO_2.deviceType)
//        }
//
//        fun getColorfitPro3FromBle(bleDevice: BLEDevice): ColorFitDevice {
//            return ColorFitDevice(bluetoothName = bleDevice.mDeviceName,
//                    address = bleDevice.mDeviceAddress,
//                    rssi = bleDevice.mRssi,
//                    deviceId = bleDevice.mDeviceId,
//                    id = bleDevice.mId,
//                    isInDuffMode = bleDevice.mIsInDfuMode,
//                    mIs = bleDevice.mIs,
//                    len = bleDevice.mLen,
//                    deviceType = DeviceType.COLORFIT_PRO_3.deviceType)
//        }
//
//        fun getColorfitPro2OxyFromBle(bleDevice: BLEDevice): ColorFitDevice {
//            return ColorFitDevice(bluetoothName = bleDevice.mDeviceName,
//                    address = bleDevice.mDeviceAddress,
//                    rssi = bleDevice.mRssi,
//                    deviceId = bleDevice.mDeviceId,
//                    id = bleDevice.mId,
//                    isInDuffMode = bleDevice.mIsInDfuMode,
//                    mIs = bleDevice.mIs,
//                    len = bleDevice.mLen,
//                    deviceType = DeviceType.COLORFIT_PRO_2_OXY.deviceType)
//        }
//
//        fun getNoisefitActiveFromBle(bleDevice: BLEDevice): ColorFitDevice {
//            return ColorFitDevice(bluetoothName = bleDevice.mDeviceName,
//                    address = bleDevice.mDeviceAddress,
//                    rssi = bleDevice.mRssi,
//                    deviceId = bleDevice.mDeviceId,
//                    id = bleDevice.mId,
//                    isInDuffMode = bleDevice.mIsInDfuMode,
//                    mIs = bleDevice.mIs,
//                    len = bleDevice.mLen,
//                    deviceType = DeviceType.NOISEFIT_ACTIVE.deviceType)
//        }

        fun getBleFromColorfit2(colorFit2: ColorFitDevice): BLEDevice {
            val bleDevice = BLEDevice()
            bleDevice.mDeviceName = colorFit2.bluetoothName
            bleDevice.mDeviceAddress = colorFit2.address
            bleDevice.mDeviceId = colorFit2.deviceId
//            bleDevice.mId = colorFit2.id
            bleDevice.mIsInDfuMode = colorFit2.isInDuffMode
//            bleDevice.mIs = colorFit2.mIs
//            bleDevice.mLen = colorFit2.len
            bleDevice.mRssi = colorFit2.rssi
            return bleDevice
        }
    }
}