package com.noisefit.ui.watchface.custom.iconBuzz

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.CustomDaFitIcons
import com.noisefit_commans.data.model.WatchFaceWidgets
import com.noisefit.luna.databinding.FragmentDaFitCustomWatchFaceBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.custom.ColorSeekBar
import com.noisefit.ui.watchface.REQUEST_LAUNCH_IMAGE_CAPTURE
import com.noisefit.ui.watchface.REQUEST_LAUNCH_LIBRARY
import com.noisefit.ui.watchface.WatchFaceProgressBottomDialog
import com.noisefit.ui.watchface.custom.BottomSheetWidgetSelection
import com.noisefit_commans.data.enums.DaFitCustomListItem
import com.noisefit_commans.utils.CommonConstants
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.CustomWatchFace
import com.noisefit_commans.models.UpdateStatus
import com.noisefit_commans.models.WatchUpdateStatus
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class DaFitCustomWatchFaceFragment :
    BaseFragment<FragmentDaFitCustomWatchFaceBinding>(FragmentDaFitCustomWatchFaceBinding::inflate),
    DaFitCustomItemInteractionListener {

    private var pairingTimeTaken: Long = 0
    private val viewModel: DaFitCustomViewModel by viewModels()


    private val daFitCustomItemAdapter: DaFitCustomItemAdapter by lazy {
        DaFitCustomItemAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.tvTitle.text = getString(R.string.text_custom_watch_face)
        viewModel.customWatchface = CustomWatchFace(0, 0, 1, 1)


        setRecyclerView()

        if (viewModel.isCircle) {
            binding.llTop.gravity = Gravity.CENTER
            binding.llBottom.gravity = Gravity.CENTER

            binding.ivBackgroundLayer.invisible()
            binding.ivBackgroundLayerCircle.visible()
        } else {
            binding.ivBackgroundLayer.visible()
            binding.ivBackgroundLayerCircle.invisible()
        }

        //setBackgroundLayer(R.drawable.ic_watchface_noisefit)
    }


    private fun setRecyclerView() {

        binding.rvTimeItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = daFitCustomItemAdapter
        }

        daFitCustomItemAdapter.setDataSet(viewModel.widgetIconList)

    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.bUpdate.setOnClickListener {
            pairingTimeTaken = System.currentTimeMillis()
            logApiStartEvent(InsiderAppEvents.WatchFaceEvents.wnc_face_apply)

            if (!viewModel.sessionManager.isDeviceConnected()) {
                logErrorEvent(
                    InsiderAppEvents.WatchFaceEvents.wnc_face_apply_failed_nc,
                    getString(R.string.text_device_not_connected)
                )
                context.showShortToast(getString(R.string.text_device_not_connected))
                return@setOnClickListener
            }
            if (viewModel.customWatchface?.selectedImagePath == null) {
                logErrorEvent(
                    InsiderAppEvents.WatchFaceEvents.wnc_face_apply_failed_ei,
                    "empty image"
                )
                uiController.onDisplayError(getString(R.string.text_please_select_an_image_first))
                return@setOnClickListener
            }

            updateWatchface()
        }

        binding.lytChooseBg.ivGalleryBack.setOnClickListener {
            val libraryIntent = Intent(Intent.ACTION_PICK)
            libraryIntent.type = "image/*"
            startActivityForResult(
                Intent.createChooser(libraryIntent, null), REQUEST_LAUNCH_LIBRARY
            )
        }

        binding.lytChooseBg.ivCameraBack.setOnClickListener {
            checkCameraPermission {
                dispatchTakePictureIntent()
            }
        }

        setFragmentResultListener(BottomSheetWidgetSelection.WIDGET_PICKER_RESULT) { key, bundle ->
            val selectedWidget = bundle.getParcelable("selectedWidget") as? WatchFaceWidgets

            when (bundle.getInt("itemId")) {
                1 -> {

                    if (selectedWidget != null) {
                        daFitCustomItemAdapter.setData(selectedWidget.widgetName, 0)
                        viewModel.timePositionWidget = selectedWidget
                    }
                }

                2 -> {
                    if (selectedWidget != null) {
                        daFitCustomItemAdapter.setData(selectedWidget.widgetName, 1)
                        viewModel.aboveTimePositionWidget = selectedWidget
                    }


                }

                3 -> {
                    if (selectedWidget != null) {
                        daFitCustomItemAdapter.setData(selectedWidget.widgetName, 2)
                        viewModel.belowTimePositionWidget = selectedWidget
                    }

                }
            }
            //    drawBitmap(viewModel.selectedColor, selectedWidget)

            handleWidgetIcons()

        }

        binding.seekbarColor.setOnColorChangeListener(object : ColorSeekBar.OnColorChangeListener {
            override fun onColorChangeListener(color: Int) {
                viewModel.customWatchface?.color = color
                viewModel.selectedColor = color
                setSvgColor()
            }
        })

    }

    private fun logApiStartEvent(eventName: String) {
        val connectedDevice = viewModel.localDataStore.getConnectedDevice() ?: return

        viewModel.sessionManager.logInsiderAppEvent(eventName,
            HashMap<String, Any>().apply {

                this["dName"] = connectedDevice.bluetoothName ?: ""
                this["dMac"] = connectedDevice.address ?: ""
                this["supplier"] = viewModel.watchesSDK.getWatchType(connectedDevice).name
            })
    }

    private fun logErrorEvent(eventName: String, status: String) {
        val timeTaken = System.currentTimeMillis() - pairingTimeTaken
        viewModel.sessionManager.logInsiderAppEvent(eventName,
            HashMap<String, Any>().apply {
                this["status"] = status
                this["time"] = TimeUnit.MILLISECONDS.toSeconds(timeTaken)
            })
    }

    private fun handleWidgetIcons() {
        LOGS.d("handleWidgetIcons ${viewModel.timePositionWidget}")
        LOGS.d("handleWidgetIcons ${viewModel.aboveTimePositionWidget}")
        LOGS.d("handleWidgetIcons ${viewModel.belowTimePositionWidget}")
        var isAbove = false
        var isAboveTimeEnable = true
        var isBelowTimeEnable = true
        if (viewModel.timePositionWidget.widgetName.equals("above", true)) {
            isAbove = true
        }
        if (viewModel.aboveTimePositionWidget.widgetName.equals("close", true)) {
            isAboveTimeEnable = false
        }
        if (viewModel.belowTimePositionWidget.widgetName.equals("close", true)) {
            isBelowTimeEnable = false
        }

        if (isAboveTimeEnable) {
            if (isAbove) {

                binding.imvAboveTimeUp.setImageDrawable(viewModel.getIcon(viewModel.aboveTimePositionWidget.widgetName.lowercase())
                    ?.let {
                        AppCompatResources.getDrawable(requireContext(), it)
                    })
                binding.imvAboveTimeUp.visible()
                binding.imvAboveTimeDown.invisible()
            } else {

                binding.imvAboveTimeDown.setImageDrawable(viewModel.getIcon(viewModel.aboveTimePositionWidget.widgetName.lowercase())
                    ?.let {
                        AppCompatResources.getDrawable(requireContext(), it)
                    })
                binding.imvAboveTimeUp.invisible()
                binding.imvAboveTimeDown.visible()
            }

        } else {
            binding.imvAboveTimeUp.invisible()
            binding.imvAboveTimeDown.invisible()
        }
        //ResourcesCompat

        if (isBelowTimeEnable) {
            if (isAbove) {
                binding.imvBelowTimeUp.setImageDrawable(viewModel.getIcon(viewModel.belowTimePositionWidget.widgetName.lowercase())
                    ?.let {
                        AppCompatResources.getDrawable(requireContext(), it)
                    })
                binding.imvBelowTimeUp.visible()
                binding.imvBelowTimeDown.invisible()
            } else {
                binding.imvBelowTimeDown.setImageDrawable(viewModel.getIcon(viewModel.belowTimePositionWidget.widgetName.lowercase())
                    ?.let {
                        AppCompatResources.getDrawable(requireContext(), it)
                    })
                binding.imvBelowTimeUp.invisible()
                binding.imvBelowTimeDown.visible()
            }
        } else {
            binding.imvBelowTimeDown.invisible()
            binding.imvBelowTimeUp.invisible()
        }

        if (isAbove) {
            binding.imvTimePositionUp.visible()
            binding.imvTimePositionDown.invisible()
        } else {

            binding.imvTimePositionUp.invisible()
            binding.imvTimePositionDown.visible()
        }


        //setSvgColor()
    }

    private fun setSvgColor() {
        binding.apply {
            imvBelowTimeUp.setColorFilter(viewModel.selectedColor)
            imvBelowTimeDown.setColorFilter(viewModel.selectedColor)
            imvAboveTimeDown.setColorFilter(viewModel.selectedColor)
            imvAboveTimeUp.setColorFilter(viewModel.selectedColor)
            imvTimePositionUp.setColorFilter(viewModel.selectedColor)
            imvTimePositionDown.setColorFilter(viewModel.selectedColor)
        }
    }

    private fun checkCameraPermission(callback: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
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
                    viewModel.photoURI = FileProvider.getUriForFile(
                        requireContext(), CommonConstants.FILE_PROVIDER, it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, viewModel.photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_LAUNCH_IMAGE_CAPTURE)
                }
            }
        }
    }


    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "PNG_${timeStamp}_", /* prefix */
            ".png", /* suffix */
            storageDir /* directory */
        ).apply {

            viewModel.currentPhotoPath = absolutePath
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)



        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_LAUNCH_IMAGE_CAPTURE -> {
                    if (viewModel.photoURI == null) return
                    viewModel.outputUri = Uri.fromFile(File(context?.cacheDir, "output.png"))
                    UCrop.of(viewModel.photoURI!!, viewModel.outputUri!!).withAspectRatio(
                        viewModel.widthHeight.first.toFloat(),
                        viewModel.widthHeight.second.toFloat()
                    ).withOptions(uCropOptions).withMaxResultSize(
                        viewModel.widthHeight.first,
                        viewModel.widthHeight.second
                            .toInt()
                    ).start(requireActivity(), this)
                }

                REQUEST_LAUNCH_LIBRARY -> {
                    val selectedImageUri: Uri = data?.data ?: return

                    viewModel.outputUri = Uri.fromFile(File(context?.cacheDir, "output.png"))
                    UCrop.of(selectedImageUri, viewModel.outputUri!!).withAspectRatio(
                        viewModel.widthHeight.first.toFloat(),
                        viewModel.widthHeight.second.toFloat()
                    ).withOptions(uCropOptions).withMaxResultSize(
                        viewModel.widthHeight.first,
                        viewModel.widthHeight.second
                            .toInt()
                    ).start(requireActivity(), this)
                }

                UCrop.REQUEST_CROP -> {
                    val resultUri = data?.let { UCrop.getOutput(it) }

                    resultUri?.let { setBackgroundLayer(it) }
                    viewModel.customWatchface?.selectedImagePath = resultUri.toString()
                }
            }
        }
    }

    private fun setBackgroundLayer(resultUri: Uri) {

        if (viewModel.isCircle) {
            binding.ivBackgroundLayerCircle.setImageURI(null)
            binding.ivBackgroundLayerCircle.setImageURI(resultUri)
        } else {
            binding.ivBackgroundLayer.setImageURI(null)
            binding.ivBackgroundLayer.setImageURI(resultUri)
        }

    }

    override fun subscribeObservers() {

        viewModel.sessionManager.updateDeviceCallback.observe(this) { event ->
            event.getContent()?.let {
                if (it is UpdateDeviceDataCallback.CustomizeWatchFaceProgress) {

                    updateWatchFaceStatus(it.watchUpdateStatus)

                }
            }
        }
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
                progressBottomSheet?.dismiss()
                logErrorEvent(InsiderAppEvents.WatchFaceEvents.wnc_face_complete, "complete")
                context.showShortToast("Watch Face Transferred")
                viewModel.sessionManager.transferInProgress = false
                //viewModel.sessionManager.showReview.postValue(Event(true))
                viewModel.earnRewardsPoints()

                val count = viewModel.localDataStore.getCustomWatchFaceTransferCount()
                val newCount = count + 1
                viewModel.localDataStore.setCustomWatchFaceTransferCount(newCount)

                viewModel.sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.WATCH_FACES_UPDATED_CLICK,
                    HashMap<String, Any>().apply {
                        this["watch_face_name"] = viewModel.watchfaceTitle.toString()
                        this["watch_face_update_status"] = true
                        this["detail"] =
                            viewModel.watchfaceId.toString() + "\n" + viewModel.watchfaceTitle + "\n" + UpdateStatus.COMPLETED.name
                    })


            }

            UpdateStatus.ERROR -> {
                var status = "failed"
                if (!watchUpdateStatus.wStatus.isNullOrEmpty()) {
                    status = watchUpdateStatus.wStatus!!
                }
                logErrorEvent(InsiderAppEvents.WatchFaceEvents.wnc_face_failed, status)
                progressBottomSheet?.dismiss()
                context.showShortToast("Failed")
                viewModel.sessionManager.transferInProgress = false
                viewModel.sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.WATCH_FACES_UPDATED_CLICK,
                    HashMap<String, Any>().apply {
                        this["watch_face_name"] = viewModel.watchfaceTitle.toString()
                        this["watch_face_update_status"] = false
                        this["detail"] =
                            viewModel.watchfaceId.toString() + "\n" + viewModel.watchfaceTitle + "\n" + UpdateStatus.ERROR.name
                    })
            }

            null -> {
                viewModel.sessionManager.transferInProgress = false
            }

            else -> {}
        }
    }

    override fun onWidgetSelected(customDaFitIcons: CustomDaFitIcons, pos: Int) {
        var itemId = 0
        var title = ""
        var selectedWidget = viewModel.timePositionWidget
        when (customDaFitIcons.daFitCustomListItem) {
            DaFitCustomListItem.TimePosition -> {
                itemId = 1
                title = "Time Position"
                selectedWidget = viewModel.timePositionWidget
            }

            DaFitCustomListItem.AboveTime -> {
                title = "Above Time"
                itemId = 2
                selectedWidget = viewModel.aboveTimePositionWidget
            }

            DaFitCustomListItem.BelowTime -> {
                itemId = 3
                title = "Below Time"
                selectedWidget = viewModel.belowTimePositionWidget
            }
        }
        viewModel.watchfaceId = pos
        viewModel.watchfaceTitle = title
        navigate(
            DaFitCustomWatchFaceFragmentDirections.actionDaFitCustomWatchFaceFragmentToBottomWidgetSelection(
                selectedWidget.supportedGrid[0], selectedWidget, itemId, title
            )
        )
    }

    private fun updateWatchface() {

        if ((viewModel.sessionManager.batterPercent.value ?: 0) <= 30) {
            showBatteryWarning()
            logErrorEvent(
                InsiderAppEvents.WatchFaceEvents.wnc_face_apply_failed_lb,
                "battery low warning"
            )
            return
        }

        viewModel.customWatchface?.let {
            viewModel.sessionManager.sendUpdateQueryAction(
                UpdateDeviceAction.SetCustomBackgroundWithLayout(
                    Uri.parse(it.selectedImagePath), viewModel.getWatchFaceLayout()
                )
            )
            showProgressDialog(
                getString(R.string.text_updating_your_watchface),
                getString(R.string.text_updating_wf),
                "Uploading…",
                null,
                null
            )
            progressBottomSheet?.setProgress(0)
        }


    }

    private fun showBatteryWarning() {
        val alertMessage = getString(R.string.text_battery_low_watchface, 20)
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

    private var progressBottomSheet: WatchFaceProgressBottomDialog? = null

    private fun showProgressDialog(
        title: String,
        message: String,
        typeText: String,
        watchfaceImageUrl: String?,
        watchFaceName: String?
    ) {
        progressBottomSheet = WatchFaceProgressBottomDialog.getInstance(
            title, message, typeText, watchfaceImageUrl, watchFaceName
        )
        progressBottomSheet?.isCancelable = false
        progressBottomSheet?.show(
            childFragmentManager, "DownloadBottomSheet"
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressBottomSheet?.dismissAllowingStateLoss()
    }



}