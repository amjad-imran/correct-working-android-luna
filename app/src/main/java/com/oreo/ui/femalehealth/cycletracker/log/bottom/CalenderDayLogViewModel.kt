package com.oreo.ui.femalehealth.cycletracker.log.bottom

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.FHFlowIconsModel
import com.oreo.data.model.FHSymptomsIconsModel
import com.oreo.data.model.FemaleHealthIconsModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class CalenderDayLogViewModel
@Inject
constructor(
    val userActivityRepository: OreoUserActivityRepository
) : BaseViewModel() {

//    var hmOfIcons = HashMap<LocalDate, Pair<ArrayList<FHSymptomsIconsModel>?, ArrayList<FHFlowIconsModel>?>>()

    private val _femaleHealthIcons = MutableLiveData<FemaleHealthIconsModel>()
    val femaleHealthIcons: LiveData<FemaleHealthIconsModel> get() = _femaleHealthIcons

    var todayDate = LocalDate.now()

    fun getFemaleHealthIcons(date: String) {

        viewModelScope.launch {
            if(_femaleHealthIcons.value != null){
                getDataForDate(date,_femaleHealthIcons.value)
                return@launch
            }
            userActivityRepository.getFemaleHealthIcons().collect { resource ->
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
                        resource.data?.data?.let {
                            getDataForDate(date,it)
                        }
                    }
                }
            }

        }
    }

    fun getDataForDate(date: String,femaleHealthIconsModel: FemaleHealthIconsModel?) {
        viewModelScope.launch {
            userActivityRepository.getFemaleHealthUserInfo(date).collect { resource ->
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
                                        getDataForDate(date,femaleHealthIconsModel)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let {
                            _femaleHealthIcons.postValue(femaleHealthIconsModel!!)
                        }
                    }
                }
            }

        }
    }


    fun saveSymptom(date:String,symptoms:ArrayList<String>,flowType:String){
        val jsonObject = JsonObject().apply {
            this.addProperty("date",date)
            this.addProperty("flow_type",flowType)
            this.add("symptoms", JsonArray().apply {
                symptoms.forEach { selectedId ->
                    this.add(selectedId)
                }
            })
        }
        viewModelScope.launch {
            userActivityRepository.saveLogSymptom(jsonObject).collect { resource ->
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

                        }
                    }
                }
            }

        }
    }

}