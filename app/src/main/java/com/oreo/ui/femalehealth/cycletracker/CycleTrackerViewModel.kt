package com.oreo.ui.femalehealth.cycletracker

import com.google.gson.Gson
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.FMHCycleHistoryDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class CycleTrackerViewModel @Inject constructor() : BaseViewModel() {
    fun getCycleHistoryData(): ArrayList<FMHCycleHistoryDataModel> {
        val listData = ArrayList<FMHCycleHistoryDataModel>()
        val response = "{\n" +
                "                \"period_date\":\"2024-05-05\",\n" +
                "                \"cycle_length\":28,\n" +
                "                \"period_length\":5,\n" +
                "                \"fertile_window\":\"2024-05-14 / 2024-05-20\",\n" +
                "                \"ovulation_start_date\":\"2024-05-19\"\n" +
                "            }"


        for (i in 1..3) {
            listData.add(Gson().fromJson<FMHCycleHistoryDataModel>(response))
        }
        return listData
    }

    fun loadPageData(selectedDate: LocalDate?) {
        //call api based on data
    }
}