package com.oreo.ui.timelineScreen.addActivity.activities

import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddPeriodViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : BaseViewModel() {


}