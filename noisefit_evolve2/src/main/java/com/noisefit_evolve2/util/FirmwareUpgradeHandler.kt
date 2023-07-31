//package com.noisefit_evolve2.util
//
//import android.content.Context
//import android.util.Log
//import com.google.gson.Gson
//import com.google.gson.annotations.SerializedName
//import com.touchgui.sdk.TGBleClient
//import com.touchgui.sdk.TGOTAManager
//import org.json.JSONException
//import java.io.IOException
//import java.util.*
//import javax.inject.Inject
//
//
//class FirmwareUpgradeHandler
//@Inject constructor(
//    val context: Context
//) {
//
//
////    private const val LOG_TAG = "NewVersionCheckManager"
////
////    private const val STAGING_API_URL = "http://app-micro-staging.gonoise.com/firmware-versions"
////    private const val PROD_API_URL = "https://backend.gonoise.in/firmware-versions"
////
////    //private const val PROD_API_URL = "https://backend-api-temp.gonoise.in/firmware-versions"
////    private val URL =
////        if (CommonAppConfigHandler.getInstance().isProductionApplicationId) PROD_API_URL else STAGING_API_URL
////
////    private var fileDir: String? = null
////    private var filePath: String? = null
////    var mBleService: TGBleClient? = Evolve2Applicationhandler.Companion.getTGBleClient()
////
////    fun checkForNewVersion(version: Int, firmwareDeviceId: Int): NewVersionResponse? {
////        val json = MediaType.parse("application/json; charset=utf-8")
////        val body = RequestBody.create(json, getLatestInfo(version, firmwareDeviceId))
////        val client = OkHttpClient()
////        try {
////            val request =
////                Request.Builder().url(URL).addHeader("content-type", "application/json")
////                    .post(body).build()
////            val call = client.newCall(request)
////            val response = call.execute()
////            if (response.isSuccessful) {
////                val result = response.body()!!.string()
////                LoggerHelper.printVerbose(
////                    "noise_fit_event:userInfo",
////                    "firmware_download res: " + Gson().toJson(result)
////                )
////                return Gson().fromJson(result, NewVersionResponse::class.java)
////            }
////        } catch (e: IOException) {
////            Log.e(LOG_TAG, e.toString())
////        } catch (e: JSONException) {
////            Log.e(LOG_TAG, e.toString())
////        }
////        return null
////    }
//
////        private fun getLatestInfo(version: Int, firmwareDeviceId: Int): String {
////            LoggerHelper.printVerbose(
////                "noise_fit_event:userInfo",
////                "firmware_download res: " + version + ":" + firmwareDeviceId
////            )
////            val jsonObject = JSONObject()
////            try {
////                jsonObject.put("version", version)
////                jsonObject.put("firmware_id", firmwareDeviceId)
////                jsonObject.put("device_type", getConnectedDevice()?.deviceType)
////                jsonObject.put("platform", "android")
////            } catch (e: JSONException) {
////                e.printStackTrace()
////            }
////            return jsonObject.toString()
////        }
//
////        private fun downloadFirmwarePackage(downloadUrl: String): Boolean {
////            val client = OkHttpClient()
////            val request = Request.Builder().url(downloadUrl).build()
////            try {
////                fileDir = "/storage/emulated/0/Android/data/${
////                    CommonAppConfigHandler.getInstance().getApplicationId()
////                }/${getConnectedDevice()?.deviceType}"
////                filePath = "$fileDir/dfu"
////                val dir = File(fileDir)
////                if (!dir.exists()) {
////                    dir.mkdirs()
////                }
////                LoggerHelper.printVerbose("noise_fit_event:", "firmware_download : started")
////                val call = client.newCall(request)
////                val response = call.execute()
////                if (response.isSuccessful) {
////                    val body = response.body()
////                    val inputStream = body!!.byteStream()
////                    Log.i(LOG_TAG, "size=" + body.contentLength())
////                    val fos: OutputStream = FileOutputStream(filePath)
////                    var len: Int
////                    val b = ByteArray(2048)
////                    var sum: Long = 0
////                    while (inputStream.read(b).also { len = it } != -1) {
////                        sum += len.toLong()
////                        Log.i(LOG_TAG, sum.toString() + "")
////                        fos.write(b, 0, len)
////                    }
////                    fos.flush()
////                    fos.close()
////                    LoggerHelper.printVerbose(
////                        "noise_fit_event:colorfit_pro_2",
////                        "firmware_download : success"
////                    )
////                    return true
////                }
////            } catch (e: Exception) {
////                e.printStackTrace()
////            }
////            LoggerHelper.printVerbose(
////                "noise_fit_event:colorfit_pro_2",
////                "firmware_download : failure"
////            )
////            return false
////        }
//
//    fun updateFirmware(
//        firmwareUrl: String?,
//        firmwareUpgradeStatus: Evolve2UpdateDeviceUnitsHandler.FirmwareUpgradeStatus
//    ) {
//        firmwareUrl?.let {
//            if (downloadFirmwarePackage(it)) {
//                LoggerHelper.printVerbose(
//                    "noise_fit_event:colorfit_pro_2",
//                    "firmware_upgrade : success1"
//                )
//                upgradeFirmware(firmwareUpgradeStatus)
//            } else {
//                LoggerHelper.printVerbose(
//                    "noise_fit_event:colorfit_pro_2",
//                    "firmware_upgrade : failure1"
//                )
//                firmwareUpgradeStatus.onUpdate(DeviceFirmware(status = "error"))
//            }
//        }
//    }
//
//    private fun upgradeFirmware(firmwareUpgradeStatus: Evolve2UpdateDeviceUnitsHandler.FirmwareUpgradeStatus) {
//        if (mBleService != null) {
//            val isForce = true
//            val version = "443"
//            val md5 = "121412511"
//            val manager: TGOTAManager? = mBleService?.otaManager
//            manager?.setCallback(object : TGOTAManager.OTACallback {
//                override fun onProgress(p0: Int) {
//                    firmwareUpgradeStatus.onUpdate(
//                        DeviceFirmware(
//                            status = "progress",
//                            percentage = p0
//                        )
//                    )
//                }
//
//                override fun onCompleted() {
//                    firmwareUpgradeStatus.onUpdate(DeviceFirmware(status = "success"))
//                }
//
//                override fun onError(p0: Throwable?) {
//                    firmwareUpgradeStatus.onUpdate(
//                        DeviceFirmware(
//                            status = "error",
//                            message = "Aborted"
//                        )
//                    )
//                }
//            })
//            manager?.start(filePath);
//        }
//    }
//
//}
//
//
////class NewVersionResponse {
////    @SerializedName("code")
////    var resultCode = 0
////
////    @SerializedName("message")
////    var message: String? = null
////
////    @SerializedName("data")
////    var data: NewVersionInfo? = null
////
////    class NewVersionInfo {
////        @SerializedName("url")
////        var url: String? = null
////
////        @SerializedName("forceUpdate")
////        var forceUpdate = false
////
////        @SerializedName("description_english")
////        var descriptionEnglish: String? = null
////
////        @SerializedName("softUpdate")
////        var specialUpgrade = false
////
////        @SerializedName("version")
////        var version = 0
////
////    }