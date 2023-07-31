package com.noisefit.ui.dashboard.feature.qrPayment

import android.graphics.Color
import com.huawei.hms.ml.scan.HmsScan
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.models.UPIQRCode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QRPaymentViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
) : BaseViewModel() {

    var uPIQRCode = UPIQRCode()
    private val qrId = 1
    private val qrTitle = "QR code"
    val color = Color.BLACK
    val type = HmsScan.QRCODE_SCAN_TYPE
    val background = Color.WHITE

    fun updateQRPayment(url: String) {
        uPIQRCode.id = qrId
        uPIQRCode.title = qrTitle
        uPIQRCode.url = url
    }

}