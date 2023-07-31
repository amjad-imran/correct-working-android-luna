package com.noisefit.ui.settings.setting

import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.models.Units
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val METRIC = "Metric (kg)"
private const val IMPERIAL = "Imperial (lbs)"

@HiltViewModel
class SettingViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val deviceRepository: DeviceRepository,
    val sessionManager: SessionManager
) : BaseViewModel() {

    var unit = localDataStore.getUnit()
    var unitList = ArrayList<String>()

    init {
        //unit list creation
        unitList.add(METRIC)
        unitList.add(IMPERIAL)

    }

    fun getSelectedUnit(): String {
        if (unit == Units.METRIC) {
            return METRIC
        }
        return IMPERIAL
    }

    fun setSelectedUnit(selectedUnit: String) {
        unit = if (selectedUnit.equals(METRIC, false)) {
            Units.METRIC
        } else {
            Units.IMPERIAL
        }
      //  localDataStore.saveUnit(unit)
    }

}