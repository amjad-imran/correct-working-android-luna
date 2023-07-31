package com.noisefit.ui.watchface.mode2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.response.WatchFaceDownloadResponse
import com.noisefit_commans.data.response.WatchFaceZip
import com.noisefit.luna.databinding.FragmentWatchFaceListingBinding
import com.noisefit.receiver.service.FeedbackSubmitService
import com.noisefit.receiver.service.ProblemType
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.profile.BottomSheetImagePicker
import com.noisefit.ui.watchface.REQUEST_LAUNCH_IMAGE_CAPTURE
import com.noisefit.ui.watchface.REQUEST_LAUNCH_LIBRARY
import com.noisefit.ui.watchface.WatchFaceProgressBottomDialog
import com.noisefit_commans.utils.CommonConstants
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit.watch.SDKWatchType
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.UpdateStatus
import com.noisefit_commans.models.WatchFace
import com.noisefit_commans.models.WatchUpdateStatus
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.LOGS
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class WatchFaceListingFragment :
    BaseFragment<FragmentWatchFaceListingBinding>(FragmentWatchFaceListingBinding::inflate),
    WatchFaceActions {

    private val viewModel: WatchFaceListViewModel by viewModels()


    private var minimumBatteryLevel = 30

    private val adapter: WatchFaceListAdapter by lazy {
        WatchFaceListAdapter(this)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        minimumBatteryLevel = viewModel.watchesSDK.getMinimumBatteryLevel()
        binding.toolbar.tvTitle.text = getString(R.string.text_watchfaces)

        initUi()
        setRecycler()


        val device = viewModel.localDataStore.getConnectedDevice()
        if (device == null) {
            context.showShortToast(getString(R.string.text_something_went_wrong))
            navigateUpSafe()
            return
        }
        if (viewModel.watchesSDK.getWatchType(device) == SDKWatchType.SDK_QUBE ||
            device.deviceType.equals(DeviceType.COLORFIT_PRO_2.deviceType, true) ||
            device.deviceType.equals(DeviceType.COLORFIT_PRO_2_OXY.deviceType, true)
        ) {
            viewModel.setLoading(true)
            viewModel.getSdkCloudWatchFaces()
        } else {
            viewModel.getCloudWatchFaces()
        }


        setBackgroundLayer(R.drawable.ic_watchface_noisefit)
        setLastWatchFace()
    }

    private fun <T> setBackgroundLayer(resultUri: T) {


        if (viewModel.isCircle) {
            binding.ivCurrentWatchface.loadCircleImageWithoutCache(
                requireActivity(),
                resultUri,
                R.drawable.ic_watchface_noisefit
            )

        } else {
            binding.ivCurrentWatchface.loadImageWithoutCache(
                requireActivity(),
                resultUri,
                R.drawable.ic_watchface_noisefit
            )
        }

    }

    private fun initUi() {

        viewModel.sessionManager.connectedDevice.value?.let {
            if (it.deviceType.equals(DeviceType.COLORFIT_NAV.deviceType, true) ||
                it.deviceType.equals(DeviceType.XFIT.deviceType, true)
            ) {
                binding.tvHeader.gone()
                binding.ivCurrentWatchface.gone()
            } else if (it.deviceType.equals(DeviceType.NOISEFIT_HYBRID.deviceType, true) ||
                it.deviceType.equals(DeviceType.COLORFIT_2.deviceType, true)
            ) {
                binding.tvHeader.gone()
                binding.ivCurrentWatchface.gone()
                binding.bCustomise.gone()
            } else if (it.deviceType.equals(DeviceType.NOISE_ICON_BUZZ.deviceType, true)) {
                if (!com.noisefit_commans.constants.CommonGlobals.hasIconBuzzWatchFaces) {
                    binding.tvHeader.gone()
                    binding.ivCurrentWatchface.gone()
                    binding.bCustomise.gone()
                }
            }
        }


    }

    private fun setLastWatchFace() {

        val lastWatchface = viewModel.getLastWatchFace()
        lastWatchface?.let {
            if (it.imageType.equals("custom", true)) {
                setBackgroundLayer(it.imageUrl)
                binding.ivCurrentWatchface.alpha = 1f
            } else {
                setBackgroundLayer(it.imageUrl)
                binding.ivCurrentWatchface.alpha = 1f
            }
        } ?: setBackgroundLayer(R.drawable.ic_watchface_noisefit)

    }

    private fun setRecycler() {
        binding.rvWatchFaces.layoutManager =
            GridLayoutManager(context, 2)
        binding.rvWatchFaces.adapter = adapter


    }

    override fun initListener() {
        binding.bCustomise.setOnClickListener {
            val device = viewModel.localDataStore.getConnectedDevice()
            device?.deviceType?.let {

                if ((viewModel.sessionManager.batterPercent.value ?: 0) <= minimumBatteryLevel) {
                    showBatteryWarning()
                    viewModel.sessionManager.sendQueryAction(QueryAction.QueryBatteryPower)
                    return@setOnClickListener
                }

                if (it.equals(DeviceType.COLORFIT_NAV.deviceType, true)) {
                    navigate(R.id.nfhCustomWatchFaceFragment)
                } else if (it.equals(DeviceType.COLORFIT_VISION.deviceType, true)) {
                    navigate(R.id.visionCustomWatchFaceFragment)
                } else if (viewModel.watchesSDK.getWatchType(device) == SDKWatchType.SDK_QUBE) {
                    navigate(R.id.daFitCustomWatchFaceFragment)
                } else if (it.equals(DeviceType.COLORFIT_NAV_PLUS.deviceType, true) ||
                    it.equals(DeviceType.XFIT.deviceType, true)
                ) {
                    navigate(R.id.customiseWatchfaceFragment)
                } else {
                    showImagePicker()
                }
            }
        }

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    private fun showImagePicker() {
        setFragmentResultListener(BottomSheetImagePicker.IMAGE_PICKER_RESULT) { key, bundle ->
            val value = bundle.getInt("selectedValue")
            if (value == 0) {
                val libraryIntent = Intent(Intent.ACTION_PICK)
                libraryIntent.type = "image/*"
                startActivityForResult(
                    Intent.createChooser(libraryIntent, null),
                    REQUEST_LAUNCH_LIBRARY
                )
            } else {
                checkCameraPermission {
                    dispatchTakePictureIntent()
                }
            }
        }
        navigate(WatchFaceListingFragmentDirections.actionWatchFaceListingFragmentToBottomSheetImagePicker())


    }

    override fun subscribeObservers() {

        viewModel.getSdkWatchFaces.observe(this) {
            it.getContent()?.let { status ->
                if (status) {
                    viewModel.setLoading(true)
                    viewModel.sessionManager.sendQueryAction(QueryAction.GetWatchFaces())
                }

            }

        }

        viewModel.facesList.observe(this) {
            adapter.setDataSet(it)
            viewModel.setLoading(false)
            if (it.isEmpty()) {
                binding.textView41.gone()
                binding.textView42.gone()
            } else {
                binding.textView41.visible()
                binding.textView42.visible()
            }
        }
        viewModel.watchFaceDownloadInfo.observe(this) {
            it.getContent()?.let { info ->
                updateWatchface(info)
            }
        }

        viewModel.getLoading().observe(this) {
            LOGS.d("Watch face progress $it")
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.sessionManager.deviceQueryCallback.observe(this) {
            when (it) {
                is QueryCallback.WatchFacesObtained -> {

                    viewModel.setLoading(false)
                    viewModel.setWatchFaceList(it.watchFace)
                }

                else -> {}
            }
        }
        viewModel.resetBottomSheet.observe(this) {
            it.getContent()?.let { hideBottomSheet ->
                if (hideBottomSheet) {
                    progressBottomSheet?.dismiss()
                    progressBottomSheet = null
                }
            }
        }
        viewModel.watchFaceDownloadProgress.observe(this) {
            it.getContent()?.let { progress ->
                binding.progressBar.root.gone()

                if (progress == 100) {
                    progressBottomSheet?.dismiss()
                    binding.progressBar.root.visible()
                } else {

                    if (progressBottomSheet == null) {
                        showProgressDialog(
                            "Downloading watch face",
                            getString(R.string.text_downloading_watchface),
                            "Downloading...",
                            viewModel.watchFace.value?.imageUrl,
                            viewModel.watchFace.value?.name,
                            false
                        )
                    }
                    progressBottomSheet?.setProgress(progress)

                }
            }

        }
        viewModel.setWatchFace.observe(this) { event ->
            event?.getContent()?.let {
                val fileUri = Uri.fromFile(it).toString()
                viewModel.localFilePath = fileUri
                startWatchfaceTransfer()
            }
        }

        viewModel.sessionManager.updateDeviceCallback.observe(this) { event ->
            event.getContent()?.let {
                if (it is UpdateDeviceDataCallback.CustomizeWatchFaceProgress) {
                    updateWatchFaceStatus(it.watchUpdateStatus)
                } else if (it is UpdateDeviceDataCallback.WatchFaceUpdated) {
                    updateWatchFaceStatus(WatchUpdateStatus(status = UpdateStatus.COMPLETED))
                }
            }
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

    }

    private fun startWatchfaceTransfer() {
        viewModel.sessionManager.sendUpdateQueryAction(UpdateDeviceAction.SetWatchFace(viewModel.watchFace.value!!.apply {
            if (viewModel.localFilePath != null) {
                this.localFilePath = viewModel.localFilePath!!
            }
        }))

        binding.progressBar.root.gone()
        showProgressDialog(
            getString(R.string.text_updating_your_watchface),
            getString(R.string.text_updating_wf),
            "Uploading…",
            viewModel.watchFace.value?.imageUrl,
            viewModel.watchFace.value?.name,
            false
        )
        viewModel.applyingCustomWf = false
        progressBottomSheet?.setProgress(0)
        logApiStartEvent(InsiderAppEvents.WatchFaceEvents.wn_face_start)
    }

    private fun startCustomWatchfaceTransfer(uri: Uri) {
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetCustomBackground(
                uri,
                viewModel.sessionManager.firmwareVersion ?: ""
            )
        )

        showProgressDialog(
            getString(R.string.text_updating_your_watchface),
            getString(R.string.text_updating_wf),
            "Uploading…",
            viewModel.watchFace.value?.imageUrl,
            viewModel.watchFace.value?.name,
            true
        )
        progressBottomSheet?.setProgress(0)
        viewModel.applyingCustomWf = true
        viewModel.pairingTimeTaken = System.currentTimeMillis()
        logApiStartEvent(InsiderAppEvents.WatchFaceEvents.wnc_face_apply)
    }

    private fun updateWatchface(watchFace: WatchFace) {
        val watchFaceZip = WatchFaceZip(watchFace.fileUrl!!, watchFace.fileName!!)
        val watchFaceDownloadResponse =
            WatchFaceDownloadResponse(watchFace.imageUrl!!, watchFace.fileName!!, watchFaceZip)
        updateWatchface(watchFaceDownloadResponse)
    }

    private fun updateWatchface(response: WatchFaceDownloadResponse) {

        if (viewModel.sessionManager.batterPercent.value ?: 0 <= minimumBatteryLevel) {
            showBatteryWarning()
            logErrorEvent(
                InsiderAppEvents.WatchFaceEvents.wn_face_apply_failed_lb,
                "battery low warning"
            )
            binding.progressBar.root.gone()
            return
        }

        binding.progressBar.root.visible()
        viewModel.downloadWatchFace(
            response.zip.url,
            requireContext().externalCacheDir!!,
            response.zip.originalFileName
        )


    }

    private fun updateWatchFaceStatus(watchUpdateStatus: WatchUpdateStatus) {
        when (watchUpdateStatus.status) {
            UpdateStatus.STARTED -> {
                progressBottomSheet?.setProgress(watchUpdateStatus.percentagePercentage ?: 0)
            }

            UpdateStatus.PROGRESS -> {
                progressBottomSheet?.setProgress(watchUpdateStatus.percentagePercentage ?: 0)
            }

            UpdateStatus.COMPLETED -> {

                if (viewModel.applyingCustomWf) {
                    logErrorEvent(InsiderAppEvents.WatchFaceEvents.wnc_face_complete, "complete")
                } else {
                    logErrorEvent(InsiderAppEvents.WatchFaceEvents.wn_face_complete, "complete")
                }
                progressBottomSheet?.dismiss()
                progressBottomSheet = null
                context.showShortToast("Watch Face Transferred")
                viewModel.deleteTempFile()

                viewModel.saveCurrentWatchface()
                viewModel.sessionManager.transferInProgress = false
                // viewModel.sessionManager.showReview.postValue(Event(true))
                viewModel.earnRewardsPoints()

                val count = viewModel.localDataStore.getWatchFaceTransferCount()
                val newCount = count + 1
                viewModel.localDataStore.setWatchFaceTransferCount(newCount)
                viewModel.sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.WATCH_FACES_UPDATED_CLICK,
                    HashMap<String, Any>().apply {
                        this["watch_face_name"] =
                            viewModel.watchFace.value?.name.toString()
                        this["watch_face_update_status"] = true
                        this["detail"] = viewModel.watchFace.value?.id.toString() + "\n" +
                                viewModel.watchFace.value?.name + "\n" +
                                UpdateStatus.COMPLETED.name
                    }
                )
            }

            UpdateStatus.ERROR -> {
                var status = "failed"
                if (!watchUpdateStatus.wStatus.isNullOrEmpty()) {
                    status = watchUpdateStatus.wStatus!!
                }

                if (viewModel.applyingCustomWf) {
                    logErrorEvent(InsiderAppEvents.WatchFaceEvents.wnc_face_failed, status)
                } else {
                    logErrorEvent(InsiderAppEvents.WatchFaceEvents.wn_face_failed, status)

                }

                viewModel.sessionManager.transferInProgress = false
                progressBottomSheet?.dismiss()
                progressBottomSheet = null
                context.showShortToast("Failed")
                viewModel.deleteTempFile()
                viewModel.sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.WATCH_FACES_UPDATED_CLICK,
                    HashMap<String, Any>().apply {
                        this["watch_face_name"] =
                            viewModel.watchFace.value?.name.toString()
                        this["watch_face_update_status"] = false
                        this["detail"] = viewModel.watchFace.value?.id.toString() + "\n" +
                                viewModel.watchFace.value?.name + "\n" +
                                UpdateStatus.ERROR.name
                    }
                )
                uiController.onApiErrorReceived(
                    ErrorResponse(
                        UIComponentType.RetryApiDialog("Watchface Transfer Failed. Retry?").apply {
                            this.callback = object : BinaryActionCallback {
                                override fun yes() {
                                    viewModel.watchFace.value?.let { onWatchFaceClicked(it) }
                                }

                                override fun no() {}
                            }
                        }
                    )
                )
                viewModel.shouldSendWatchFailureLogs {
                    if (it) {
                        context?.let { context ->
                            val comment =
                                "${viewModel.watchFace.value?.name ?: ""} ${viewModel.watchFaceId} ${UpdateStatus.ERROR.name}"
                            FeedbackSubmitService.startService(
                                context,
                                ProblemType.WATCHFACE_TRANSFER.name,
                                comment
                            )
                        }
                    }
                }
            }

            else -> {}
        }
    }

    private fun showBatteryWarning() {
        val alertMessage = getString(R.string.text_battery_low_watchface, minimumBatteryLevel)
        uiController.onApiErrorReceived(
            ErrorResponse(
                UIComponentType.InfoAlertDialog(
                    getString(
                        R.string.text_watch_battery_low
                    ), alertMessage, getString(R.string.text_got_it)
                )
            )
        )

    }

    private fun logErrorEvent(eventName: String, status: String) {
        val timeTaken = System.currentTimeMillis() - viewModel.pairingTimeTaken
        viewModel.sessionManager.logInsiderAppEvent(
            eventName,
            HashMap<String, Any>().apply {
                this["status"] = status
                if (viewModel.watchFaceId != null && viewModel.watchFaceId != -1) {
                    this["wId"] = viewModel.watchFaceId ?: -1
                }

                this["time"] = TimeUnit.MILLISECONDS.toSeconds(timeTaken)

            })
    }

    private fun logApiStartEvent(eventName: String) {
        val connectedDevice = viewModel.localDataStore.getConnectedDevice() ?: return

        viewModel.sessionManager.logInsiderAppEvent(
            eventName,
            HashMap<String, Any>().apply {
                if (viewModel.watchFaceId != null && viewModel.watchFaceId != -1) {
                    this["wId"] = viewModel.watchFaceId ?: -1
                }


                this["dName"] = connectedDevice.bluetoothName ?: ""
                this["dMac"] = connectedDevice.address ?: ""
                this["supplier"] = viewModel.watchesSDK.getWatchType(connectedDevice).name
            })
    }

    override fun onWatchFaceClicked(watchFace: WatchFace) {
        viewModel.mLastClickTime?.let {
            if (SystemClock.elapsedRealtime() - it < 2000) {
                LOGS.d("Returning from watchface click")
                return
            }
        }
        viewModel.mLastClickTime = SystemClock.elapsedRealtime()

        if (nullableBinding == null) return

        viewModel.pairingTimeTaken = System.currentTimeMillis()
        logApiStartEvent(InsiderAppEvents.WatchFaceEvents.wn_face_apply)

        binding.progressBar.root.visible()
        viewModel.watchFaceId = watchFace.id
        viewModel.setWatchFace(watchFace)
        setBackgroundLayer(watchFace.imageUrl)
        binding.ivCurrentWatchface.alpha = 1f

        if (!viewModel.sessionManager.isDeviceConnected()) {
            logErrorEvent(
                InsiderAppEvents.WatchFaceEvents.wn_face_apply_failed_nc,
                getString(R.string.text_device_not_connected)
            )
            context.showShortToast(getString(R.string.text_device_not_connected))
            binding.progressBar.root.gone()
            return
        }

        viewModel.setRecentWatchFace(watchFace)
        if (watchFace.imageType.equals("in_built", true)) {
            watchFace.faceType = try {
                watchFace.zip_file?.toInt() ?: -1
            } catch (exp: Exception) {
                -1
            }
            startWatchfaceTransfer()
        } else if (watchFace.imageType.equals("supplier", true)) {
            updateWatchface(watchFace)
        } else if (watchFace.imageType.equals("cloud_supplier", true)) {
            viewModel.watchFace.value?.let { it1 -> viewModel.getWatchFaceDownloadInfoCF2(it1) }
        } else {
            viewModel.watchFaceId?.let { it1 -> viewModel.getWatchFaceDownloadInfo(it1) }
        }

    }

    private fun checkCameraPermission(callback: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            callback.invoke()
        } else {
            cameraPermissionResult.launch(Manifest.permission.CAMERA)
        }
    }

    private val cameraPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            dispatchTakePictureIntent()
        } else {
            context.showShortToast("Permission Required")
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    photoURI = FileProvider.getUriForFile(
                        requireContext(), CommonConstants.FILE_PROVIDER, it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_LAUNCH_IMAGE_CAPTURE)
                }
            }
        }
    }

    lateinit var currentPhotoPath: String
    var photoURI: Uri? = null
    var outputUri: Uri? = null
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "PNG_${timeStamp}_", /* prefix */
            ".png", /* suffix */
            storageDir /* directory */
        ).apply {

            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_LAUNCH_IMAGE_CAPTURE -> {
                    if (photoURI == null) return
                    outputUri = Uri.fromFile(File(context?.cacheDir, "output.png"))
                    UCrop.of(photoURI!!, outputUri!!)
                        .withAspectRatio(
                            viewModel.getBackgroundImageWidth().toFloat(),
                            viewModel.getBackgroundImageHeight().toFloat()
                        )
                        .withOptions(uCropOptions)
                        .withMaxResultSize(
                            viewModel.getBackgroundImageWidth(),
                            viewModel.getBackgroundImageHeight()
                        )
                        .start(requireActivity(), this)
                }

                REQUEST_LAUNCH_LIBRARY -> {
                    val selectedImageUri: Uri = data?.data ?: return

                    outputUri = Uri.fromFile(File(context?.cacheDir, "output.png"))
                    UCrop.of(selectedImageUri, outputUri!!)
                        .withAspectRatio(
                            viewModel.getBackgroundImageWidth().toFloat(),
                            viewModel.getBackgroundImageHeight().toFloat()
                        )
                        .withOptions(uCropOptions)
                        .withMaxResultSize(
                            viewModel.getBackgroundImageWidth(),
                            viewModel.getBackgroundImageHeight()
                        )
                        .start(requireActivity(), this)
                }

                UCrop.REQUEST_CROP -> {
                    if (data == null) return

                    val resultUri = data.let { UCrop.getOutput(it) }

                    setBackgroundLayer(resultUri)
                    binding.ivCurrentWatchface.alpha = 1f
                    /*resultUri?.let {
                        viewModel.uploadUserImage(it)
                    }*/



                    resultUri?.let {
                        viewModel.setWatchFace(
                            WatchFace(
                                faceId = "custom",
                                imageType = "custom",
                                imageUrl = it.toString()
                            )
                        )


                        startCustomWatchfaceTransfer(it)
                    }
                }
            }
        }
    }

    private var progressBottomSheet: WatchFaceProgressBottomDialog? = null

    private fun showProgressDialog(
        title: String, message: String, typeText: String, watchfaceImageUrl: String?,
        watchFaceName: String?, isCustom: Boolean
    ) {
        progressBottomSheet = WatchFaceProgressBottomDialog.getInstance(
            title, message, typeText, watchfaceImageUrl,
            watchFaceName, isCustom
        )
        progressBottomSheet?.isCancelable = false
        progressBottomSheet?.show(
            childFragmentManager,
            "DownloadBottomSheet"
        )
    }

    override fun onDestroyView() {
        progressBottomSheet?.dismissAllowingStateLoss()
        super.onDestroyView()
    }

}