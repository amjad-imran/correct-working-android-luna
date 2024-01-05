package com.oreo.ui.recordworkout

import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.OWorkoutListModal
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RecordWorkoutViewModel @Inject constructor(
    val sessionManager: SessionManager,
    val watchDataStore: WatchDataStore
) : BaseViewModel() {


    var workout: OWorkoutListModal?=null
    var sportStartTime = 0L


}