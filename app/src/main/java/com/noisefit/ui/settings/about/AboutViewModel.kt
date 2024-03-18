package com.noisefit.ui.settings.about

import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    val sessionManager: SessionManager,
    val localDataStore: DataStoredInterface
) : BaseViewModel() {
}