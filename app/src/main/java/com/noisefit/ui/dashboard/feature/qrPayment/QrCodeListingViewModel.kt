package com.noisefit.ui.dashboard.feature.qrPayment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.UPIQRCode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QrCodeListingViewModel @Inject constructor(
    val sessionManager: SessionManager,
    val localDataStore: DataStoredInterface
) : ViewModel() {


    var removePosition: Int = -1
    private val _editMode = MutableLiveData<Boolean>()

    val editMode: LiveData<Boolean>
        get() = _editMode

    fun setEditMode(mode: Boolean) {
        _editMode.value = mode
    }

    fun shouldShowInfo(): Boolean {
        return !localDataStore.getQrCodeInfoStatus()
    }

    var qrCodes: ArrayList<UPIQRCode>? = null
    var updatePosition = -1


}