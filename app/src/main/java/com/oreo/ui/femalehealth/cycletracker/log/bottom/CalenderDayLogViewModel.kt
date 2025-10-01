package com.oreo.ui.femalehealth.cycletracker.log.bottom

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.timeline.ItemTimelineResponseModel
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.FemaleHealthIconsModel
import com.oreo.data.repository.abstraction.FemaleHealthRepository
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CalenderDayLogViewModel
@Inject
constructor(
    val femaleHealthRepository: FemaleHealthRepository,
    val sessionManager: SessionManager
) : BaseViewModel() {

//    var hmOfIcons = HashMap<LocalDate, Pair<ArrayList<FHSymptomsIconsModel>?, ArrayList<FHFlowIconsModel>?>>()

    private val _serverSuccess = MutableLiveData<Event<Boolean>>()
    val serverSuccess: LiveData<Event<Boolean>> get() = _serverSuccess
    private val _femaleHealthIcons = MutableLiveData<FemaleHealthIconsModel>()
    val femaleHealthIcons: LiveData<FemaleHealthIconsModel> get() = _femaleHealthIcons


    private val _currentPeriodRange = MutableLiveData<String?>()
    val currentPeriodRange: LiveData<String?> get() = _currentPeriodRange

    var calendarStartDate: LocalDate = LocalDate.now().minusMonths(2)
    var selectedDate: MutableLiveData<LocalDate> = MutableLiveData(LocalDate.now())
    var todayDate = LocalDate.now()


    var periodStartDate: LocalDate? = null
    var periodEndDate: LocalDate? = null

    var editDataAddActivity : ItemTimelineResponseModel ?= null

    fun getFemaleHealthIcons(date: String) {

        viewModelScope.launch {
            if (_femaleHealthIcons.value != null) {
                getDataForDate(date, _femaleHealthIcons.value)
                return@launch
            }
            femaleHealthRepository.getFemaleHealthIcons().collect { resource ->
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
                                        getFemaleHealthIcons(date)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let { data ->
                            if(editDataAddActivity?.canBeEditedOrDeleted == 0) {
                                editDataAddActivity?.metadata?.symptoms?.forEach { curItem ->
                                    data.symptoms?.first { it.symptomShortName?.lowercase() == curItem }
                                        ?.let {
                                            it.isChecked = true
                                        }
                                }

                                editDataAddActivity?.metadata?.flow?.forEach { curItem ->
                                    data.flow?.first { it.symptomShortName?.lowercase() == curItem }
                                        ?.let {
                                            it.isChecked = true
                                        }
                                }

                                _femaleHealthIcons.postValue(data)

                            }
                            else{
                                getDataForDate(date, data)
                            }
                        }
                    }
                }
            }

        }
    }

    fun getDataForDate(date: String, femaleHealthIconsModel: FemaleHealthIconsModel?) {
        viewModelScope.launch {
            femaleHealthRepository.getFemaleHealthUserInfo(date).collect { resource ->
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
                                        getDataForDate(date, femaleHealthIconsModel)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let { femaleHealthUserInfo ->

                            femaleHealthIconsModel?.symptoms?.forEach { data ->
                                val femaleHealthUser =
                                    femaleHealthUserInfo?.symptom?.symptoms?.find {
                                        it.symptomShortName?.lowercase() == data.symptomShortName?.lowercase()
                                    }

                                data.isChecked = femaleHealthUser != null
                            }

                            femaleHealthIconsModel?.flow?.forEach { data ->

                                if (femaleHealthUserInfo?.symptom?.flow?.symptomShortName?.lowercase() == data.symptomShortName?.lowercase()) {
                                    data.isChecked = true
                                } else {
                                    data.isChecked = false
                                }
                            }
                            _femaleHealthIcons.postValue(femaleHealthIconsModel!!)
                        }
                    }
                }
            }

        }
    }


    fun saveSymptom(date: String, symptoms: ArrayList<String>, flowType: String?) {
        val jsonObject = JsonObject().apply {
            this.addProperty("date", date)
            this.addProperty("flow_type", flowType)
            this.add("symptoms", JsonArray().apply {
                symptoms.forEach { selectedId ->
                    this.add(selectedId)
                }
            })
        }
        viewModelScope.launch {
            femaleHealthRepository.saveLogSymptom(jsonObject).collect { resource ->
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
                                        saveSymptom(date, symptoms, flowType)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let {
                            _serverSuccess.postValue(Event(true))
                        }
                    }
                }
            }

        }
    }

    fun getPeriodDates() {
        if (periodStartDate != null && periodEndDate != null) {
            if (selectedDate.value!! <= periodEndDate) {
                _currentPeriodRange.value =
                    "${periodStartDate!!.format(DateTimeFormatter.ofPattern("dd"))} - ${
                        periodEndDate!!.format(DateTimeFormatter.ofPattern("dd MMM"))
                    }"
                return
            }
        }
        _currentPeriodRange.value = null
    }

}