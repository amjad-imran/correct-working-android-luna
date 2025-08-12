package com.oreo.ui.timelineScreen

import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.timeline.ItemTimelineModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TimelineScreenDataViewmodel @Inject constructor(

): BaseViewModel() {

    val activityListData = MutableLiveData<ArrayList<ItemTimelineModel>>()

}