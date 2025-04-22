package com.noisefit.ui.onboarding.pairing.find

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.response.RingInfoResponse
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.ColorFitNetworkDevice
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.RingInfo
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.RingSerialNoParser
import com.noisefit_commans.utils.bleUtils.CRPScanRecordInfo.McuPlatform
import com.noisefit_commans.utils.bleUtils.DeviceEntity
import com.noisefit_commans.utils.bleUtils.DeviceScanQrCodeBean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchNearbyDeviceViewModel
@Inject
constructor(
    private val deviceRepository: DeviceRepository,
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager
) : BaseViewModel() {

    var selectedColorFitDevice: ColorFitDevice? = null

    var tempColorFitDevice: ColorFitDevice? = null
    private val _deviceList = MutableLiveData<List<ColorFitNetworkDevice>>()
    private val ringInfo = MutableLiveData<List<RingInfoResponse>?>()
    private val _scannedDeviceList = MutableLiveData<List<ColorFitDevice>>()
    private val _startBluetoothScan = MutableLiveData<Event<Boolean>>()
    private val _watchToken = MutableLiveData<Event<String>>()

    private val _startDisconnectProcess = MutableLiveData<Event<ColorFitDevice>>()
    private val _startConnectionProcess = MutableLiveData<Event<ColorFitDevice>>()

    fun getWatchToken(): LiveData<Event<String>> = _watchToken
    fun getDevices(): LiveData<List<ColorFitNetworkDevice>> = _deviceList
    fun getScannedDevices(): LiveData<List<ColorFitDevice>> = _scannedDeviceList
    fun getStartBluetoothScan(): LiveData<Event<Boolean>> = _startBluetoothScan

    fun getDisconnectProcessEvent(): LiveData<Event<ColorFitDevice>> = _startDisconnectProcess
    fun getConnectionProcessEvent(): LiveData<Event<ColorFitDevice>> = _startConnectionProcess

    var previousDevice: ColorFitDevice? = null

    var findDeviceState = FindDeviceState.DEFAULT

    var forcedQRScanning = true
    var deviceFound = false
    val scanDeviceTime = 30 * 1000L
    val qrScanDeviceTime = 30 * 1000L

    var isBLEScanning = false
//    var troubleShootData: String? = null

    var troubleshootScrPos = 0

    fun deviceScanQrCodeBean(data: String?): String? {
        //http:...?radio=d855eb6b43c8384e010209000000&random=016260&name=E15_43C8";
        //http:...?radio=e72415133d083075010206340000d82415133d08&random=804545;
        if (data == null) return null
        val mUri = Uri.parse(data)

        // 根据参数的 key，取出相应的值
        if (isUriExistParam(mUri, "radio")) {
            val radio = mUri.getQueryParameter("radio")!!.trim { it <= ' ' }
            LOGS.d("======参数===radio ==$radio")
            return DeviceScanQrCodeBean.DeviceRadioBroadcastBean(radio).deviceMac
        }
        return null
    }

    private fun isUriExistParam(uri: Uri, param: String): Boolean {
        return uri.getQueryParameter(param) != null
    }

    fun clearScannedDeviceList() {
        _scannedDeviceList.value = ArrayList()
    }


    fun checkWatchTokenExist(macAddress: String) {

        viewModelScope.launch {
            deviceRepository.checkWatchTokenExist(macAddress).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        checkWatchTokenExist(macAddress)
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            if (it.ringToken.isNullOrEmpty()) {
                                _watchToken.postValue(Event(""))
                            } else {
                                _watchToken.postValue(Event(it.ringToken))
                            }

                        }
                    }
                }
            }

        }
    }

    fun fetchDeviceList() {
        val dType: String =
            "ring"

        if (!getDevices().value.isNullOrEmpty()) {
            _deviceList.postValue(getDevices().value)
            _startBluetoothScan.postValue(Event(true))
            return

        }

        sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_fetch_device_start)
        viewModelScope.launch {
            deviceRepository.getDeviceList(dType).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_fetch_device_ge)
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_fetch_device_ne)
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        fetchDeviceList()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_fetch_device_complete)
                        resource.data?.data?.let {
                            ringInfo.value = (it.ringInfo)
                            _deviceList.postValue(it.devices)
//                            addWatch(it.devices)

                            _startBluetoothScan.postValue(Event(true))
                        }
                    }
                }
            }

        }
    }

//    private fun addWatch(devices: List<ColorFitNetworkDevice>) {
//        val list = ArrayList<ColorFitNetworkDevice>()
//        list.addAll(devices)
//        list.add(
//            ColorFitNetworkDevice(
//                DeviceType.NOISEFIT_RX.deviceName,
//                DeviceType.NOISEFIT_RX.deviceType,
//                0,
//                120,
//                DeviceType.NOISEFIT_RX.deviceName,
//                "",
//                "pattern"
//            )
//        )
//        /*list.add(
//            ColorFitNetworkDevice(
//                DeviceType.NOISEFIT_COLOR_FIT.deviceName,
//                DeviceType.NOISEFIT_COLOR_FIT.deviceType,
//                0,
//                118,
//                DeviceType.NOISEFIT_COLOR_FIT.deviceName,
//                "",
//                "exact"
//            )
//        )*/
//
//        LOGS.d("device_list", Gson().toJson(list))
//
//    }

    fun onDeviceFound(scannedDevice: DeviceEntity) {


        val result = getDeviceType(scannedDevice) ?: return

        result.first?.let {

            var mcuPlatform: McuPlatform? = null
            if (scannedDevice.mcuPlatform != null) {
                mcuPlatform = scannedDevice.mcuPlatform

            }
            val colorFitDevice = ColorFitDevice(
                bluetoothName = result.second.bluetoothName,
                address = scannedDevice.address,
                rssi = scannedDevice.rssi,
                deviceType = it.deviceType,
                url = result.second.url,
                deviceId = result.second.id,
                isSupportHeadset = scannedDevice.mDeviceRadioBroadcastBean?.isSupportHeadset
                    ?: false,
                headsetMac = scannedDevice.mDeviceRadioBroadcastBean?.headsetMac ?: "",
                isBind = scannedDevice.mDeviceRadioBroadcastBean?.isBind ?: false,
                mcuPlatform = mcuPlatform?.name
            )


            colorFitDevice.apply {
                this.ringInfo =
                    getRingInfo(scannedDevice.mDeviceRadioBroadcastBean?.serialNumber)


            }


            val deviceList = if (_scannedDeviceList.value == null) {
                ArrayList()
            } else {
                _scannedDeviceList.value as ArrayList<ColorFitDevice>
            }

            if (!checkIfPresent(deviceList, colorFitDevice)) {
                deviceList.add(colorFitDevice)

                if (previousDevice != null) {
                    if (previousDevice!!.address.equals(colorFitDevice.address, true)) {
                        previousDevice = null
                        if (_startDisconnectProcess.value == null) {
                            _startDisconnectProcess.value = Event(colorFitDevice)
                        }
                    }
                }
            }
            _scannedDeviceList.postValue(deviceList)

        }

    }

    private fun getRingInfo(serialNo: String?): RingInfo? {
        if (serialNo.isNullOrEmpty()) return null
        if (ringInfo.value == null) return null

        val serial = serialNo.replace(
            ":",
            ""
        )
        val convertedSerialNo = RingSerialNoParser().convertSerialNo(serial)
        LOGS.w("INPUT : $serial convertedSerialNo $convertedSerialNo")

        val code = try {
            convertedSerialNo.substring(7, 9).toInt()
        } catch (exp: Exception) {
            0
        }
        if (code == 0) {
            return null
        }

        var ringInfoRes: RingInfo? = null

        ringInfo.value?.forEach outer@{ info ->
            info.mapping.forEach { mapping ->
                if (mapping.code == code) {
                    ringInfoRes = RingInfo(
                        size = mapping.size,
                        color = info.color,
                        image = info.imageUrl,
                        image2 = info.imageUrl2,
                        serialNoRaw = convertedSerialNo
                    )
                    return@outer
                }
            }
        }
        return ringInfoRes

    }

    private fun addDevice(device: java.util.ArrayList<ColorFitDevice>): java.util.ArrayList<ColorFitDevice> {
        device.sortWith { o1, o2 ->
            val num1: Int = o1.rssi
            val num2: Int = o2.rssi
            var aa = 0
            if (num2 > num1) {
                aa = num2 - num1
            }
            if (num2 < num1) {
                aa = num2 - num1
            }
            aa
        }
        return device
    }

    private fun checkIfPresent(
        deviceList: java.util.ArrayList<ColorFitDevice>,
        colorFitDevice: ColorFitDevice
    ): Boolean {
        deviceList.forEach {
            if (it.address.equals(colorFitDevice.address, true)) {
                return true
            }
        }
        return false
    }


    fun getDeviceType(scannedDevice: DeviceEntity): Pair<DeviceType?, ColorFitNetworkDevice>? {

        if (_deviceList.value != null) {
            _deviceList.value!!.forEach { device ->
                if (scannedDevice.name.isNullOrEmpty()) {
                    return null
                }
                if (device.namePattern.isNullOrEmpty() || device.matchingType.isNullOrEmpty()) {
                    return@forEach
                }

                device.namePattern.split(";").forEach { pattern ->
                    if (device.matchingType == "exact") {
                        if (scannedDevice.name!!.lowercase() == pattern.lowercase()) return Pair(
                            DeviceType.findDeviceType(device.deviceType!!),
                            device
                        )
                    } else if (device.matchingType == "exact_pattern") {
                        val devName = scannedDevice.name?.dropLast(5)
                        if (devName?.lowercase() == pattern.lowercase()) return Pair(
                            DeviceType.findDeviceType(device.deviceType!!),
                            device
                        )
                    } else if (device.matchingType == "pattern") {
                        if (scannedDevice.name!!.contains(pattern, true)) {
                            return Pair(
                                DeviceType.findDeviceType(device.deviceType!!),
                                device
                            )
                        }
                    }
                }
            }
        }
        return null
    }


    fun userId(): String {
        return localDataStore.getUser()?.id.toString()
    }

    fun isProfileSetupComplete(): Boolean {
        val user = localDataStore.getUser() ?: return false
        if (user.userInfo?.dob.isNullOrEmpty() || (user.userInfo?.height
                ?: 0) == 0 || (user.userInfo?.weight ?: 0) == 0 || (user.userGoals?.caloriesGoal
                ?: 0) == 0
        ) {
            return false
        }
        return true
    }

    fun setPairLaterClicked(b: Boolean) {
        localDataStore.setPairLaterClicked(b)
    }

}

enum class FindDeviceState {
    DEFAULT, SEARCHING, FINISHED
}