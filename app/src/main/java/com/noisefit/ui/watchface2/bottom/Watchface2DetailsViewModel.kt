package com.noisefit.ui.watchface2.bottom

import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit.data.remote.response.DiyMyCreation
import com.noisefit.data.remote.response.Watchface2
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.watch.WatchesSDK
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class Watchface2DetailsViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
    val watchesSDK: WatchesSDK
) : BaseViewModel() {

    var showProgress = false
    val screenType = watchesSDK.getWatchForm()
    var widthHeight: Pair<Int, Int> = watchesSDK.getWatchWidthHeight()
    var diyMyCreation: DiyMyCreation? = null
    var watchFace2: Watchface2? = null
}