package com.noisefit.data.remote

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.noisefit.data.remote.NetworkErrors.WRONG_CLIENT_TIME_ERROR
import com.noisefit.data.remote.abstraction.TokenRefreshApi
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.safeApiCallFlow
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit.ui.onboarding.OnBoardActivity
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.Token
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.LOGS
import com.useinsider.insider.Insider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class NetworkConnectionInterceptor(
    private val appContext: Context,
    private val lastSyncProvider: LastSyncProvider,
    private val localDataStore: DataStoredInterface,
    private val ringDataStore: RingDataStore,
    private val watchesSdk: WatchesSDK,
    private val tokenRefreshApi: TokenRefreshApi,
) : Interceptor {

    private val STATUS_CODE_LOGOUT = 401
    private val STATUS_CODE_REFRESH = 403
    private val WRONG_TIME_CODE = 406


    /**
     * Created here because of cyclic dependency
     * same as AuthenticationRepository logoutUserLocally()
     */
    private fun logoutUser() {
        LOGS.d("LOG OUT USER")

        localDataStore.setVerifyMobileNumberStatus(false)
        localDataStore.setUserDataSynced(false)
        localDataStore.deleteUserInfo()
        localDataStore.deleteUserToken()
        localDataStore.deleteFcmToken()
        localDataStore.setWarrantyStatus(-1)


        Handler(Looper.getMainLooper()).post {
            appContext.showShortToast(appContext.getString(R.string.text_session_expired))
        }


        //Insider app event on device session expiry
        Insider.Instance.currentUser.logout()

        appContext.startActivity(OnBoardActivity.getStartIntent(appContext, true).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isNetworkAvailable()) {
            throw IOException("Make sure you have an active data connection")
        }
        try {
            //Add Header


            val newRequest: Request = getHeaders(chain)


            AppLogger.logRequest(newRequest)

            var response = chain.proceed(newRequest)
            val bodyString =
                response.peekBody(Long.MAX_VALUE).string()

            var contentType: String = response.headers["content-type"] ?: ""
            if (contentType.isEmpty()) {
                contentType = response.headers["Content-Type"] ?: ""
            }
            if (contentType.contains("application/json", true)) {
                AppLogger.logResponse(newRequest.url.toString(), response.code, bodyString)

                val tx = response.sentRequestAtMillis
                val rx = response.receivedResponseAtMillis

                AppLogs.sendAppLogs("API Response Time -> ${rx - tx} ms URL->${newRequest.url}")
            }

            when (response.code) {
                WRONG_TIME_CODE -> {// wrong time
                    throw IOException(WRONG_CLIENT_TIME_ERROR)
                }

                STATUS_CODE_REFRESH -> {//Refresh token
                    val lastTimestamp = localDataStore.getLastTokenRefreshTimestamp()
                    val currentTimestamp = System.currentTimeMillis()
                    val difference = (currentTimestamp - lastTimestamp)
                    if (difference < (10 * 1000) && lastTimestamp != 0L) {
                        throw IOException("Error Connecting to internet")
                    }

                    localDataStore.saveLastTokenRefreshTimestamp()
                    runBlocking {

                        getUpdatedToken().collect { resource ->
                            when (resource) {
                                is Resource.Success -> {
                                    resource.data?.let {

                                        localDataStore.updateUserToken(it.data)

                                        response = chain.proceed(getHeaders(chain))
                                    }
                                }

                                is Resource.Loading -> {}
                                is Resource.GenericError -> {
                                    logoutUser()
                                }

                                is Resource.NetworkError -> {
                                    logoutUser()
                                }
                            }
                        }
                    }

                }

                STATUS_CODE_LOGOUT -> {
                    logoutUser()
                }
            }

            return response
        } catch (e: ConnectException) {
            throw IOException("Error Connecting to internet")
        } catch (e: SocketTimeoutException) {
            throw IOException("Error Connecting to internet")
        } catch (e: UnknownHostException) {
            throw IOException("Error Connecting to internet")
        } catch (e: HttpException) {
            throw IOException("Error Connecting to internet")
        }
    }

    private fun getHeaders(chain: Interceptor.Chain): Request {
        val request = chain.request()
        val userToken = localDataStore.getUserToken()
        val deviceId = localDataStore.getDeviceToken()
        val device = ringDataStore.getRingDevice()

        return request.newBuilder().apply {
            addHeader("content-type", "application/json")
            addHeader("version", BuildConfig.VERSION_CODE.toString())
            addHeader("version-name", BuildConfig.VERSION_NAME)

            addHeader("user-agent", getUserAgent())
            addHeader("device-model", Build.MODEL)
            addHeader("device-manufacturer", Build.BRAND)
            addHeader("os-version", Build.VERSION.RELEASE)
            addHeader("platform", "android")
            addHeader("epoch-time", System.currentTimeMillis().toString())

            userToken?.let {
                addHeader("access-token", "Bearer ${userToken.access_token}")
//                addHeader("access-token", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoyNTQ5OTMsImRldmljZV9pZCI6MSwiaWF0IjoxNjk0NTkwODQ3LCJleHAiOjE2OTQ2MDUyNDd9.uSstcIECOB2xQ0jg0h56tkjOzBVtwBpxTpwoXh3N0IM")
            }
            if (request.url.toString().contains("/user_detail/ring/devices", true)) {
                userToken?.let {
                    addHeader("refresh-token", "Bearer ${userToken.refresh_token}")
                }
            }

            deviceId?.let {
                addHeader("device-external-id", it)
            }
            device?.let {
                addHeader("device-id", it.deviceId.toString())
                addHeader("device-type", it.deviceType.toString())
            }
            addHeader("wearable-type", "ring")

            addHeader("timezone", TimeZone.getDefault().id)
            addHeader(
                "offset",
                TimeUnit.MILLISECONDS.toMinutes(
                    Calendar.getInstance().get(Calendar.ZONE_OFFSET).toLong()
                ).toString()
            )
        }.build()
    }

    private suspend fun getUpdatedToken(): Flow<Resource<BaseApiResponse<Token>?>> {
        LOGS.d("getting new token")
        val refreshToken = localDataStore.getUserToken()?.refresh_token
        return safeApiCallFlow(Dispatchers.IO) {
            tokenRefreshApi.refreshAccessToken(
                "${BuildConfig.BASE_URL_NEW}/auth_v2/refresh-token",
                "Bearer $refreshToken", "ring"
            )
        }
    }

    private fun getUserAgent(): String {
        return String.format(
            Locale.US,
            "Android %s(%s;%s;%s)",
            Build.VERSION.RELEASE,
            Build.BRAND,
            Build.MODEL,
            BuildConfig.VERSION_NAME
        )
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