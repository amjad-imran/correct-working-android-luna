package com.noisefit.ui.watchface.custom.vision

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.JsonObject
import com.noisefit.luna.R
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.local.AppStaticData
import com.noisefit.data.repository.abstraction.RewardsRepository
import com.noisefit.luna.databinding.FragmentVisionCustomWatchFaceBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.showShortToast
import com.noisefit.ui.watchface.REQUEST_LAUNCH_IMAGE_CAPTURE
import com.noisefit.ui.watchface.REQUEST_LAUNCH_LIBRARY
import com.noisefit.ui.watchface.WatchFaceProgressBottomDialog
import com.noisefit.ui.watchface.adapter.ColorListAction
import com.noisefit.ui.watchface.adapter.ColorsAdapter
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.utils.CommonConstants
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.CustomWatchFace
import com.noisefit_commans.models.UpdateStatus
import com.noisefit_commans.models.WatchUpdateStatus
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class VisionCustomWatchFaceFragment :
    BaseFragment<FragmentVisionCustomWatchFaceBinding>(FragmentVisionCustomWatchFaceBinding::inflate),
    ColorListAction {

    private var customWatchface: CustomWatchFace? = null

    @Inject
    lateinit var watchesSDK: WatchesSDK

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var rewardsRepository: RewardsRepository

    private val adapter: ColorsAdapter by lazy {
        ColorsAdapter(this)
    }
    private var widthHeight: Pair<Int, Int>? = null
    var outputUri: Uri? = null
    var photoURI: Uri? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        widthHeight = watchesSDK.getWatchWidthHeight()
        binding.toolbar.tvTitle.text = getString(R.string.text_custom_watch_face)
        customWatchface = CustomWatchFace(0, 0, 1, 1)


        setRecyclerView()


    }


    private fun setRecyclerView() {
        binding.rvColors.layoutManager = GridLayoutManager(context, 7)
        binding.rvColors.adapter = adapter

        adapter.setDataSet(AppStaticData.getWatchFaceColorList())

    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.bUpdate.setOnClickListener {

            if (!sessionManager.isDeviceConnected()) {
                context.showShortToast(getString(R.string.text_device_not_connected))
                return@setOnClickListener
            }
            updateWatchface()
        }

        binding.lytChooseBg.ivGalleryBack.setOnClickListener {
            val libraryIntent = Intent(Intent.ACTION_PICK)
            libraryIntent.type = "image/*"
            startActivityForResult(
                Intent.createChooser(libraryIntent, null),
                REQUEST_LAUNCH_LIBRARY
            )
        }

        binding.lytChooseBg.ivCameraBack.setOnClickListener {
            checkCameraPermission {
                dispatchTakePictureIntent()
            }
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
                            widthHeight?.first?.toFloat() ?: 0f,
                            widthHeight?.second?.toFloat() ?: 0f
                        )
                        .withOptions(uCropOptions)
                        .withMaxResultSize(
                            widthHeight?.first ?: 100,
                            widthHeight?.second ?: 100
                        )
                        .start(requireActivity(), this)
                }

                REQUEST_LAUNCH_LIBRARY -> {
                    val selectedImageUri: Uri = data?.data ?: return

                    outputUri = Uri.fromFile(File(context?.cacheDir, "output.png"))
                    UCrop.of(selectedImageUri, outputUri!!)
                        .withAspectRatio(
                            widthHeight?.first?.toFloat() ?: 0f,
                            widthHeight?.second?.toFloat() ?: 0f
                        )
                        .withOptions(uCropOptions)
                        .withMaxResultSize(
                            widthHeight?.first ?: 100,
                            widthHeight?.second ?: 100
                        )
                        .start(requireActivity(), this)
                }

                UCrop.REQUEST_CROP -> {
                    val resultUri = data?.let { UCrop.getOutput(it) }

                    binding.ivBackgroundLayer.setImageURI(null)
                    binding.ivBackgroundLayer.setImageURI(resultUri)
                    customWatchface?.selectedImagePath = resultUri.toString()
                }
            }
        }
    }

    override fun subscribeObservers() {

        sessionManager.updateDeviceCallback.observe(viewLifecycleOwner) { event ->
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

                context.showShortToast("Watch Face Transferred")
                sessionManager.transferInProgress = false
                //sessionManager.showReview.postValue(Event(true))
                earnRewardsPoints()

                val count = localDataStore.getCustomWatchFaceTransferCount()
                val newCount = count + 1
                localDataStore.setCustomWatchFaceTransferCount(newCount)
                sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.CUSTOM_WATCHFACE_UPDATE_CLICK,
                    HashMap<String, Any>().apply {
                        this["watch_face_update_status"] = true
                    })
                sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.WATCH_FACES_UPDATED_CLICK,
                    HashMap<String, Any>().apply {
                        this["watch_face_name"] =
                            "custom_watchface_0"
                        this["watch_face_update_status"] = true
                        this["detail"] = "0" + "\n" +
                                "custom_watchface_0" + "\n" +
                                UpdateStatus.COMPLETED.name
                    }
                )
            }

            UpdateStatus.ERROR -> {
                progressBottomSheet?.dismiss()
                context.showShortToast("Failed")
                sessionManager.transferInProgress = false
                sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.CUSTOM_WATCHFACE_UPDATE_CLICK,
                    HashMap<String, Any>().apply {
                        this["watch_face_update_status"] = false
                    })
                sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.WATCH_FACES_UPDATED_CLICK,
                    HashMap<String, Any>().apply {
                        this["watch_face_name"] =
                            "custom_watchface_0"
                        this["watch_face_update_status"] = false
                        this["detail"] = "0" + "\n" +
                                "custom_watchface_0" + "\n" +
                                UpdateStatus.ERROR.name
                    }
                )
            }
            null -> {
                sessionManager.transferInProgress = false
            }
            else -> {}
        }
    }

    override fun onColorSelected(color: Int) {
        customWatchface?.color = color
        binding.ivTextLayer.setColorFilter(color)
    }

    private fun updateWatchface() {

        if ((sessionManager.batterPercent.value ?: 0) <= 30) {
            showBatteryWarning()
            return
        }


        if (customWatchface?.selectedImagePath == null) {
            uiController.onDisplayError(getString(R.string.text_please_select_an_image_first))
            return
        }

        customWatchface?.let {
            sessionManager.sendUpdateQueryAction(UpdateDeviceAction.SetWatchFaceCustom(it))

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

    fun earnRewardsPoints() {
        GlobalScope.launch {
            val request = JsonObject().apply {
                this.addProperty("task_enum", "1st_custom_watch-face")
            }
            rewardsRepository.earnRewardsPoints(request).collect { resource ->

            }
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
        title: String, message: String, typeText: String, watchfaceImageUrl: String?,
        watchFaceName: String?
    ) {
        progressBottomSheet = WatchFaceProgressBottomDialog.getInstance(
            title, message, typeText, watchfaceImageUrl,
            watchFaceName
        )
        progressBottomSheet?.isCancelable = false
        progressBottomSheet?.show(
            childFragmentManager,
            "DownloadBottomSheet"
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressBottomSheet?.dismissAllowingStateLoss()
    }


}