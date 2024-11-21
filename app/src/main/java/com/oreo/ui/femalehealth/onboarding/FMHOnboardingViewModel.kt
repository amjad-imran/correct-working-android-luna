package com.oreo.ui.femalehealth.onboarding


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.model.DiagnoseDataItem
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.WheelItem
import com.noisefit_commans.utils.wheel.WheelItemPeriod
import com.oreo.data.repository.abstraction.FemaleHealthRepository
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.joda.time.format.DateTimeFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject


const val DefaultPeriodDays = 5
const val MinPeriodDays = 1
const val MaxPeriodDays = 15

const val DefaultCycleDays = 28
const val MinCycleDays = 12
const val MaxCycleDays = 100

@HiltViewModel
class FMHOnboardingViewModel @Inject constructor(
    val localDataStore: DataStoredInterface, val femaleHealthRepository: FemaleHealthRepository,
    val resourcesProvider: ResourcesProvider
) : BaseViewModel() {
    val fragmentSize = 7

    var onNextPress = MutableLiveData<Event<Boolean>>()
    var onNotSurePress = MutableLiveData<Event<Boolean>>()

    var pDays = 0
    var pcDays = 0

    private var periodDays = ArrayList<Int>()
    private var periodCycleDays = ArrayList<Int>()
    var goalTypeSelected: GoalType? = null
    var selectedDiagnoseListData = ArrayList<String>()
    var selectedHormoneListData = ArrayList<String>()

    //    val updateFMHDate = MutableLiveData<Event<Boolean>>()
    var selectedPEndDate: LocalDate? = null
    var selectedPStartDate: LocalDate? = null

    init {
        for (i in MinPeriodDays..MaxPeriodDays) {
            periodDays.add(i)
        }
        for (i in MinCycleDays..MaxCycleDays) {
            periodCycleDays.add(i)
        }
    }

    fun getPeriodDayData(): List<WheelItemPeriod<String>> {
        val dataSet = ArrayList<WheelItemPeriod<String>>()
        periodDays.forEach {
            if (it < 10) {
                dataSet.add(WheelItemPeriod("0$it"))
            } else {
                dataSet.add(WheelItemPeriod("$it"))
            }
        }
        return dataSet

    }

    fun getPeriodCycleDayData(): List<WheelItemPeriod<String>> {
        val dataSet = ArrayList<WheelItemPeriod<String>>()
        periodCycleDays.forEach {
            if (it < 10) {
                dataSet.add(WheelItemPeriod("0$it"))
            } else {
                dataSet.add(WheelItemPeriod("$it"))
            }
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
            pcDays = DefaultCycleDays
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
        listData.add(DiagnoseDataItem("None", resourcesProvider.getString(R.string.text_none), false))
        listData.add(DiagnoseDataItem("Fertility treatments",
            resourcesProvider.getString(R.string.text_fertility_treatments), false))
        listData.add(
            DiagnoseDataItem(
                "Hormone replacement therapy",
                resourcesProvider.getString(R.string.text_hormone_replacement_therapy),
                false
            )
        )
        listData.add(DiagnoseDataItem("Hormonal contraception",
            resourcesProvider.getString(R.string.text_hormonal_contraception), false))
        listData.add(DiagnoseDataItem("Prefer not to say",
            resourcesProvider.getString(R.string.text_prefer_not_to_say), false))
        listData.add(DiagnoseDataItem("Other",
            resourcesProvider.getString(R.string.text_other), false))
        return listData
    }

    fun getDiagnoseData(): ArrayList<DiagnoseDataItem> {
        val listData = ArrayList<DiagnoseDataItem>()
        listData.add(DiagnoseDataItem("None", resourcesProvider.getString(R.string.text_none), false))
        listData.add(DiagnoseDataItem("PCOS", "PCOS", false))
        listData.add(DiagnoseDataItem("PCOD", "PCOD", false))
        listData.add(DiagnoseDataItem("Hypothyroidism",
            resourcesProvider.getString(R.string.text_hypothyroidism), false))
        listData.add(DiagnoseDataItem("Menopause",
            resourcesProvider.getString(R.string.text_menopause), false))
        listData.add(DiagnoseDataItem("Endometriosis",
            resourcesProvider.getString(R.string.text_endometriosis), false))
        return listData
    }

    private val _femaleHealthSubmitInfo = MutableLiveData<Event<Boolean>>()
    val femaleHealthSubmitInfo: LiveData<Event<Boolean>>
        get() = _femaleHealthSubmitInfo

    private val _femaleHealthSkip = MutableLiveData<Event<Boolean>>()
    val femaleHealthSkip: LiveData<Event<Boolean>>
        get() = _femaleHealthSkip

    private val _moveBack = MutableLiveData<Event<Boolean>>()
    val moveBack: LiveData<Event<Boolean>>
        get() = _moveBack

    /**
     * navigationState 1->Back, 2->All Set, 3->Cycle Tracker,4 -> No action
     */
    fun updateFemaleHealthData(navigationState: Int = 2) {
        val jsonObject = JsonObject()

        jsonObject.addProperty("goal", goalTypeSelected?.name)
        if (pDays == 0) {
            jsonObject.addProperty("period_length", DefaultPeriodDays)
        } else {
            jsonObject.addProperty("period_length", pDays)
        }

        if (pcDays == 0) {
            jsonObject.addProperty("cycle_length", DefaultCycleDays)
        } else {
            jsonObject.addProperty("cycle_length", pcDays)
        }

        selectedPStartDate?.let {
            val date = it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH))
            jsonObject.addProperty("period_date", date)
        }

        val diagnoseArray = JsonArray()
        selectedDiagnoseListData.forEach {
            diagnoseArray.add(it)
        }
        jsonObject.add("diagnosis", diagnoseArray)
        val medicinesArray = JsonArray()
        selectedHormoneListData.forEach {
            medicinesArray.add(it)
        }
        jsonObject.add("medicines", medicinesArray)

        viewModelScope.launch {
            femaleHealthRepository.submitFemaleHealthInfo(jsonObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        updateFemaleHealthData(navigationState)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            localDataStore.setFMHWalkthroughShown(true)
                            /**
                             * navigationState 1->Back, 2->All Set, 3->Cycle Tracker
                             */
                            when (navigationState) {
                                1 -> {
                                    _moveBack.postValue(Event(true))
                                }

                                2 -> {
                                    _femaleHealthSubmitInfo.postValue(Event(true))
                                }

                                3 -> {
                                    _femaleHealthSkip.postValue(Event(true))
                                }

                                4 -> {
                                    //do nothing
                                }
                            }

                            goalTypeSelected = null
                            selectedDiagnoseListData.clear()
                            selectedHormoneListData.clear()
                            selectedPStartDate = null
                            selectedPEndDate = null
                        }
                    }
                }
            }
        }
    }


    fun calculatePeriodEndDate(date: LocalDate): LocalDate {
        return date.plusDays(pDays.toLong() - 1)
    }

    fun calculateStartAndEndPeriodDates() {
        if (pDays != 0 && selectedPStartDate != null && selectedPEndDate != null) {
            selectedPEndDate = selectedPStartDate!!.plusDays(pDays.toLong() - 1)
        }
    }

    fun resetData() {
        goalTypeSelected = null
        selectedDiagnoseListData.clear()
        selectedHormoneListData.clear()
        selectedPStartDate = null
        selectedPEndDate = null

    }

}

enum class GoalType {
    TRACK_CYCLE, TRY_CONCEIVE, TRACK_PREGNANCY
}