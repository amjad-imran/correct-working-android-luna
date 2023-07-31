package com.noisefit.ui.dashboard.feature.qrPayment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.viewModels
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.hmsscankit.WriterException
import com.huawei.hms.ml.scan.HmsBuildBitmapOption
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentQrPaymentBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.watchface.REQUEST_LAUNCH_LIBRARY
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QrPaymentFragment :
    BaseFragment<FragmentQrPaymentBinding>(FragmentQrPaymentBinding::inflate) {

    private val viewModel: QRPaymentViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setLoading(true)
        viewModel.sessionManager.sendQueryAction(QueryAction.GetUPIQRCode)
    }

    override fun initListener() {

        binding.toolbar.apply {
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
            tvTitle.text = getString(R.string.text_upi_qr_code)
        }

        binding.btnSelectImage.setOnClickListener {
            val libraryIntent = Intent(Intent.ACTION_PICK)
            libraryIntent.type = "image/*"
            startActivityForResult(
                Intent.createChooser(libraryIntent, null), REQUEST_LAUNCH_LIBRARY
            )
        }


        binding.bUpdate.setOnClickListener {
            updateQrCode()
        }

        binding.btnReset.setOnClickListener {
            resetQrCode()
        }


    }

    private fun clearQRCode(){
        viewModel.uPIQRCode.url = null
        binding.ivBackgroundLayer.setImageResource(R.drawable.ic_watchface_noisefit)
    }

    private fun resetQrCode() {
        if (!viewModel.uPIQRCode.url.isNullOrEmpty()) {
            viewModel.setLoading(true)
            //id hardcoded because icon 3 support only 1 QRCode
            viewModel.sessionManager.sendUpdateQueryAction(
                UpdateDeviceAction.ClearUPIQRCode(
                    1
                )
            )
        }
    }

    private fun updateQrCode() {
        if (viewModel.uPIQRCode.url.isNullOrEmpty()) {
            uiController.onDisplayError(getString(R.string.text_please_select_an_qr_first))
            return
        }

        viewModel.setLoading(true)
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetUPIQRCode(
                arrayListOf(viewModel.uPIQRCode)
            )
        )
    }

    private fun handleImage(data: Uri) {
        val bitmap = MediaStore.Images.Media.getBitmap(
            requireActivity().contentResolver, data
        )
        val hmsScans = ScanUtil.decodeWithBitmap(
            requireActivity(), bitmap, HmsScanAnalyzerOptions.Creator().setPhotoMode(true).create()
        )
        if (!hmsScans.isNullOrEmpty() && hmsScans[0] != null && !TextUtils.isEmpty(
                hmsScans[0]!!.getOriginalValue()
            )
        ) {

            viewModel.updateQRPayment(hmsScans[0].getOriginalValue())
            generateCode(hmsScans[0].getOriginalValue())
            // hmsScans[0]
        } else {
            uiController.onDisplayError(getString(R.string.text_invalid_qr))
        }
    }

    private fun generateCode(content: String) {

        val width = 700
        val height = 700

        try {
            //Generate the barcode.
            val options =
                HmsBuildBitmapOption.Creator().setBitmapMargin(1).setBitmapColor(viewModel.color)
                    .setBitmapBackgroundColor(viewModel.background).create()
            val resultImage = ScanUtil.buildBitmap(content, viewModel.type, width, height, options)
            binding.ivBackgroundLayer.setImageBitmap(resultImage)

        } catch (e: WriterException) {
            e.printStackTrace()
            uiController.onDisplayError(getString(R.string.text_something_went_wrong_retrying))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                REQUEST_LAUNCH_LIBRARY -> {
                    //val selectedImageUri: Uri = data?.data ?: return
                    data?.data?.let {
                        handleImage(it)
                    } ?: uiController.onDisplayError(getString(R.string.text_something_went_wrong_retrying))

                }

            }
        }
    }

    override fun subscribeObservers() {
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        viewModel.sessionManager.deviceQueryCallback.observe(this) {
            when (it) {
                is QueryCallback.UPIQRCodeObtained -> {
                    viewModel.setLoading(false)
                    //viewModel.uPIQRCode = it.uPIQRCode
                    viewModel.uPIQRCode.url?.let { url ->
                        generateCode(url)
                    }

                }

                else -> {}
            }
        }


        viewModel.sessionManager.updateDeviceCallback.observe(this) {
            it.getContent()?.let { value ->
                when (value) {
                    is UpdateDeviceDataCallback.UPIQRCodeUpdated -> {
                        binding.progressBar.root.gone()
                        if (value.success) {
                            viewModel.setLoading(false)
                            context.showShortToast(getString(R.string.text_upi_qr_code_success))
                        }
                    }
                    is UpdateDeviceDataCallback.ClearUPIQRCodeUpdated -> {
                        binding.progressBar.root.gone()
                        if (value.success) {
                            viewModel.setLoading(false)
                            clearQRCode()
                        }
                    }
                    else -> {}
                }
            }
        }

    }
}


