package com.oreo.ui.googlefit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.model.GoogleFitDataDb
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.db.abstaction.GoogleFitDataSource
import com.oreo.data.model.GoogleFitDataDisplayModel
import com.oreo.data.model.GoogleFitDataType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoogleFitDataViewModel @Inject constructor(
    private val googleFitDataSource: GoogleFitDataSource
) : BaseViewModel() {

    var unSyncedDataList = MutableLiveData<List<GoogleFitDataDisplayModel>>()
        private set

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {

            val data = googleFitDataSource.getUnSyncedData()
            unSyncedDataList.postValue(convertData(data))
        }

    }

    private fun convertData(data: List<GoogleFitDataDb>): List<GoogleFitDataDisplayModel> {

        val result = ArrayList<GoogleFitDataDisplayModel>()

        data.forEach {

            //workout, sleep, height, weight, body_fat

            val type = if (it.type.equals("workout", true)) {
                GoogleFitDataType.WORKOUT
            } else if (it.type.equals("sleep", true)) {
                val duration = it.endTime - it.startTime
                if (duration < (3 * 60 * 60)) {
                    GoogleFitDataType.NAP
                } else {
                    GoogleFitDataType.SLEEP
                }
            } else {
                null
            }

            if (type != null) {
                /*val data = if(type==GoogleFitDataType.WORKOUT){
                    Gson().fromJson<WorkoutGoogleFit>(it.data?:"")
                }else{
                    Gson().fromJson<SleepDataGoogleFit>(it.data?:"")
                }*/
                result.add(
                    GoogleFitDataDisplayModel(
                        type = type,
                        startTime = it.startTime,
                        endTime = it.endTime,
                        duration = it.endTime - it.startTime,
                        rawData = it.data ?: "",
                    )
                )
            }
        }

        return result
    }

    val success = ArrayList<GoogleFitDataDisplayModel>()
    val fail = ArrayList<GoogleFitDataDisplayModel>()

    fun sendDataToServer(selectedItems: List<GoogleFitDataDisplayModel>) {

    }


}
