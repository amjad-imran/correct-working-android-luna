package com.noisefit.hybrid.utils

import android.content.Context


import cn.appscomm.bluetooth.implement.MBluetooth;
import cn.appscomm.bluetooth.interfaces.IBluetoothResultCallback;
import cn.appscomm.bluetooth.interfaces.IUpdateProgressCallBack;
import cn.appscomm.bluetooth.interfaces.PMBluetoothCall;
import cn.appscomm.bluetoothsdk.interfaces.ResultCallBack;
import cn.appscomm.bluetoothsdk.utils.FileUtil;
import cn.appscomm.bluetoothsdk.utils.ParseUtil;
import cn.appscomm.ota.util.OtaAppContext;
import com.noisefit_commans.utils.LOGS
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException


object APGSOta {

    fun updateAGPS(file1: File?, file2: File?, time: Long, callBack: ResultCallBack?) {
        var time = time
        if (time <= 0) {
            time = System.currentTimeMillis() / 1000L
        }
        try {
            val fis1 = FileInputStream(file1)
            val fis2 = FileInputStream(file2)
            val finalTime = time
            // 这个回调是，获取设备的AGPS时间的回调
            val tmp: IBluetoothResultCallback = object : IBluetoothResultCallback {
                override fun onSuccess(s: String) {
                    val bv = MBluetooth.INSTANCE.getBluetoothVarByMAC(s)
                    if (file1 != null && bv != null) {
                        val lastTime = bv.agpsUpdateTimeStamp
                        // 7天的, 604800 = 7天的秒数
                        if (finalTime - lastTime > 604800L) {
                            val context: Context = OtaAppContext.INSTANCE.context
                            val basePath: String = context.cacheDir.absolutePath
                            val path = "$basePath/gps.bin"
                            FileUtil.saveAGPSData(path, fis2)
                            val pcb: IUpdateProgressCallBack = object : IUpdateProgressCallBack {
                                override fun curUpdateProgress(i: Int) {
                                    val params = arrayOfNulls<Any>(1)
                                    params[0] = i
                                    callBack?.onSuccess(200826002, params)
                                }

                                override fun curUpdateMax(i: Int) {
                                    val params = arrayOfNulls<Any>(1)
                                    params[0] = i
                                    callBack?.onSuccess(200826003, params)
                                }

                                override fun updateResult(b: Boolean) {
                                    if (b) {
                                        val params = arrayOfNulls<Any>(1)
                                        params[0] = java.lang.Boolean.TRUE
                                        if (callBack != null) {
                                            callBack.onSuccess(200826004, params)
                                            LOGS.d(
                                                "nbvb",
                                                "updateAPGSData 10"
                                            )
                                        }
                                        var var3: Array<String?>
                                        arrayOfNulls<String>(1).also { var3 = it }[0] = cn.appscomm.bluetoothsdk.app.a.a
                                        MBluetooth.INSTANCE.setAGPSUpdateTimeStamp(object :
                                            IBluetoothResultCallback {
                                            override fun onSuccess(s: String) {
                                                // ignore callback
                                            }

                                            override fun onFail(s: String) {
                                                // ignore callback
                                            }
                                        }, 2, finalTime, PMBluetoothCall.COMMAND_TYPE_PAGE, *var3)
                                        // MBluetooth.INSTANCE.setAGPSUpdateTimeStamp(null, 2, finalTime, PMBluetoothCall.COMMAND_TYPE_PAGE, var3);
                                    } else {
                                        if (callBack != null) {
                                            callBack.onFail(200826004)
                                            LOGS.d(
                                                "nbvb",
                                                "updateAPGSData 1"
                                            )
                                        }
                                    }
                                }
                            }
                            val var10001 = cn.appscomm.bluetoothsdk.app.a.a
                            // 开始发送OTA
                            MBluetooth.INSTANCE.updateGPS(var10001, path, pcb)
                        } else {
                            // 1天的
                            val rc: IBluetoothResultCallback = object : IBluetoothResultCallback {
                                override fun onSuccess(s: String) {
                                    val var5 = MBluetooth.INSTANCE.getBluetoothVarByMAC(s)
                                    if (var5 != null) {
                                        if (finalTime - var5.agpsUpdateTimeStamp > 86400L) {
                                            val bytes: ByteArray =
                                                FileUtil.fileToBytes(file1.absolutePath)
                                            if (bytes == null) {
                                                if (callBack != null) {
                                                    callBack.onFail(200826004)
                                                    LOGS.d(
                                                        "nbvb",
                                                        "updateAPGSData 2"
                                                    )
                                                }
                                                return
                                            }
                                            val pList = ParseUtil.parseAGPSPackageList(finalTime, bytes)
                                            val setTodayCallback: IBluetoothResultCallback =
                                                object : IBluetoothResultCallback {
                                                    override fun onSuccess(s: String) {
                                                        var var4: Array<Any?>?
                                                        arrayOfNulls<Any>(1).also { var4 = it }[0] =
                                                            java.lang.Boolean.TRUE
                                                        if (callBack != null) {
                                                            callBack.onSuccess(200826004, var4)
                                                            LOGS.d(
                                                                "nbvb",
                                                                "updateAPGSData 3"
                                                            )
                                                        }
                                                        var var3: Array<String?>
                                                        arrayOfNulls<String>(1).also {
                                                            var3 = it
                                                        }[0] = cn.appscomm.bluetoothsdk.app.a.a
                                                        MBluetooth.INSTANCE.setAGPSUpdateTimeStamp(
                                                            null,
                                                            4,
                                                            finalTime,
                                                            PMBluetoothCall.COMMAND_TYPE_PAGE,
                                                            *var3
                                                        )
                                                    }

                                                    override fun onFail(s: String) {
                                                        if (callBack != null) {
                                                            callBack.onFail(200826004)
                                                            LOGS.d(
                                                                "nbvb",
                                                                "updateAPGSData 4$s"
                                                            )
                                                        }
                                                    }
                                                }
                                            var var3: Array<String?>
                                            arrayOfNulls<String>(1).also { var3 = it }[0] = cn.appscomm.bluetoothsdk.app.a.a
                                            MBluetooth.INSTANCE.sendAGPSData(
                                                setTodayCallback,
                                                pList,
                                                PMBluetoothCall.COMMAND_TYPE_PAGE,
                                                *var3
                                            )
                                        } else {
                                            // 不需要OTA 1天的
                                            var var4: Array<Any?>?
                                            arrayOfNulls<Any>(1).also { var4 = it }[0] =
                                                java.lang.Boolean.TRUE
                                            if (callBack != null) {
                                                callBack.onSuccess(200826004, var4)
                                                LOGS.d(
                                                    "nbvb",
                                                    "updateAPGSData 5"
                                                )
                                            }
                                        }
                                    } else {
                                        // 没有找到Var，直接失败？
                                        if (callBack != null) {
                                            callBack.onFail(200826004)
                                            LOGS.d(
                                                "nbvb",
                                                "updateAPGSData 6"
                                            )
                                        }
                                    }
                                }

                                override fun onFail(s: String) {
                                    if (callBack != null) {
                                        callBack.onFail(200826004)
                                        LOGS.d(
                                            "nbvb",
                                            "updateAPGSData 7"
                                        )
                                    }
                                }
                            }
                            var macs: Array<String?>
                            arrayOfNulls<String>(1).also { macs = it }[0] = cn.appscomm.bluetoothsdk.app.a.a
                            MBluetooth.INSTANCE.getAGPSUpdateTimeStamp(
                                rc,
                                4,
                                PMBluetoothCall.COMMAND_TYPE_PAGE,
                                *macs
                            )
                        }
                    } else {
                        if (callBack != null) {
                            callBack.onFail(200826004)
                            LOGS.d("nbvb", "updateAPGSData 8")
                        }
                    }
                }

                override fun onFail(s: String) {
                    if (callBack != null) {
                        callBack.onFail(200826004)
                        LOGS.d("nbvb", "updateAPGSData 9")
                    }
                }
            }
            var var4: Array<String?>
            arrayOfNulls<String>(1).also { var4 = it }[0] = cn.appscomm.bluetoothsdk.app.a.a
            MBluetooth.INSTANCE.getAGPSUpdateTimeStamp(
                tmp,
                2,
                PMBluetoothCall.COMMAND_TYPE_PAGE,
                *var4
            )
        } catch (ex: FileNotFoundException) {
            if (callBack != null) {
                callBack.onFail(200826001)
                ex.printStackTrace()
            }
        }
    }

}