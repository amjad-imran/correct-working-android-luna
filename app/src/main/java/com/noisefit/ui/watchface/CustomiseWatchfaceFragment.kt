package com.noisefit.ui.watchface

import alirezat775.lib.carouselview.Carousel
import alirezat775.lib.carouselview.CarouselListener
import alirezat775.lib.carouselview.CarouselView
import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.gson.JsonObject
import com.noisefit.R
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.local.AppStaticData
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.RewardsRepository
import com.noisefit.databinding.FragmentCustomiseWatchfaceBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.returnValueIfExist
import com.noisefit_commans.ui.showShortToast
import com.noisefit.ui.custom.ColorSeekBar
import com.noisefit.ui.watchface.adapter.ColorListAction
import com.noisefit.ui.watchface.adapter.ColorsAdapter
import com.noisefit.ui.watchfacenew.custom.CustomWatchfaceSliderAdapter
import com.noisefit.util.AnalyticEventUtils
import com.noisefit_commans.utils.CommonConstants
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit.watch.WatchForm
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.CustomWatchFace
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.UpdateStatus
import com.noisefit_commans.models.WatchUpdateStatus
import com.noisefit_commans.utils.LOGS
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs


const val REQUEST_LAUNCH_LIBRARY = 384
const val REQUEST_LAUNCH_IMAGE_CAPTURE = 664

@AndroidEntryPoint
class CustomiseWatchfaceFragment :
    BaseFragment<FragmentCustomiseWatchfaceBinding>(FragmentCustomiseWatchfaceBinding::inflate),
    ColorListAction {

    private var defaultEventProperty = HashMap<String, Any?>()

    @Inject
    lateinit var analyticEventUtils: AnalyticEventUtils

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var rewardsRepository: RewardsRepository

    var mLastClickTime: Long? = null

    @Inject
    lateinit var watchesSDK: WatchesSDK

    private var minimumBatteryLevel = 30
    var selectedWatchfacePosition: Int = 0
    var mCustomWatchFaces: List<CustomWatchFace>? = null
    private var pairingTimeTaken: Long = 0
    var widthHeight: Pair<Int, Int> = watchesSDK.getWatchWidthHeight()
    private val adapter: ColorsAdapter by lazy {
        ColorsAdapter(this)
    }

    var outputUri: Uri? = null
    var photoURI: Uri? = null


    var mSelectedFinalColor: Int? = null
    var mSelectedImagePath: String? = null

    private val wfSliderAdapter: CustomWatchfaceSliderAdapter by lazy {
        CustomWatchfaceSliderAdapter()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.tvTitle.text = getString(R.string.text_custom_watch_face)

        minimumBatteryLevel = watchesSDK.getMinimumBatteryLevel()

        initUi()


        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(40))
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }
        var isCircle = false
        var isSquareWithRadius = false
        if (watchesSDK.getWatchForm() == WatchForm.CIRCLE) {
            isCircle = true
        }

        when (sessionManager.connectedDevice.value?.deviceType) {
            DeviceType.COLORFIT_VISION_3.deviceType,
            DeviceType.ULTRA_3.deviceType,
            DeviceType.COLORFIT_ORE.deviceType,
            DeviceType.COLORFIT_PRO_5_47MM.deviceType,
            DeviceType.COLORFIT_PRO_5_44MM.deviceType,
            DeviceType.COLORFIT_CHROME.deviceType-> {
                isSquareWithRadius = true
            }
        }


        val carousel =
            Carousel(requireActivity() as AppCompatActivity, binding.vCarousal, wfSliderAdapter)
        val connectedDevice = localDataStore.getConnectedDevice()
        connectedDevice?.deviceType?.let {
            wfSliderAdapter.setDeviceType(
                it,
                isCircle,
                isSquareWithRadius
            )
        }
        carousel.setOrientation(CarouselView.HORIZONTAL, false)
        carousel.scaleView(true)


        carousel.addCarouselListener(object : CarouselListener {
            override fun onPositionChange(position: Int) {
                selectedWatchfacePosition = position
            }

            override fun onScroll(dx: Int, dy: Int) {

            }
        })

        mCustomWatchFaces =
            AppStaticData.getCustomWatchFaceNav(connectedDevice)

        if (mCustomWatchFaces.isNullOrEmpty()) {
            //CASE No Custom Watchface available
            navigateUpSafe()
        }

        mCustomWatchFaces?.let {
            wfSliderAdapter.setDataSet(it)
        }

        setRecyclerView()

        //customWatchface = mCustomWatchFaces?.first()


    }

    private fun logErrorEvent(eventName: String, status: String) {
        val timeTaken = System.currentTimeMillis() - pairingTimeTaken
        sessionManager.logInsiderAppEvent(eventName,
            HashMap<String, Any>().apply {
                this["status"] = status
                this["time"] = TimeUnit.MILLISECONDS.toSeconds(timeTaken)
            })
    }

    private fun logApiStartEvent(eventName: String) {
        val connectedDevice = localDataStore.getConnectedDevice() ?: return

        sessionManager.logInsiderAppEvent(eventName,
            HashMap<String, Any>().apply {
                this["dName"] = connectedDevice?.bluetoothName ?: ""
                this["dMac"] = connectedDevice?.address ?: ""
                this["supplier"] = watchesSDK.getWatchType(connectedDevice).name
            })
    }

    private fun initUi() {

        sessionManager.connectedDevice.value?.let {
            if (it.deviceType == DeviceType.NOISE_EVOLVE_2.deviceType ||
                it.deviceType == DeviceType.NOISE_EVOLVE_2_PLAY.deviceType ||
                it.deviceType == DeviceType.COLORFIT_PULSE_2.deviceType ||
                it.deviceType == DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType
            ) {
                binding.textSelectFontColor.gone()
                binding.seekbarColor.gone()
                binding.backBar.gone()
                binding.divider.root.gone()
                //binding.rvColors.gone()
            }
        }

    }

    private fun setRecyclerView() {
        /*binding.rvColors.layoutManager = GridLayoutManager(context, 7)
        binding.rvColors.adapter = adapter*/

        adapter.setDataSet(AppStaticData.getWatchFaceColorList())

    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.bUpdate.setOnClickListener {

            pairingTimeTaken = System.currentTimeMillis()
            logApiStartEvent(InsiderAppEvents.WatchFaceEvents.wnc_face_apply)
            if (!sessionManager.isDeviceConnected()) {
                logErrorEvent(
                    InsiderAppEvents.WatchFaceEvents.wnc_face_apply_failed_nc,
                    getString(R.string.text_device_not_connected)
                )
                context.showShortToast(getString(R.string.text_device_not_connected))
                return@setOnClickListener
            }

            if (mSelectedImagePath.isNullOrEmpty()) {
                logErrorEvent(
                    InsiderAppEvents.WatchFaceEvents.wnc_face_apply_failed_ei,
                    "empty image"
                )
                context.showShortToast(getString(R.string.text_please_select_an_image_first))
                return@setOnClickListener
            }


            updateWatchface()
        }

        binding.lytChooseBg.ivGalleryBack.setOnClickListener {

            mLastClickTime?.let {
                if (SystemClock.elapsedRealtime() - it < 1000) {
                    LOGS.d("Returning from watchface click")
                    return@setOnClickListener
                }
            }
            mLastClickTime = SystemClock.elapsedRealtime()

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

        binding.seekbarColor.setOnColorChangeListener(object : ColorSeekBar.OnColorChangeListener {
            override fun onColorChangeListener(color: Int) {
                mSelectedFinalColor = color
                wfSliderAdapter.setFontColor(color)
                binding.vCarousal.post {
                    //binding.vpWatchFaces.requestTransform()
                }
            }
        })
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



        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_LAUNCH_IMAGE_CAPTURE -> {
                    if (photoURI == null) return
                    outputUri = Uri.fromFile(File(context?.cacheDir, "output.png"))
                    UCrop.of(photoURI!!, outputUri!!)
                        .withAspectRatio(
                            widthHeight.first.toFloat(),
                            widthHeight.second.toFloat()
                        )
                        .withOptions(uCropOptions)
                        .withMaxResultSize(
                            widthHeight.first,
                            widthHeight.second
                        )
                        .start(requireActivity(), this)
                }

                REQUEST_LAUNCH_LIBRARY -> {
                    val selectedImageUri: Uri = data?.data ?: return

                    outputUri = Uri.fromFile(File(context?.cacheDir, "output.png"))
                    UCrop.of(selectedImageUri, outputUri!!)
                        .withAspectRatio(
                            widthHeight.first.toFloat(),
                            widthHeight.second.toFloat()
                        )
                        .withOptions(uCropOptions)
                        .withMaxResultSize(
                            widthHeight.first,
                            widthHeight.second
                        )
                        .start(requireActivity(), this)
                }

                UCrop.REQUEST_CROP -> {
                    val resultUri = data?.let { UCrop.getOutput(it) }
                    if (resultUri != null) {
                        wfSliderAdapter.setBackgroundImage(resultUri)
                        mSelectedImagePath = resultUri.toString()
                        /*binding.vpWatchFaces.post {
                            binding.vpWatchFaces.requestTransform()
                        }*/
                    }
                }
            }
        }
    }

    override fun subscribeObservers() {

        sessionManager.updateDeviceCallback.observe(this) { event ->
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
                defaultEventProperty =
                    analyticEventUtils.getEventProperty(type = AnalyticEventUtils.CustomWatchFace)

                progressBottomSheet?.setProgress(watchUpdateStatus.percentagePercentage ?: 0)
            }

            UpdateStatus.PROGRESS -> {
                progressBottomSheet?.setProgress(watchUpdateStatus.percentagePercentage ?: 0)
            }

            UpdateStatus.COMPLETED -> {

                progressBottomSheet?.dismiss()
                logErrorEvent(InsiderAppEvents.WatchFaceEvents.wnc_face_complete, "complete")
                context.showShortToast("Watch Face Transferred")
                sessionManager.transferInProgress = false
                earnRewardsPoints()

                val count = localDataStore.getCustomWatchFaceTransferCount()
                val newCount = count + 1
                localDataStore.setCustomWatchFaceTransferCount(newCount)

                sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.WATCH_FACES_UPDATED_CLICK,
                    HashMap<String, Any>().apply {
                        this["watch_face_name"] =
                            "custom_watchface_$selectedWatchfacePosition"
                        this["watch_face_update_status"] = true
                        this["detail"] =
                            selectedWatchfacePosition.toString() + "\n" +
                                    "custom_watchface_$selectedWatchfacePosition\n" +
                                    UpdateStatus.COMPLETED.name
                    }
                )

            }

            UpdateStatus.ERROR -> {

                progressBottomSheet?.dismiss()
                if (watchUpdateStatus.message.isNullOrEmpty()) {
                    context.showShortToast("Failed")
                } else {
                    context.showShortToast(watchUpdateStatus.message)
                }
                var status = "failed"
                if (!watchUpdateStatus.wStatus.isNullOrEmpty()) {
                    status = watchUpdateStatus.wStatus!!
                }
                logErrorEvent(InsiderAppEvents.WatchFaceEvents.wnc_face_failed, status)
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
                            "custom_watchface_$selectedWatchfacePosition"
                        this["watch_face_update_status"] = false
                        this["detail"] =
                            selectedWatchfacePosition.toString() + "\n" +
                                    "custom_watchface_$selectedWatchfacePosition" + UpdateStatus.ERROR.name
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
        mSelectedFinalColor = color

        wfSliderAdapter.setFontColor(color)
    }

    private fun updateWatchface() {

        if (sessionManager.batterPercent.value ?: 0 <= minimumBatteryLevel) {
            showBatteryWarning()
            logErrorEvent(
                InsiderAppEvents.WatchFaceEvents.wnc_face_apply_failed_lb,
                "battery low warning"
            )
            return
        }

        val watchFaceToApply =
            mCustomWatchFaces?.returnValueIfExist(selectedWatchfacePosition) ?: return

        val customWatchface = CustomWatchFace(
            id = watchFaceToApply.id,
            binFile = watchFaceToApply.binFile,
            color = mSelectedFinalColor ?: Color.argb(255, 255, 255, 255),
            selectedImagePath = mSelectedImagePath,
            backgroundLayer = watchFaceToApply.backgroundLayer,
            textLayer = watchFaceToApply.textLayer
        )

        sessionManager.sendUpdateQueryAction(UpdateDeviceAction.SetWatchFaceCustom(customWatchface))
        showProgressDialog(
            getString(R.string.text_updating_your_watchface),
            getString(R.string.text_updating_wf),
            "Uploading…",
            null,
            null
        )
        progressBottomSheet?.setProgress(0)


    }

    fun earnRewardsPoints() {
        if (localDataStore.getIsWatchFaceRewardEarned()) return

        GlobalScope.launch {
            val request = JsonObject().apply {
                this.addProperty("task_enum", "1st_custom_watch-face")
            }
            rewardsRepository.earnRewardsPoints(request).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        localDataStore.setIsWatchFaceRewardEarned(true)
                    }

                    else -> {}
                }
            }
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