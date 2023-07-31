package com.noisefit_commans.network

import com.google.gson.Gson
import com.noisefit_commans.models.CFP2WatchFaces
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import java.io.IOException
import com.noisefit_commans.models.WatchFace
import org.json.JSONObject


class CF2NetworkCalls {
    companion object {
        private const val WATCH_FACES_URL =
            "http://veryfitproapi.veryfitplus.com/dailPlate/list?deviceId="

        fun getWatchFaces(deviceId: Int): CFP2WatchFaces? {
            val client = OkHttpClient()
            try {
                val request = Request.Builder().url(WATCH_FACES_URL + deviceId)
                    .addHeader("content-type", "application/json").get().build()
                val call = client.newCall(request)
                val response = call.execute()
                if (response.isSuccessful) {
                    val result = response.body!!.string()
                    return Gson().fromJson(result, CFP2WatchFaces::class.java)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return null
        }
        fun getWatchFaceDetails(watchFace: WatchFace):WatchFace? {
            val watchFaceUrl = "http://veryfitproapi.veryfitplus.com/dailPlate/info?id=${watchFace.faceId}&imagename=cloud"

            val client = OkHttpClient()
            try {
                val request = Request.Builder().url(watchFaceUrl).addHeader("content-type", "application/json").get().build()
                val call = client.newCall(request)
                val response = call.execute()
                if (response.isSuccessful) {
                    val result = response.body!!.string()
                    val json = JSONObject(result)
                    val fileUrl = json.getJSONObject("data").getJSONObject("zip").getString("url")
                    watchFace.fileUrl = fileUrl
                    if(json.getJSONObject("data").getJSONObject("zip").has("originalFileName")) {
                        val fileName = json.getJSONObject("data").getJSONObject("zip").getString("originalFileName")
                        watchFace.zipName = fileName
                    }
                    return watchFace
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return null
        }
    }


}