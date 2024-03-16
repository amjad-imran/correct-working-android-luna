package com.oreo.ui.home.summary.update

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.oreo.data.model.AppUpdateModel
import com.oreo.data.model.OtaUpdateModel
import com.noisefit.data.repository.abstraction.UpdateRepository
import com.noisefit.session.SessionManager
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class UpdateDetailViewModel @Inject constructor(
    var sessionManager: SessionManager,
    val ringDataStore: RingDataStore,
    val localDataStore: DataStoredInterface,
    val updateRepo: UpdateRepository,
    var watchesSDK: WatchesSDK
) : BaseViewModel() {

    var appUpdateInfo = MutableLiveData<AppUpdateModel?>()
    var otaUpdateInfo = MutableLiveData<OtaUpdateModel?>()
    var launchMode: UpdateLaunchMode? = null

    private fun getAppUpdateDetails() {
        val data = localDataStore.getNewAppVersion()
        if (data?.first == null) {
            appUpdateInfo.postValue(null)
            return
        }

        val update = Gson().fromJson<AppUpdateModel>(data.first)
        appUpdateInfo.postValue(update)

    }

    private fun getOtaUpdateDetails() {
        val data = ringDataStore.getNewOtaVersion()
        if (data?.first == null) {
            appUpdateInfo.postValue(null)
            return
        }

        val update = Gson().fromJson<OtaUpdateModel>(data.first)
        otaUpdateInfo.postValue(update)
    }

    fun initLaunchMode(launchMode: UpdateLaunchMode) {
        this.launchMode = launchMode
        when (launchMode) {
            UpdateLaunchMode.APP -> {
                getAppUpdateDetails()
            }

            UpdateLaunchMode.OTA, UpdateLaunchMode.OTA_DEVICE -> {
                getOtaUpdateDetails()
            }
        }
    }

    fun otaRemindLater() {
        updateRepo.otaRemindLater()
    }

    fun appRemindLater() {
        updateRepo.appRemindLater()
    }

}