package com.noisefit.data.remote

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.freshchat.consumer.sdk.Freshchat
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit.data.remote.NetworkErrors.FORCE_UPDATE
import com.noisefit.data.remote.NetworkErrors.WRONG_CLIENT_TIME_ERROR
import com.noisefit.data.remote.abstraction.TokenRefreshApi
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.safeApiCallFlow
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit.ui.onboarding.OnBoardActivity
import com.noisefit.util.ApplicationUtils
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.data.model.Token
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.LOGS
import com.oreo.data.db.OreoDataBase
import com.oreo.data.repository.AlarmRepository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
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
    private val watchDataStore: WatchDataStore,
    private val resourcesProvider: ResourcesProvider,
    private val watchesSdk: WatchesSDK,
    private val alarmRepository: AlarmRepository,
    private val keyValueDataSource: KeyValueDataSource,
    private val database: OreoDataBase,
    private val tokenRefreshApi: TokenRefreshApi,
) : Interceptor {

    private val STATUS_CODE_LOGOUT = 401
    private val STATUS_CODE_REFRESH = 403
    private val WRONG_TIME_CODE = 406
    private val APP_FORCE_UPDATE = 410


    /**
     * Created here because of cyclic dependency
     * same as AuthenticationRepository logoutUserLocally()
     */
    private fun logoutUser() {
        LOGS.d("LOG OUT USER")

        localDataStore.setVerifyMobileNumberStatus(false)
        localDataStore.setUserDataSynced(false)
        localDataStore.deleteUserInfo()
        localDataStore.clearUserLogoutData()
        localDataStore.clearDashCardClickState()
        localDataStore.deleteUserToken()
        localDataStore.deleteFcmToken()
        localDataStore.setWarrantyStatus(-1)
        localDataStore.setCrossedCampaign(-1)
        localDataStore.saveAudioMaxAmp(0)
        localDataStore.clearDisplayEditHomeScreenCard()
        localDataStore.clearCustomHomeScreenApiCallTimeStamps()
        localDataStore.clearCustomHomeScreenItemsPriorityList()
        //
        localDataStore.setIsInDemoMode(false)

        GlobalScope.launch(Dispatchers.IO) {
            alarmRepository.cancelAllAlarms()
            removeOfflineUserData()
        }

        Handler(Looper.getMainLooper()).post {
            appContext.showShortToast(resourcesProvider.getString(R.string.text_session_expired))
        }

        Freshchat.resetUser(NoiseFitApplicationMain.context)



        //Insider app event on device session expiry


        appContext.startActivity(OnBoardActivity.getStartIntent(appContext, true).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
    private suspend fun removeOfflineUserData() {
        database.clearAllTables()
        keyValueDataSource.removeDataByType(KeyValueDataType.SLEEP_PLANNER)
        keyValueDataSource.removeDataByType(KeyValueDataType.NOTIFICATION_GOAL_TOGGLE)
        keyValueDataSource.removeDataByType(KeyValueDataType.NOTIFICATION_GOAL_DATA)
        keyValueDataSource.removeDataByType(KeyValueDataType.CIRCADIAN_DATA)
        keyValueDataSource.removeDataByType(KeyValueDataType.FEMALE_CYCLE_HISTORY)
        keyValueDataSource.removeDataByType(KeyValueDataType.FEMALE_HEALTH_CURRENT_DAY_V2)
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
                APP_FORCE_UPDATE -> {
                    throw IOException(FORCE_UPDATE)
                }

                STATUS_CODE_REFRESH -> {//Refresh token
                    /*val lastTimestamp = localDataStore.getLastTokenRefreshTimestamp()
                    val currentTimestamp = System.currentTimeMillis()
                    val difference = (currentTimestamp - lastTimestamp)
                    if (difference < (10 * 1000) && lastTimestamp != 0L) {
                        throw IOException("Error Connecting to internet")
                    }

                    localDataStore.saveLastTokenRefreshTimestamp()*/
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
            throw IOException(resourcesProvider.getString(R.string.text_error_connecting_to_internet))
        } catch (e: SocketTimeoutException) {
            throw IOException(resourcesProvider.getString(R.string.text_error_connecting_to_internet))
        } catch (e: UnknownHostException) {
            throw IOException(resourcesProvider.getString(R.string.text_error_connecting_to_internet))
        } catch (e: HttpException) {
            throw IOException(resourcesProvider.getString(R.string.text_error_connecting_to_internet))
        }catch (e: StringIndexOutOfBoundsException) {//For interceptor crash
            throw IOException(resourcesProvider.getString(R.string.text_error_connecting_to_internet))
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
            val appLanguage = localDataStore.getSelectedAppLanguage()?:ApplicationUtils.getDefaultLanguage().languageCode
            addHeader("accept-language", appLanguage)
            addHeader("user-agent", getUserAgent())
            addHeader("device-model", Build.MODEL)
            addHeader("device-manufacturer", Build.BRAND)
            addHeader("os-version", Build.VERSION.RELEASE)
            addHeader("platform", "android")
            addHeader("epoch-time", System.currentTimeMillis().toString())

            userToken?.let {
                addHeader("access-token", "Bearer ${userToken.access_token}")
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
                val sNo = if (it.ringInfo?.serialNoRaw.isNullOrEmpty()) {
                    val sNo = watchDataStore.getSerialNo()
                    sNo ?: ""
                } else {
                    it.ringInfo?.serialNoRaw ?: ""
                }

                val fwVersion = if(WatchInfoGlobals.firmwareVersionRing.isNullOrEmpty()){
                    watchDataStore.getFirmwareVersion()
                }else{
                    WatchInfoGlobals.firmwareVersionRing
                }

                addHeader("device-id", it.deviceId.toString())
                addHeader("device-type", it.deviceType.toString())
                addHeader("bt-pc", watchDataStore.getBatteryPercentRing().toString())
                addHeader("fv", fwVersion?:"")
            }
            addHeader("wearable-type", "ring")

            val timeZone = localDataStore.getLastKnownTimezone() ?: TimeZone.getDefault().id
            addHeader("timezone", timeZone)
            addHeader(
                "offset",
                TimeUnit.MILLISECONDS.toMinutes(
                    Calendar.getInstance().get(Calendar.ZONE_OFFSET).toLong()
                ).toString()
            )
            addHeader("device-no", WatchInfoGlobals.firmwareDeviceIdRing.toString())
        }.build()
    }

    private suspend fun getUpdatedToken(): Flow<Resource<BaseApiResponse<Token>?>> {
        LOGS.d("getting new token")
        val refreshToken = localDataStore.getUserToken()?.refresh_token
        return safeApiCallFlow(Dispatchers.IO) {
            tokenRefreshApi.refreshAccessToken(
                "${BuildConfig.BASE_URL_NEW}/auth_v2/refresh-token",
                "Bearer $refreshToken", "ring",getUserAgent()
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