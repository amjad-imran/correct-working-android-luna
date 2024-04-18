package com.oreo.ui.heartrate

import com.noisefit.luna.R
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.OHRInfoDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OHRInfoViewModel @Inject constructor() : BaseViewModel() {
    fun getHrInfoListData(): ArrayList<OHRInfoDataModel> {
        val infoListData = ArrayList<OHRInfoDataModel>()
        infoListData.add(
            OHRInfoDataModel(
                description =
                "The heart rate graph displays your daily heart rate from midnight to midnight. Heart rate measurements are taken every 5 minutes while you're wearing the ring."
            )
        )
        infoListData.add(
            OHRInfoDataModel(
                title = "Average line",
                banner = R.drawable.ic_ohr_info_avg_line,
                description = "Your average heart rate is shown as a red line on top of the bars. This line helps you follow your heart rate trend throughout the day."
            )
        )
        infoListData.add(
            OHRInfoDataModel(
                title = "Heart rate bars",
                banner = R.drawable.ic_ohr_info_hr_bars,
                description = "Your heart rate graph is divided into 30-minute intervals, represented by line bars. Each line bar corresponds to a time slot, indicating whether your heart rate was measured consistently every 5 minutes within that period.\n\n" +
                        "Two bars stacked on top of each other indicate that there were moments when the ring wasn't able to detect your heart rate due to movement or the ring not being worn."
            )
        )
        infoListData.add(
            OHRInfoDataModel(
                title = "Why are there gaps on my graphs?",
                banner = R.drawable.ic_ohr_info_break_bars,
                description = "There can be gaps in your heart rate data if you don't wear your ring or if you move around a lot. Short gaps are marked as a dotted line on the average heart rate curve. Gaps over 3 hours aren't marked with a dotted line at all.\n\n" +
                        "If you're not getting heart rate readings while you're relatively still, make sure that your ring's sensors are underneath your finger, and that your ring fits snugly. If your ring feels too loose, try wearing it on a different finger."
            )
        )
        infoListData.add(
            OHRInfoDataModel(
                title = "Header to be given",
                banner = R.drawable.ic_ohr_info_headers_bars,
                description =
                "There can be gaps in your heart rate data if you don't wear your ring or if you move around a lot. Short gaps are marked as a dotted line on the average heart rate curve. Gaps over 3 hours aren't marked with a dotted line at all.\n\n" +
                        "If you're not getting heart rate readings while you're relatively still, make sure that your ring's sensors are underneath your finger, and that your ring fits snugly. If your ring feels too loose, try wearing it on a different finger."
            )
        )
        return infoListData
    }
}