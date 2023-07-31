package com.noisefit.ui.dashboard.feature.qrPayment.bottomsheets

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.hmsscankit.WriterException
import com.huawei.hms.ml.scan.HmsBuildBitmapOption
import com.huawei.hms.ml.scan.HmsScan
import com.noisefit.R
import com.noisefit.databinding.BottomSheetEditQrBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.models.UPIQRCode
import dagger.hilt.android.AndroidEntryPoint

const val QR_EDIT = "QR_EDIT"

@AndroidEntryPoint
class BottomSheetEditQr :
    BaseBottomSheetWithTransparent<BottomSheetEditQrBinding>(BottomSheetEditQrBinding::inflate) {

    private var qrCodeObj: UPIQRCode? = null

    private val args: BottomSheetAddNameArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL,com.noisefit_commans.R.style.DialogStyle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        qrCodeObj = args.qrCode

        binding.tvNameLabel.text = qrCodeObj?.title

        qrCodeObj?.url?.let { generateCode(it) }
    }

    override fun initListener() {

        binding.icEditName.setOnClickListener {
            navigateUpSafe()
            setFragmentResult(
                QR_EDIT, bundleOf("action" to EditQrActions.EDIT_NAME)
            )
        }

        binding.btnBack.setOnClickListener {
            navigateUpSafe()
            setFragmentResult(
                QR_EDIT, bundleOf("action" to EditQrActions.REMOVE)
            )
        }
        binding.btnNext.setOnClickListener {
            navigateUpSafe()
            setFragmentResult(
                QR_EDIT, bundleOf("action" to EditQrActions.REPLACE)
            )
        }
    }

    override fun subscribeObservers() {

    }

    private fun generateCode(content: String) {

        val width = 700
        val height = 700

        try {
            val options =
                HmsBuildBitmapOption.Creator().setBitmapMargin(1).setBitmapColor(Color.LTGRAY)
                    .setBitmapBackgroundColor(Color.TRANSPARENT).create()
            val resultImage =
                ScanUtil.buildBitmap(content, HmsScan.QRCODE_SCAN_TYPE, width, height, options)
            binding.ivQrCode.setImageBitmap(resultImage)

        } catch (e: WriterException) {
            e.printStackTrace()
            context.showShortToast(getString(R.string.text_something_went_wrong))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dia ->
            val dialog = dia as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
                isHideable = true
                isDraggable = true
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }
}

enum class EditQrActions {
    REMOVE, REPLACE, EDIT_NAME
}