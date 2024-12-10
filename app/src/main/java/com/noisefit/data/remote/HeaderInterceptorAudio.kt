package com.noisefit.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.TimeZone

class HeaderInterceptorAudio(
    private val appContext: Context,
    private val localDataStore: DataStoredInterface,
    private val ringDataStore: RingDataStore,
    ) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isNetworkAvailable()) {
            throw IOException("Make sure you have an active data connection")
        }


        val userToken = localDataStore.getUserToken()
        //val device = ringDataStore.getRingDevice()


        val request = chain.request().newBuilder()
            .apply {
                addHeader("Content-Type", "audio/pcm")
                addHeader("platform", "android")
                addHeader("wearable-type", "ring")
                addHeader("timezone", TimeZone.getDefault().id)
                userToken?.let {
                    addHeader("access-token", "Bearer ${userToken.access_token}")
                }
            }
            .build()
        return chain.proceed(request)
    }

    private fun isNetworkAvailable(): Boolean {
        var result = true
        val connectivityManager =
            appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        connectivityManager?.let {
            it.getNetworkCapabilities(connectivityManager.activeNetwork)?.apply {
                result = when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            }
        }
        return result
    }
}