package com.oreo.ui.home.summary.update

import androidx.lifecycle.MutableLiveData
import com.noisefit.data.model.AppUpdateModel
import com.noisefit.data.model.OtaUpdateModel
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel


@HiltViewModel
class UpdateDetailViewModel : BaseViewModel() {

    var appUpdateInfo = MutableLiveData<AppUpdateModel?>()
    var otaUpdateInfo = MutableLiveData<OtaUpdateModel?>()
    var launchMode: UpdateLaunchMode? = null

    private fun getAppUpdateDetails() {
    }

    private fun getOtaUpdateDetails() {

    }

    fun setLaunchMode(launchMode: UpdateLaunchMode) {
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