package com.noisefit.oreo

import androidx.lifecycle.MutableLiveData
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OreoMainViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
    val ringDataStore: RingDataStore
) : BaseViewModel() {


    var checkBluetooth = MutableLiveData<Event<Boolean>>()

    var bottomNavigation = MutableLiveData<Event<BottomNavOption>>()
    fun navigateTo(option: BottomNavOption) {
        bottomNavigation.postValue(Event(option))
    }
}