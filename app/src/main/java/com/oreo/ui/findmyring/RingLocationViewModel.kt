package com.oreo.ui.findmyring

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.os.Build
import android.text.format.DateUtils
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.model.RingLocationData
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.OHealthOverview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import javax.inject.Inject


@HiltViewModel
class RingLocationViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
    val watchDataStore: WatchDataStore,
    val geoCoder: Geocoder,
    private val ringDataStore: RingDataStore
) : BaseViewModel() {

    var isInitialMove: Boolean = false

    var isDataHidden: Boolean = false

    val ringLocationData = MutableLiveData<RingLocationData?>()


    fun getRingLastLocation() {
        viewModelScope.launch {
            val mac = ringDataStore.getRingDevice()?.address
            if (mac.isNullOrEmpty()) return@launch
            userRepository.getRingLastLocation(mac).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object :
                                    BinaryActionCallback {
                                    override fun yes() {
                                        getRingLastLocation()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            setLoading(true)
                            getAddress(it.latitude, it.longitude) { address ->
                                setLoading(false)
                                ringLocationData.postValue(it.apply {
                                    this.address = address
                                })
                            }
                        }
                    }
                }
            }
        }


    }

    fun getUserName(): String {
        val name = localDataStore.getUser()?.firstName
        return if (name.isNullOrEmpty()) "" else "$name's "
    }

    fun getRingImage(): String? {
        val connectedDevice = ringDataStore.getRingDevice()
        return connectedDevice?.ringInfo?.image
    }


    fun getAddress(lat: Double?, long: Double?, onAddressFetched: (String?) -> Unit) {
        if (lat == null || long == null) {
            onAddressFetched(null)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geoCoder.getFromLocation(
                lat, long, 1
            ) { addresses ->
                val address = addresses.getOrNull(0)

                //LOGS.d("sdfjshdkfjh $address")
                viewModelScope.launch(Dispatchers.Main) {
                    onAddressFetched(address?.getAddressLine(0))
                }
            }
        } else {
            val addresses = geoCoder.getFromLocation(lat, long, 1)
            val address = addresses?.getOrNull(0)
            viewModelScope.launch(Dispatchers.Main) {
                onAddressFetched(address?.getAddressLine(0))
            }
//            onAddressFetched(address?.locality)
        }
    }

    fun updateRingLocation(location: Pair<Double, Double>) {
        viewModelScope.launch {
            val mac = ringDataStore.getRingDevice()?.address
            val batteryPercent = watchDataStore.getBatteryPercentRing()
            setLoading(true)

            getAddress(location.first, location.second) { address ->
                setLoading(false)
                ringLocationData.postValue(
                    RingLocationData(
                        location.first,
                        location.second,
                        batteryPercent,
                        DateFormats.getCurrentDate(DateFormats.dateTimeFormat5()),
                        address
                    )
                )
            }


            val request = JsonObject().apply {
                this.addProperty("latitude", location.first)
                this.addProperty("longitude", location.second)
                this.addProperty("battery_percentage", batteryPercent)
                this.addProperty("mac_address", mac)
            }

            userRepository.setRingLastLocation(
                request
            ).collect { resource ->
                when (resource) {

                    is Resource.Success -> {
                        resource.data?.data?.let {

                        }
                    }

                    else -> {}
                }
            }
        }
    }

    fun getBitmapFromLayout(context: Activity, view: View): Bitmap? {
        try {
            val displayMetrics = DisplayMetrics()
            context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
            view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
            view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
            view.buildDrawingCache()
            val bitmap =
                Bitmap.createBitmap(
                    view.measuredWidth,
                    view.measuredHeight,
                    Bitmap.Config.ARGB_8888
                )

            val canvas = Canvas(bitmap)
            view.draw(canvas)
            return bitmap
        } catch (exp: Exception) {
            return null
        }
    }

    fun formatRelativeTime(timestamp: Long): String {
        val currentTimeStamp = System.currentTimeMillis()

        val relativeTime = if (timestamp + 60000 > currentTimeStamp) {
            "Just Now"
        } else {
            if (timestamp + 24 * 60 * 60 * 1000 >= currentTimeStamp) {
                val totalSecs = (currentTimeStamp - timestamp) / 1000
                val hours = totalSecs / 3600;
                val minutes = (totalSecs % 3600) / 60;

                if (hours == 0L) {
                    if (minutes == 1L) {
                        String.format("%02d min ago", minutes);
                    } else {
                        String.format("%02d mins ago", minutes);
                    }
                } else {
                    String.format("%02d hr %02d mins ago", hours, minutes);
                }
            } else {
                val dt = Instant.ofEpochMilli(timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()

                "at ${dt.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))} at ${
                    dt.format(
                        DateTimeFormatter.ofPattern("hh:mm a")
                    )
                }"
            }
        }
        return relativeTime
    }

}