//package com.noisefit_evolve2.handler.misc
//
//import com.noisefit_commans.BuildConfig
//import com.noisefit_commans.NoisefitApplication
//
//import javax.inject.Inject
//
//private const val LOG_TAG = "AGPSManager"
//private const val STAGING_API_URL = "http://app-micro-staging.gonoise.com/devices/file/link"
//private const val PROD_API_URL = "https://backend.gonoise.in/devices/file/link"
////private const val PROD_API_URL = "https://backend-api-temp.gonoise.in/devices/file/link"
//
//class AGPSUpdateHandler
//@Inject
//constructor(var context: NoisefitApplication) {
//
//
//    private val URL =
//        if (BuildConfig.DEBUG)
//            PROD_API_URL else STAGING_API_URL
//
//    private var fileDir: String? = null
//    private var filePath: String? = null
//
////    var mBleService: ZhBraceletService? =
////        NavPlusApplicationHandler.getZhBraceletService()
//
////    fun getAGPSData() {
////
////        val client = OkHttpClient()
////        try {
////            val request =
////                Request.Builder()
////                    .url(URL)
////                    .addHeader("content-type", "application/json")
////                    .get()
////                    .build()
////            val call = client.newCall(request)
////            val response = call.execute()
////            if (response.isSuccessful) {
////                val result = response.body()!!.string()
//////                LoggerHelper.printVerbose(
//////                    "noise_fit_event:noisefit_hybrid",
//////                    "updateAPGSData 15: " + Gson().toJson(result)
//////                )
////                val json = JSONObject(result)
////                if (json.getInt("status") == 200) {
////                }
////                //updateAGPS(json.getJSONObject("response").getString("file"))
////
////                //updateAGPS("http://file.genius.liuqingzhi.com/upload/20210504/89ceda529a5745988ff8796cfbfbb368.brm")
////            }
////        } catch (e: IOException) {
//////            Log.e(LOG_TAG, e.toString())
////        } catch (e: JSONException) {
//////            Log.e(LOG_TAG, e.toString())
////        }
////    }
//
//
////    private fun downloadAGPSPackage(downloadUrl: String): Boolean {
////        val client = OkHttpClient()
////        val request = Request.Builder().url(downloadUrl).build()
////        try {
////            fileDir = "/storage/emulated/0/Android/data/${
////                CommonAppConfigHandler.getInstance().getApplicationId()
////            }/${getConnectedDevice()?.deviceType}"
////            filePath = "$fileDir/agps.brm"
////            val dir = File(fileDir)
////            if (!dir.exists()) {
////                dir.mkdirs()
////            }
//////            LoggerHelper.printVerbose("noise_fit_event:", "AGPS : started")
////            val call = client.newCall(request)
////            val response = call.execute()
////            if (response.isSuccessful) {
////                val body = response.body()
////                val inputStream = body!!.byteStream()
//////                Log.i(LOG_TAG, "size=" + body.contentLength() + ":" + inputStream)
////                val fos: OutputStream = FileOutputStream(filePath)
////                var len: Int
////                val b = ByteArray(2048)
////                var sum: Long = 0
////                while (inputStream.read(b).also { len = it } != -1) {
////                    sum += len.toLong()
////                    Log.i(LOG_TAG, sum.toString() + "")
////                    fos.write(b, 0, len)
////                }
////                fos.flush()
////                fos.close()
//////                LoggerHelper.printVerbose(
//////                    "noise_fit_event:colorfit_pro_2",
//////                    "updateAPGSData : success"
//////                )
////                return true
////            }
////        } catch (e: Exception) {
////            e.printStackTrace()
////        }
//////        LoggerHelper.printVerbose(
//////            "noise_fit_event:colorfit_pro_2",
//////            "updateAPGSData : failure"
//////        )
////        return false
////    }
////
////    private fun updateAGPS(agpsURL: String?) {
////        agpsURL?.let {
////            if (downloadAGPSPackage(it)) {
//////                LoggerHelper.printVerbose(
//////                    "noise_fit_event:colorfit_pro_2",
//////                    "updateAPGSData : success1" + agpsURL
//////                )
////                updateAPGSData()
////            } else {
//////                LoggerHelper.printVerbose(
//////                    "noise_fit_event:colorfit_pro_2",
//////                    "updateAPGSData : failure1"
//////                )
////
////            }
////        }
////    }
////
////    private fun updateAPGSData() {
////        getConnectedDevice()?.deviceType?.let { deviceType ->
////            if (deviceType == "colorfit_nav_plus") {
////                val file1: File = File(filePath)
////                if (mBleService != null) {
////                    mBleService!!.getAGpsPrepareStatus(object : AGpsPrepareStatusListener {
////                        override fun timeOut() {
////                            CommonGlobals.isWatchDataUpdating = false
////                            LoggerHelper.printVerbose(
////                                "noise_fit_event:noisefit_hybrid",
////                                "updateAPGSData : onTimeout"
////                            )
////
////                        }
////
////                        override fun onSuccess(needGpsInfo: Boolean) {
////                            LoggerHelper.printVerbose(
////                                "noise_fit_event:noisefit_hybrid",
////                                "updateAPGSData Success"
////                            )
////                            if (needGpsInfo) {
////                                val fileByte: ByteArray = file1.readBytes()
////                                mBleService!!.startUploadBigData(
////                                    BleConstant.UPLOAD_BIG_DATA_LTO,
////                                    fileByte,
////                                    object : UploadBigDataListener {
////                                        override fun onSuccess() {
////                                            CommonGlobals.isWatchDataUpdating = false
////                                            LoggerHelper.printVerbose(
////                                                "noise_fit_event:noisefit_hybrid",
////                                                "updateAPGSData : Success"
////                                            )
////                                        }
////
////                                        override fun onProgress(
////                                            curPiece: Int,
////                                            dataPackTotalPieceLength: Int
////                                        ) {
////                                            CommonGlobals.isWatchDataUpdating = true
////                                            LoggerHelper.printVerbose(
////                                                "noise_fit_event:noisefit_hybrid",
////                                                "updateAPGSData" + (curPiece * 100 / dataPackTotalPieceLength)
////                                            )
////                                        }
////
////                                        override fun onTimeout() {
////                                            CommonGlobals.isWatchDataUpdating = false
////                                            LoggerHelper.printVerbose(
////                                                "noise_fit_event:noisefit_hybrid",
////                                                "updateAPGSData : onTimeout"
////                                            )
////                                        }
////                                    })
////                            }
////                        }
////                    })
////                }
////            } else {
////
////            }
////        }
////
////    }
//
//}
//
