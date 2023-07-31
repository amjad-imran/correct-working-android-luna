package com.noisefit.ui.dashboard.feature.worldclock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.noisefit_commans.models.WorldClockList
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WorldClockViewModel @Inject constructor() : ViewModel() {


    private val _editMode = MutableLiveData<Boolean>()

    val editMode: LiveData<Boolean>
        get() = _editMode

    fun setEditMode(mode: Boolean) {
        _editMode.value = mode
    }

    fun hasClock(newClock: WorldClockList.WClock): Boolean {
        if (clocks == null) return false
        clocks?.forEach {
            if (it.content.equals(newClock.content, true)) {
                return true
            }
        }
        return false
    }

    var clocks: ArrayList<WorldClockList.WClock>? = null
    var updatePosition = -1


}