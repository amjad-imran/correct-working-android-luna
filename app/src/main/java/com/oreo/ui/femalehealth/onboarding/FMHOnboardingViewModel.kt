package com.oreo.ui.femalehealth.onboarding


import androidx.lifecycle.MutableLiveData
import com.noisefit.data.model.DiagnoseDataItem
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.WheelItem
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject


const val DefaultPeriodDays = 7
const val MinPeriodDays = 1
const val MaxPeriodDays = 45

@HiltViewModel
class FMHOnboardingViewModel @Inject constructor() : BaseViewModel() {
    val fragmentSize = 7

    var isGoalSelected = MutableLiveData<Event<Boolean>>()
    private var pDays = 0
    private var pcDays = 0

    private var periodDays = ArrayList<Int>()
    private var periodCycleDays = ArrayList<Int>()
    var goalTypeSelected: String = ""
    var selectedDiagnoseListData = ArrayList<String>()
    var selectedHormoneListData = ArrayList<String>()
    val updateFMHDate = MutableLiveData<Event<Boolean>>()
    var selectedEndDate: LocalDate? = null

    init {
        for (i in MinPeriodDays..MaxPeriodDays) {
            periodDays.add(i)
            periodCycleDays.add(i)
        }
    }

    fun getPeriodDayData(): ArrayList<WheelItem<String>> {
        val dataSet = ArrayList<WheelItem<String>>()
        periodDays.forEach {
            dataSet.add(WheelItem("$it days"))
        }
        return dataSet

    }

    fun getPeriodCycleDayData(): ArrayList<WheelItem<String>> {
        val dataSet = ArrayList<WheelItem<String>>()
        periodCycleDays.forEach {
            dataSet.add(WheelItem("$it days"))
        }
        return dataSet

    }

    fun getPeriodDayIndex(): Int {
        if (pDays == 0) {
            pDays = DefaultPeriodDays
        }
        return periodDays.indexOf(pDays)

    }

    fun getPeriodCycleDayIndex(): Int {
        if (pcDays == 0) {
            pcDays = DefaultPeriodDays
        }
        return periodCycleDays.indexOf(pcDays)

    }

    fun updatePeriodDayIndex(index: Int) {
        pDays = getPeriodDay(index)
    }

    fun updatePeriodCycleDayIndex(index: Int) {
        pcDays = getPeriodCycleDay(index)
    }

    private fun getPeriodDay(index: Int): Int {
        return periodDays[index]
    }

    private fun getPeriodCycleDay(index: Int): Int {
        return periodCycleDays[index]
    }

    fun getHormonalData(): ArrayList<DiagnoseDataItem> {
        val listData = ArrayList<DiagnoseDataItem>()
        listData.add(DiagnoseDataItem("None", false))
        listData.add(DiagnoseDataItem("Fertility treatments", false))
        listData.add(DiagnoseDataItem("Hormone replacement therapy", false))
        listData.add(DiagnoseDataItem("Hormonal contraception", false))
        listData.add(DiagnoseDataItem("Prefer not to say", false))
        listData.add(DiagnoseDataItem("Other", false))
        return listData
    }

    fun getDiagnoseData(): ArrayList<DiagnoseDataItem> {
        val listData = ArrayList<DiagnoseDataItem>()
        listData.add(DiagnoseDataItem("None", false))
        listData.add(DiagnoseDataItem("PCOS", false))
        listData.add(DiagnoseDataItem("PCOD", false))
        listData.add(DiagnoseDataItem("Hypothyroidism", false))
        listData.add(DiagnoseDataItem("Menopause", false))
        listData.add(DiagnoseDataItem("Endometriosis", false))
        return listData
    }


    fun updateFemaleHealthData() {
        //will do here api call
        updateFMHDate.postValue(Event(true))
    }

    fun calenderDayRange(): Int {
        val dayDiff = pcDays - pDays
        val dayRange: Int = if (dayDiff > 0) {
            dayDiff
        } else {
            0
        }
        return dayRange
    }

}

enum class GoalType {
    TRACK_CYCLE, TRACK_CONCEIVE, TRACK_PREGNANCY
}