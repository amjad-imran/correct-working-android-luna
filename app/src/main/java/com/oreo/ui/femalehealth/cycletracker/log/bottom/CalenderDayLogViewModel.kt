package com.oreo.ui.femalehealth.cycletracker.log.bottom

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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

    var hmOfIcons = HashMap<LocalDate, Pair<List<FHSymptomsIconsModel>?, List<FHFlowIconsModel>?>>()

    private val _femaleHealthIcons = MutableLiveData<FemaleHealthIconsModel>()
    val femaleHealthIcons: LiveData<FemaleHealthIconsModel> get() = _femaleHealthIcons

    var todayDate = LocalDate.now()

    fun getFemaleHealthIcons() {
        viewModelScope.launch {
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
                                        getFemaleHealthIcons()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let {
                            _femaleHealthIcons.postValue(it)
                        }
                    }
                }
            }

        }
    }

}