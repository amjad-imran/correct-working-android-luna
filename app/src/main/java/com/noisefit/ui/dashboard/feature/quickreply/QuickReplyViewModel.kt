package com.noisefit.ui.dashboard.feature.quickreply

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.noisefit.session.SessionManager
import com.noisefit_commans.models.CustomReplyData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QuickReplyViewModel @Inject constructor(
    val sessionManager: SessionManager
) : ViewModel() {


    private val _editMode = MutableLiveData<Boolean>()

    val editMode: LiveData<Boolean>
        get() = _editMode

    fun setEditMode(mode: Boolean) {
        _editMode.value = mode
    }

    var replies : ArrayList<CustomReplyData.CustomReply>?=null
    var updatePosition = -1


}