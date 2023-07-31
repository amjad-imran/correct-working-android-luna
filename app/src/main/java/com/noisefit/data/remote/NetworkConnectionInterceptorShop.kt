package com.noisefit.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NetworkConnectionInterceptorShop(
    val appContext: Context,
) : Interceptor {


    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isNetworkAvailable()) {
            throw IOException("Make sure you have an active data connection")
        }
        try {
            //Add Header
            val request = chain.request()


            val newRequest: Request = request.newBuilder().apply {
                addHeader("content-type", "application/json")
                addHeader(
                    "x-auth-secret",
                    "9b51dd47f60c91e32cdf5c20e604799ab91f4b2d826e969c360f740ac1bca870"
                )
                addHeader(
                    "x-auth-partner",
                    "app"
                )
            }.build()


            AppLogger.logRequest(newRequest)

            val response = chain.proceed(newRequest)
            val bodyString =
                response.peekBody(Long.MAX_VALUE).string()

            var contentType: String = response.headers["content-type"] ?: ""
            if (contentType.isEmpty()) {
                contentType = response.headers["Content-Type"] ?: ""
            }
            if (contentType.contains("application/json", true)) {
                AppLogger.logResponse(newRequest.url.toString(), response.code, bodyString)
            }
            if (response.code == 401) {
                /**
                 * Remove Session incase of unauthorised
                 */
            }

            return response
        } catch (e: ConnectException) {
            throw IOException("Error Connecting to internet")
        } catch (e: SocketTimeoutException) {
            throw IOException("Error Connecting to internet")
        } catch (e: UnknownHostException) {
            throw IOException("Error Connecting to internet")
        }
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
                    else -> false
                }
            }
        }
        return result
    }

}