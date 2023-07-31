package com.noisefit.ui.friends.request

import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RequestTabSharedViewModel @Inject constructor(var sessionManager: SessionManager):
    BaseViewModel() {

    var currentItem:Int=0
}