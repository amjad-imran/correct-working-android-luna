package com.noisefit.receiver.workManager

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.hilt.work.HiltWorker
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.WeatherRepository
import com.noisefit.session.SessionManager
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.converter.WeatherConverter
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.Location
import com.noisefit_commans.models.LocationDataModel
import com.noisefit_commans.models.SwitchSetting
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LocationClientClass
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@HiltWorker
class WeatherWork
@AssistedInject
constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val localDataStore: DataStoredInterface,
    private val weatherRepository: WeatherRepository,
    private val sessionManager: SessionManager,
    private val geoCoder: Geocoder
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val LOCATION_BROADCAST_RECEIVER = "LOCATION_BROADCAST_RECEIVER"
        const val LAT_LONG = "LAT_LONG"
    }

    private val TAG = "WeatherWork"

    private var locationClientClass: LocationClientClass? = null


    private val workScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext =
            Dispatchers.IO // no job added i.e + SupervisorJob()
    }


    override suspend fun doWork(): Result {
        LOGS.d(TAG, "Running Weather Scheduler")

        sessionManager.connectState.value?.let { connectState ->
            if (connectState is ConnectState.ConnectSuccess) {
                getLocation()
            }
        }


        return Result.success()
    }


    private fun hasBackgroundLocationPermission(): Boolean {
        val permissionAccessCoarseLocationApproved =
            (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)

        val backgroundLocationPermissionApproved =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
            } else {
                true
            }

        if (permissionAccessCoarseLocationApproved && backgroundLocationPermissionApproved) {
            return true
        }

        return false

    }

    private suspend fun getLocation() {
        if (hasBackgroundLocationPermission()) {
            enableLocation()
        } else {
            getWeatherData()
        }
    }

    private fun enableLocation() {
        locationClientClass = LocationClientClass()
        NoisefitApplication.context?.let {
            locationClientClass?.initialize(it)
            locationClientClass?.requestLocationUpdates(it)
            LocalBroadcastManager
                .getInstance(it)
                .registerReceiver(
                    locationReceiver,
                    IntentFilter(LOCATION_BROADCAST_RECEIVER)
                )
        }
    }

    private var locationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val locationArrayList =
                intent.getParcelableArrayListExtra<LocationDataModel>(
                    LAT_LONG
                )
            if (locationArrayList.isNullOrEmpty()) {
                return
            }
            val location = locationArrayList.last()
            getAddress(location.latitude, location.longitude)
            workScope.launch {
                LOGS.d("$TAG disable location")
                disableLocation()
            }

        }
    }

    fun getAddress(latitude: Double, longitude: Double) {

        workScope.launch {
            val addresses: List<Address?> = try {
                val result = kotlin.runCatching { geoCoder.getFromLocation(latitude, longitude, 1) }
                if (result.isSuccess) {
                    result.getOrNull() ?: ArrayList()
                } else {
                    ArrayList()
                }
            } catch (e: Exception) {
                ArrayList()
            }
            if (addresses.isNotEmpty()) {
                val address = addresses[0]!!
                val locationString = ApplicationUtils.getLocationString(address)
                localDataStore.updateLocations(
                    Location(
                        latitude,
                        longitude,
                        address,
                        locationString
                    )
                )
                LOGS.d(TAG, "getAddress : $locationString")
            } else {
                localDataStore.updateLocations(
                    Location(
                        latitude,
                        longitude,
                        null,
                        ""
                    )
                )
            }

            getWeatherData()


        }
    }

    private fun disableLocation() {
        NoisefitApplication.context?.let {
            locationClientClass?.removeLocationUpdates(it)
            LocalBroadcastManager
                .getInstance(it)
                .unregisterReceiver(locationReceiver)
        }
    }


    private suspend fun getWeatherData() {

        val location = localDataStore.getLocation() ?: return
        val temperatureUnit = localDataStore.getTemperatureUnit()

        weatherRepository.getWeatherData(
            location.lat,
            location.lng,
            temperatureUnit.name.lowercase()
        )
            .collect { resource ->
                when (resource) {
                    is Resource.NetworkError -> {

                        localDataStore.saveLastWeatherSyncTimeStamp(DateFormats.getTimeStamp())
                    }
                    is Resource.Success -> {

                        localDataStore.saveLastWeatherSyncTimeStamp(DateFormats.getTimeStamp())
                        resource.data?.let {
                            LOGS.d(
                                TAG,
                                "WeatherWork  Settings Weather data : ${location.addressString}"
                            )
                            sessionManager.sendUpdateQueryAction(
                                UpdateDeviceAction.SetWeatherSwitch(
                                    SwitchSetting(
                                        status = true
                                    )
                                )
                            )

                            val weatherPair = WeatherConverter.parseWeatherData(
                                it,
                                temperatureUnit.name.lowercase(),
                                location.address,
                                location.addressString
                            )

                            sessionManager.sendUpdateQueryAction(
                                UpdateDeviceAction.SetWeatherData(
                                    weatherPair.first,
                                    temperatureUnit.name.lowercase()
                                )
                            )
                            sessionManager.sendUpdateQueryAction(
                                UpdateDeviceAction.SetWeatherDataHourly(
                                    weatherPair.first,
                                    weatherPair.second,
                                    temperatureUnit.name.lowercase()
                                )
                            )

//                            sessionManager.connectState.value?.let { connectState ->
//                                if (connectState is ConnectState.ConnectSuccess) {
                            localDataStore.saveLastWeatherSyncTimeStamp(DateFormats.getTimeStamp())
//                                }
//                            }

                        }
                    }
                    else -> {}
                }
            }
    }

}