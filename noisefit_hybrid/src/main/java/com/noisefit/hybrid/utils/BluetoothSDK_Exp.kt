package com.noisefit.hybrid.utils

import cn.appscomm.bluetooth.implement.MBluetooth
import cn.appscomm.bluetooth.interfaces.IBluetoothResultCallback
import cn.appscomm.bluetooth.interfaces.PMBluetoothCall
import cn.appscomm.bluetooth.mode.Customize
import cn.appscomm.bluetoothsdk.app.BluetoothSDK
import cn.appscomm.bluetoothsdk.app.SettingType
import cn.appscomm.bluetoothsdk.app.a
import cn.appscomm.bluetoothsdk.interfaces.ResultCallBack
import cn.appscomm.bluetoothsdk.utils.ParseUtil
import com.google.gson.Gson
import com.noisefit_commans.utils.LOGS
import javax.inject.Inject

class BluetoothSDK_Exp @Inject constructor() {
    abstract class BoolCallback {
        abstract fun onSuccess()
        abstract fun onFail(code: Int)
    }

    abstract class CustomReplyCallback {
        abstract fun onSuccess(customizeReplyList: List<Customize>?)
        abstract fun onFail(code: Int)
    }

    fun jumpOutTakePhoto(callback: BoolCallback?) {
        val resultCallBack: ResultCallBack = object : ResultCallBack {
            override fun onSuccess(i: Int, objects: Array<Any>) {
                // clear callback

                if (objects.isEmpty()) {
                    callback?.onFail(1)
                    return
                }

                val callbackBytes = objects[0] as ByteArray
                if (callbackBytes.size == 8 && (callbackBytes[5] == 0x1a.toByte() && callbackBytes[6] == 0x00.toByte())) {
                    BluetoothSDK.set8002CallBack(null)
                    callback?.onSuccess()
                }
            }

            override fun onFail(i: Int) {
                callback?.onFail(1)
                BluetoothSDK.set8002CallBack(null)
            }
        }

        BluetoothSDK.set8002CallBack(resultCallBack)
        val bytes = byteArrayOf(0x6f, 0x1a, 0x71, 0x01, 0x00, 0x0c, 0x8f.toByte())
        BluetoothSDK.sendCustomCommand(bytes, true)
    }

    fun editCustomizeReply(index: Int, text: String?, callback: BoolCallback?) {
        val crc = ParseUtil.stringToCRC(text)
        LOGS.d("editCustomizeReply $index $text $crc")
        val resultCallback: IBluetoothResultCallback = object : IBluetoothResultCallback {
            override fun onSuccess(s: String) {
                val `var` = MBluetooth.INSTANCE.getBluetoothVarByMAC(s)
                if (`var` == null) {
                    callback?.onFail(21)
                    return
                }
                //val list: List<Customize> = `var`.customizeList
                //LOGS.d("editCustomizeReply:: ${Gson().toJson(list)}")
                callback?.onSuccess()
            }

            override fun onFail(s: String) {
                callback?.onFail(1)
            }
        }
        var macs: Array<String?>
        arrayOfNulls<String>(1).also { macs = it }[0] = a.a

        LOGS.d("editCustomizeReply:_:::::: $index $text $crc")
        MBluetooth.INSTANCE.setCustomizeReplyUTF8(
            resultCallback,
            SettingType.CUSTOMIZE_REPLY_CHANGE,
            index,
            crc,
            text,
            PMBluetoothCall.COMMAND_TYPE_CALL,
            *macs
        )
    }


    fun getCustomizeReply(callback: CustomReplyCallback?) {
        val countCallBack: ResultCallBack = object : ResultCallBack {
            override fun onSuccess(i: Int, objects: Array<Any>) {
                BluetoothSDK.set8002CallBack(null)
                val callbackBytes = objects[0] as ByteArray
                if (callbackBytes.size < 6) {
                    callback?.onFail(1)
                    return
                }
                val count = callbackBytes[5].toInt()
                val resultCallback: IBluetoothResultCallback = object : IBluetoothResultCallback {
                    override fun onSuccess(s: String) {
                        val `var` = MBluetooth.INSTANCE.getBluetoothVarByMAC(s)
                        if (`var` == null) {
                            callback?.onFail(21)
                            return
                        }
                        val list: List<Customize> = `var`.customizeList
                        callback?.onSuccess(list)
                    }

                    override fun onFail(s: String) {
                        callback?.onFail(31)
                    }
                }
                var macs: Array<String?>
                arrayOfNulls<String>(1).also { macs = it }[0] = a.a
                MBluetooth.INSTANCE.getCustomizeReplyUTF8(resultCallback, 0, null, count, 1, *macs)
            }

            override fun onFail(i: Int) {
                BluetoothSDK.set8002CallBack(null)
                callback?.onFail(1)
            }
        }
        BluetoothSDK.set8002CallBack(countCallBack)
        val bytes = byteArrayOf(0x6f, 0xda.toByte(), 0x70, 0x01, 0x00, 0x00, 0x8f.toByte())
        BluetoothSDK.sendCustomCommand(bytes, true)
    }

}