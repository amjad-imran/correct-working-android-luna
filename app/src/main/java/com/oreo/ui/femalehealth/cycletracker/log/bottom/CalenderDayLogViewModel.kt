package com.oreo.ui.femalehealth.cycletracker.log.bottom

import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class CalenderDayLogViewModel
@Inject
constructor(
    val userActivityRepository: OreoUserActivityRepository
) : BaseViewModel() {

    var todayDate = LocalDate.now()

}