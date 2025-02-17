package com.oreo.ui.heartrate

import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.OHRInfoDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OHRInfoViewModel @Inject constructor(
    private val resourcesProvider: ResourcesProvider
) : BaseViewModel() {
    fun getHrInfoListData(): ArrayList<OHRInfoDataModel> {
        val infoListData = ArrayList<OHRInfoDataModel>()
        infoListData.add(
            OHRInfoDataModel(
                description =
                resourcesProvider.getString(R.string.text_hr_content_1)
            )
        )
        infoListData.add(
            OHRInfoDataModel(
                title = resourcesProvider.getString(R.string.text_average_line),
                banner = R.drawable.ic_ohr_info_avg_line,
                description = resourcesProvider.getString(R.string.text_hr_content_2)
            )
        )
        infoListData.add(
            OHRInfoDataModel(
                title = resourcesProvider.getString(R.string.text_heart_rate_bars),
                banner = R.drawable.ic_ohr_info_hr_bars,
                description = resourcesProvider.getString(R.string.text_hr_content_3)
            )
        )
        infoListData.add(
            OHRInfoDataModel(
                title = resourcesProvider.getString(R.string.text_why_are_there_gaps_on_my_graphs),
                banner = R.drawable.ic_ohr_info_break_bars,
                description = resourcesProvider.getString(R.string.text_hr_content_4)
            )
        )
        infoListData.add(
            OHRInfoDataModel(
                title = resourcesProvider.getString(R.string.text_heart_rate_variations_throughout_the_day),
                banner = R.drawable.ic_ohr_info_headers_bars,
                description = resourcesProvider.getString(R.string.text_hr_content_6)
            )
        )
        return infoListData
    }
}