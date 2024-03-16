package com.oreo.ui.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.oreo.data.model.OtaUpdateModel
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UpdateRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.DateFormats.checkTimeDifferenceMoreThanN
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OAboutDeviceViewModel
@Inject
constructor(
    val ringDataStore: RingDataStore,
    val sessionManager: SessionManager,
    val ringDataSore: RingDataStore,
    val updateRepository: UpdateRepository
) : BaseViewModel() {


    var noUpdateAvailable = MutableLiveData<Event<Boolean>>()
    var otaUpdateInfo = MutableLiveData<Event<OtaUpdateModel?>>()
    fun checkOtaVersionServer(pair: Pair<Int, Int>) {
        viewModelScope.launch(Dispatchers.IO) {

            val request = getOtaVersionRequest(pair)
            updateRepository.checkAppVersionV2(request).collect { resource ->
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
                                        checkOtaVersionServer(pair)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            updateRepository.saveNewOtaVersion(it.firmwareVersion, pair.first)
                            if (it.firmwareVersion == null) {
                                noUpdateAvailable.postValue(Event(true))
                            }

                            postUpdateOtaDataOffline()
                        }
                    }


                }
            }
        }


    }

    private fun postUpdateOtaDataOffline(): Boolean {
        val firmwareObj = ringDataStore.getNewOtaVersion()
        if (firmwareObj?.first != null) {

            val lastSaveTimeStamp = firmwareObj.third

            val isMoreThan2 = lastSaveTimeStamp.checkTimeDifferenceMoreThanN(2)
            if (isMoreThan2) {
                ringDataStore.cleaNewOtaVersion()
                return true
            }

            val remindDate = ringDataStore.getOtaRemindDate()

            if (remindDate == null) {
                val obj = Gson().fromJson<OtaUpdateModel>(firmwareObj.first)
                otaUpdateInfo.postValue(Event(obj))
            } else if (!remindDate.equals(DateFormats.getCurrentDate())) {
                val obj = Gson().fromJson<OtaUpdateModel>(firmwareObj.first)
                otaUpdateInfo.postValue(Event(obj))
            }
            return false
        } else {
            val lastCheckTimestamp = ringDataStore.getOtaVersionCheckTimeStamp()
            LOGS.d(
                "" +
                        " ${lastCheckTimestamp}"
            )
            return if (lastCheckTimestamp == 0L) {
                true
            } else {
                lastCheckTimestamp.checkTimeDifferenceMoreThanN(2)
            }
        }
    }

    private fun getOtaVersionRequest(pair: Pair<Int, Int>): JsonObject {
        val ringDevice = ringDataStore.getRingDevice()
        val deviceType = ringDevice?.deviceType

        return JsonObject().apply {
            addProperty(
                "version",
                pair.first
            )
            addProperty(
                "firmware_id",
                pair.second
            )
            addProperty("mac", ringDevice?.address)
            addProperty("device_type", deviceType)
            addProperty(
                "isOTARequired",
                sessionManager.needDfuUpdate.value?.peekContent() ?: false
            )
            addProperty("platform", "android")
        }
    }

}