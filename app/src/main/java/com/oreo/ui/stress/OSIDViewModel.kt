package com.oreo.ui.stress

import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.ChartModel
import com.oreo.data.model.ChartModelStress
import com.oreo.data.model.OInternalPageResponseModal
import com.oreo.data.model.ResultData
import com.oreo.data.model.ResultDataStress
import com.oreo.data.model.OStressInternalPageResponseModal
import com.oreo.data.model.StressData
import com.oreo.data.model.StressShowData
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import com.oreo.ui.sleep.scoredetails.ViewItemClickType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OSIDViewModel @Inject constructor(
    val userActivityRepository: OreoUserActivityRepository,
    val sessionManager: SessionManager
) : BaseViewModel() {


    var dayType: String? = null
    var selectedDate: String? = null

    private val _internalDetailsData = MutableLiveData<OStressInternalPageResponseModal>()
    val internalDetailsData: LiveData<OStressInternalPageResponseModal>
        get() = _internalDetailsData


    fun getInternalDetailsData() {
        /*viewModelScope.launch {
            userActivityRepository.getStressInternalPagesData(
                selectedDate!!, dayType.toString().lowercase()
            ).collect { resource ->
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
                                        getInternalDetailsData()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _internalDetailsData.postValue(it)
                        }
                    }
                }
            }
        }*/
        val dummyData = OStressInternalPageResponseModal(
            resultData = null, stressData = StressData(
                focussed = StressShowData(duration = 368, 5),
                calm = StressShowData(duration = 468, 5),
                stressed = StressShowData(duration = 125, 14),
                avgDuration = 425,
                dspMsg = "You have spent an average of"
            )
        )
        _internalDetailsData.postValue(dummyData)
    }

    fun getPrefixAndSuffixList(
        dataList: ArrayList<ResultDataStress>,
        dayType: String?
    ): Triple<Pair<ArrayList<ChartModelStress>, Int>, ArrayList<ChartModelStress>, ArrayList<ChartModelStress>> {
        dataList.reversed()
        val list = ArrayList<ChartModelStress>()
        var max = 10
        dataList.forEach {


            val index = if (dayType?.lowercase() == "day")
                DateFormats.shortFormatWeek(it.date)
            else if (dayType?.lowercase() == "month")
                DateFormats.getMonth(it.date.toInt() - 1)
            else {
                it.date
            }

            val chartModel = ChartModelStress(
                date = it.date,
                index = index,
                calm = it.data.calm,
                focussed = it.data.focussed,
                stressed = it.data.stressed,
            )
            list.add(chartModel)
        }


        val suffix = ArrayList<ChartModelStress>()
        for (i in 1..7) {
            val chartModel = ChartModelStress(index = "")
            suffix.add(chartModel)
        }

        val prefix = ArrayList<ChartModelStress>()
        for (i in 1..7) {
            val chartModel = ChartModelStress(index = "")
            prefix.add(chartModel)
        }

        return Triple(Pair(list, max), suffix, prefix)
    }

    fun lineGraphScoreColor(): Triple<Int, Int, Int> {
        var lineColor = 0
        var fillColorStart = 0
        var fillColorEnd = 0

        lineColor = ContextCompat.getColor(
            NoisefitApplication.context!!,
            R.color.sleep_chart_line_color
        )
        fillColorStart = ContextCompat.getColor(
            NoisefitApplication.context!!,
            R.color.sleep_fill_start_color
        )
        fillColorEnd = ContextCompat.getColor(
            NoisefitApplication.context!!,
            R.color.sleep_fill_end_color
        )

        return Triple(lineColor, fillColorStart, fillColorEnd)

    }


}