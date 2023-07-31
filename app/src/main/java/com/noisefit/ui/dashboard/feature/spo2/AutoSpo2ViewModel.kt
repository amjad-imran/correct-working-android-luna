package com.noisefit.ui.dashboard.feature.spo2

import com.noisefit.data.local.AppStaticData
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AutoSpo2ViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager
) : BaseViewModel() {

    var interval: Int = 5
    var intervalValueList: Array<String>? = null

    init {

        intervalValueList = AppStaticData.getTimeFrequency()
    }

    fun getHrValue(): String {
        return "$interval Mins"
    }
}
