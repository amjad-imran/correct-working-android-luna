package com.noisefit.ui.watchface.custom.iconBuzz

import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.luna.R
import com.noisefit.data.local.AppStaticData
import com.noisefit_commans.data.model.WatchFaceWidgets
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.RewardsRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.watch.WatchForm
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.enums.GridType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.CustomWatchFace
import com.noisefit_commans.models.WatchFaceLayout
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DaFitCustomViewModel
@Inject
constructor(
    var sessionManager: SessionManager,
    var localDataStore: DataStoredInterface,
    var watchesSDK: WatchesSDK,
    private val rewardsRepository: RewardsRepository
) : BaseViewModel() {

    var isCircle = false
    var watchfaceId: Int? = null
    var watchfaceTitle: String? = null
    var currentPhotoPath: String? = null
    var customWatchface: CustomWatchFace? = null
    var selectedColor = Color.argb(255, 255, 255, 255)
    var outputUri: Uri? = null
    var photoURI: Uri? = null
    var widthHeight: Pair<Int, Int> = watchesSDK.getWatchWidthHeight()
    var isWatchFaceAwardEarned = false

    init {
        isWatchFaceAwardEarned = localDataStore.getIsWatchFaceRewardEarned()

        if (watchesSDK.getWatchForm() == WatchForm.CIRCLE) {
            isCircle = true
        }

    }

    var timePositionWidget: WatchFaceWidgets = WatchFaceWidgets(
        supportedGrid = arrayListOf(
            GridType.TIME_POSITION
        ),
        image = R.drawable.ic_wf_battery,
        widgetName = "Above",
        type = -1
    )

    var aboveTimePositionWidget: WatchFaceWidgets = WatchFaceWidgets(
        supportedGrid = arrayListOf(
            GridType.ABOVE_TIME
        ),
        image = R.drawable.ic_wf_battery,
        widgetName = "Close",
        type = -1
    )

    var belowTimePositionWidget: WatchFaceWidgets = WatchFaceWidgets(
        supportedGrid = arrayListOf(
            GridType.BELOW_TIME
        ),
        image = R.drawable.ic_date_buzz,
        widgetName = "Date",
        type = 0
    )

    var widgetIconList = AppStaticData.getDaFitCustomItems()

    fun getIcon(widgetName: String): Int? {
        when (widgetName) {
            "date" -> {
                return R.drawable.ic_date_buzz
            }
            "sleep" -> {
                return R.drawable.ic_sleep_buzz
            }
            "heart rate" -> {
                return R.drawable.ic_hr_buzz
            }
            "steps" -> {
                return R.drawable.ic_steps_buzz
            }
            else -> {
                return null
            }
        }
    }


    fun earnRewardsPoints() {
        if (isWatchFaceAwardEarned) return

        viewModelScope.launch {
            val request = JsonObject().apply {
                this.addProperty("task_enum","1st_custom_watch-face")
            }
            rewardsRepository.earnRewardsPoints(request).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        isWatchFaceAwardEarned = true
                        localDataStore.setIsWatchFaceRewardEarned(true)
                    }
                    else -> {}
                }
            }
        }
    }

    fun getWatchFaceLayout(): WatchFaceLayout {
        val watchFaceLayout = WatchFaceLayout()
        watchFaceLayout.textColor = selectedColor
        watchFaceLayout.timeBottomContent = belowTimePositionWidget.widgetName
        watchFaceLayout.timePosition = timePositionWidget.widgetName
        watchFaceLayout.timeTopContent = aboveTimePositionWidget.widgetName
        return watchFaceLayout
    }


}