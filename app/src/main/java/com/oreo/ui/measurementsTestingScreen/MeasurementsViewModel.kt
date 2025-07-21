package com.oreo.ui.measurementsTestingScreen

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.ManualMeasureType
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.TapMeasureState
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeasurementsViewModel @Inject constructor(
    val sessionManager: SessionManager,
    val ringDataStore: RingDataStore,
    val resourceProvider: ResourcesProvider,
    val userRepository: OreoUserActivityRepository,
): BaseViewModel() {

    val stateStressCard = MutableLiveData<Pair<TapMeasureState, Int?>>()

    val stateHeartRateCard = MutableLiveData<Pair<TapMeasureState, Int?>>()

    val stateBloodOxygen = MutableLiveData<Pair<TapMeasureState, Int?>>()

    val stateBodyTemp = MutableLiveData<Pair<TapMeasureState, Int?>>()

    val stateHrv = MutableLiveData<Pair<TapMeasureState, Int?>>()

    val isMeasuringAny = MutableLiveData(false)

    fun initData(){
        viewModelScope.launch {
            val device = ringDataStore.getRingDevice()

            val measureState = if (device == null) TapMeasureState.NO_DEVICE
                            else TapMeasureState.DEFAULT

            // Stress
            stateStressCard.value = Pair(
                    measureState,
                    null
                )

            // ----

            // Heart Rate
            stateHeartRateCard.value = Pair(
                measureState,
                null
            )

            // bODY Temp
            stateBodyTemp.value = Pair(
                measureState,
                null
            )

            // BloodOxygen
            stateBloodOxygen.value = Pair(
                measureState,
                null
            )

            // HRV
            stateHrv.value = Pair(
                measureState,
                null
            )

        }

    }

    fun measure(status: Boolean, measurementType: ManualMeasureType) {

        val pair = Pair(
            TapMeasureState.MEASURING,
            null
        )

        when(measurementType){
            ManualMeasureType.HEART_RATE -> {
                stateHeartRateCard.postValue(pair)
            }
            ManualMeasureType.BLOOD_OXYGEN -> {
                stateBloodOxygen.postValue(pair)
            }
            ManualMeasureType.STRESS -> {
                stateStressCard.postValue(pair)
            }
            ManualMeasureType.HRV -> {
                stateHrv.postValue(pair)
            }
            ManualMeasureType.BODY_TEMPERATURE -> {
                stateBodyTemp.postValue(pair)
            }
        }

        sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetManualMeasurement(
                measurementType, status
            )
        )

    }

    fun updateManualValue(type: ManualMeasureType) {

        when(type){
            ManualMeasureType.HEART_RATE -> {
                val manualMeasurement = ringDataStore.getManualMeasurementValue()
                if (manualMeasurement != null) {

                    if (manualMeasurement.isError) {
                        stateHeartRateCard.postValue(
                            Pair(
                                TapMeasureState.ERROR,
                                null
                            )
                        )
                        TapMeasureState.ERROR
                    } else {
                        stateHeartRateCard.postValue(
                            Pair(
                                if (manualMeasurement.isMeasuring) {
                                    TapMeasureState.MEASURING
                                } else {
                                    TapMeasureState.LAST_MEASURED
                                },
                                manualMeasurement.value
                            )
                        )
                    }
                }
            }

            ManualMeasureType.BLOOD_OXYGEN -> {
                val manualMeasurement = ringDataStore.getManualMeasurementValueBloodOxygen()
                if (manualMeasurement != null) {

                    if (manualMeasurement.isError) {
                        stateBloodOxygen.postValue(
                            Pair(
                                TapMeasureState.ERROR,
                                null
                            )
                        )
                        TapMeasureState.ERROR
                    } else {
                        stateBloodOxygen.postValue(
                            Pair(
                                if (manualMeasurement.isMeasuring) {
                                    TapMeasureState.MEASURING
                                } else {
                                    TapMeasureState.LAST_MEASURED
                                },
                                manualMeasurement.value
                            )
                        )
                    }
                }
            }

            ManualMeasureType.STRESS -> {
                val manualMeasurement = ringDataStore.getManualMeasurementValueStress()
                if (manualMeasurement != null) {

                    if (manualMeasurement.isError) {
                        stateStressCard.postValue(
                            Pair(
                                TapMeasureState.ERROR,
                                null
                            )
                        )
                    } else {
                        stateStressCard.postValue(
                            Pair(
                                if (manualMeasurement.isMeasuring) {
                                    TapMeasureState.MEASURING
                                } else {
                                    TapMeasureState.LAST_MEASURED
                                },
                                manualMeasurement.value
                            )
                        )
                    }
                }
            }

            ManualMeasureType.HRV -> {
                val manualMeasurement = ringDataStore.getManualMeasurementValueHrv()
                if (manualMeasurement != null) {

                    if (manualMeasurement.isError) {
                        stateHrv.postValue(
                            Pair(
                                TapMeasureState.ERROR,
                                null
                            )
                        )
                        TapMeasureState.ERROR
                    } else {
                        stateHrv.postValue(
                            Pair(
                                if (manualMeasurement.isMeasuring) {
                                    TapMeasureState.MEASURING
                                } else {
                                    TapMeasureState.LAST_MEASURED
                                },
                                manualMeasurement.value
                            )
                        )
                    }
                }
            }

            ManualMeasureType.BODY_TEMPERATURE -> {
                val manualMeasurement = ringDataStore.getManualMeasurementValueBodyTemp()
                if (manualMeasurement != null) {

                    if (manualMeasurement.isError) {
                        stateBodyTemp.postValue(
                            Pair(
                                TapMeasureState.ERROR,
                                null
                            )
                        )
                        TapMeasureState.ERROR
                    } else {
                        stateBodyTemp.postValue(
                            Pair(
                                if (manualMeasurement.isMeasuring) {
                                    TapMeasureState.MEASURING
                                } else {
                                    TapMeasureState.LAST_MEASURED
                                },
                                manualMeasurement.value
                            )
                        )
                    }
                }
            }
        }

    }

}