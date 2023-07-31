package com.noisefit.ui.dashboard.feature.experimental

import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.ExperimentalSettings
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExperimentalFeaturesViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface
) : BaseViewModel() {

    var experimentalSettings = ExperimentalSettings()

    init {
        experimentalSettings = localDataStore.getExperimentalSettings()
    }

    fun saveExperimentalSettings() {
        localDataStore.setExperimentalSettings(experimentalSettings)
    }


}