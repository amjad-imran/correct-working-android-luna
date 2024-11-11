package com.oreo.ui.heartrate

import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.ChartModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HeartRateViewModel @Inject constructor(
    private val resourcesProvider: ResourcesProvider
) : BaseViewModel() {

    fun getPrefixAndSuffixList(dataList: List<String>): Triple<ArrayList<ChartModel>, ArrayList<ChartModel>, ArrayList<ChartModel>> {
        val list = java.util.ArrayList<ChartModel>()
        dataList.forEach {
            val chartModel = ChartModel()
            chartModel.date = it
            var currentDayText = ""
            if (it == DateFormats.getCurrentDate(DateFormats.dateFormat3())) {
                currentDayText = resourcesProvider.getString(R.string.text_today_comma)
            }
            val formattedDate = if (currentDayText.isEmpty()) {
                DateFormats.getOrdinalDate(
                    it,
                    DateFormats.dateFormat3()
                )
            } else {
                DateFormats.getOrdinalDateToday(
                    it,
                    DateFormats.dateFormat3(),
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
            DateFormats.dateFormat3(), DateFormats.singleWeekDay()
        )
        val currentDateFromList = dataList.last()
        val currentDate = DateFormats.addDateFormat3(currentDateFromList, 1)!!
        val prefixDatesList = DateFormats.getWeekDaysBetweenDates(
            currentDate,
            DateFormats.addDateFormat3(currentDate, 14)!!,
            DateFormats.dateFormat3(), DateFormats.singleWeekDay()
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
}