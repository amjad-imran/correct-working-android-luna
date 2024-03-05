package com.oreo.ui.home.summary.update

import androidx.lifecycle.MutableLiveData
import com.noisefit.data.model.AppUpdateModel
import com.noisefit.data.model.OtaUpdateModel
import com.noisefit.data.repository.abstraction.DownloadRepository
import com.noisefit.session.SessionManager
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class UpdateDetailViewModel @Inject constructor(
    var sessionManager: SessionManager,
    var watchesSDK: WatchesSDK
) : BaseViewModel() {

    var appUpdateInfo = MutableLiveData<AppUpdateModel?>()
    var otaUpdateInfo = MutableLiveData<OtaUpdateModel?>()
    var launchMode: UpdateLaunchMode? = null

    private fun getAppUpdateDetails() {
    }

    private fun getOtaUpdateDetails() {

    }

    fun initLaunchMode(launchMode: UpdateLaunchMode) {
        this.launchMode = launchMode
        when (launchMode) {
            UpdateLaunchMode.APP -> {
                getAppUpdateDetails()
            }

            UpdateLaunchMode.OTA -> {
                getOtaUpdateDetails()
            }
        }
    }

}