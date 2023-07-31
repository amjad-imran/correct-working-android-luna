package com.noisefit.ui.myDevice.warranty

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.budiyev.android.codescanner.*
import com.noisefit.luna.databinding.FragmentQrCodeScanBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.LOGS

const val QR_REQUEST_KEY = "QR_REQUEST_KEY"

class QrCodeScanFragment :
    BaseFragment<FragmentQrCodeScanBinding>(FragmentQrCodeScanBinding::inflate) {
    private var codeScanner: CodeScanner? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkCameraPermission {
            if (codeScanner == null) {
                initScanner()
            }
        }
    }

    private fun initScanner() {
        codeScanner = CodeScanner(requireContext(), binding.qrScanner)

        codeScanner?.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ONE_DIMENSIONAL_FORMATS
            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true

        }


        // Callbacks
        codeScanner?.decodeCallback = DecodeCallback {
            activity?.runOnUiThread {
                onQrScanned(it.text)
            }

        }
        codeScanner?.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            activity?.runOnUiThread {
                uiController.onDisplayError("Camera initialization error: ${it.message}")
            }
        }

        codeScanner?.startPreview()
    }

    private fun onQrScanned(text: String?) {

        LOGS.d("Scanned Result $text")

        if (context == null) return

        text?.let {
            setFragmentResult(
                VALUE_REQUEST_KEY,
                bundleOf("scanResult" to it)
            )
            navigateUpSafe()
        }

    }

    private val cameraResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            if (codeScanner == null) {
                initScanner()
            }
        } else {
            context.showShortToast("Permission Required")
        }
    }

    fun checkCameraPermission(callback: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            callback.invoke()
        } else {
            cameraResult.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onDestroyView() {
        codeScanner?.releaseResources()
        super.onDestroyView()
    }


    override fun initListener() {
        binding.btnBack.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }


}