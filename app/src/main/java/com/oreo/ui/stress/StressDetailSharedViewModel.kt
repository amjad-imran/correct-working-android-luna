package com.oreo.ui.stress

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.enums.StressType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.oreo.data.model.ChartModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StressDetailSharedViewModel @Inject
constructor(
    val localDataStore: DataStoredInterface
) : BaseViewModel() {

    val selectedStressLevel = MutableLiveData<StressType>()

    fun setSelectedType(type: StressType) {
        val lastValue = selectedStressLevel.value
        if (lastValue != type) {
            selectedStressLevel.value = type
        }
    }

    fun getPrefixAndSuffixList(dataList: List<String>): Triple<ArrayList<ChartModel>, ArrayList<ChartModel>, ArrayList<ChartModel>> {
        val list = java.util.ArrayList<ChartModel>()
        dataList.forEach {
            val chartModel = ChartModel()
            chartModel.date = it
            var currentDayText = ""
            if (it == DateFormats.getCurrentDate(DateFormats.dateFormat3)) {
                currentDayText = "Today, "
            }
            val formattedDate = if (currentDayText.isEmpty()) {
                DateFormats.getOrdinalDate(
                    it,
                    DateFormats.dateFormat3
                )
            } else {
                DateFormats.getOrdinalDateToday(
                    it,
                    DateFormats.dateFormat3,
                )
            }
            chartModel.formattedDate = "$currentDayText$formattedDate"
            chartModel.index = ""
            chartModel.value = 0
            list.add(chartModel)
        }

        list.reverse()
        val lastDateFromList = dataList.first()
        val lastDate = DateFormats.subtractDateFormat3(lastDateFromList, 1)!!
        val suffixDatesList = DateFormats.getWeekDaysBetweenDates(
            DateFormats.subtractDateFormat3(lastDate, 14)!!, lastDate,
            DateFormats.dateFormat3, DateFormats.singleWeekDay
        )
        val currentDateFromList = dataList.last()
        val currentDate = DateFormats.addDateFormat3(currentDateFromList, 1)!!
        val prefixDatesList = DateFormats.getWeekDaysBetweenDates(
            currentDate,
            DateFormats.addDateFormat3(currentDate, 14)!!,
            DateFormats.dateFormat3, DateFormats.singleWeekDay
        )

        val suffix = java.util.ArrayList<ChartModel>()
        suffixDatesList.forEach {
            val chartModel = ChartModel()
            chartModel.index = it
            chartModel.date = ""
            chartModel.value = 0
            suffix.add(chartModel)
        }

        suffix.reverse()


        val prefix = java.util.ArrayList<ChartModel>()
        prefixDatesList.forEach {
            val chartModel = ChartModel()
            chartModel.index = it
            chartModel.date = ""
            chartModel.value = 0
            prefix.add(chartModel)
        }
        prefix.reverse()
        return Triple(list, suffix, prefix)
    }

    var lastSelectedStressType: StressType = StressType.CALM


}