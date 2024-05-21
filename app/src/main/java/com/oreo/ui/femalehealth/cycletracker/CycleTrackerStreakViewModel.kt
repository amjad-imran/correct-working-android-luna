package com.oreo.ui.femalehealth.cycletracker

import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CycleTrackerStreakViewModel @Inject constructor() : BaseViewModel() {

    fun getSymptomsData(): ArrayList<String> {
        return arrayListOf("Test1", "Test2", "Test3", "Test4")
    }
}