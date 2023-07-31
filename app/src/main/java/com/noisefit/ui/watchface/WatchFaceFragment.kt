package com.noisefit.ui.watchface

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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentWatchFaceBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.profile.BottomSheetImagePicker
import com.noisefit.ui.watchface.adapter.WatchCatActions
import com.noisefit.ui.watchface.adapter.WatchFaceCategoryAdapter
import com.noisefit.ui.watchfacenew.WatchFaceCategoryListingFragmentDirections
import com.noisefit.util.AnalyticEventUtils
import com.noisefit_commans.utils.CommonConstants
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchForm
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.UpdateStatus
import com.noisefit_commans.models.WatchFace
import com.noisefit_commans.models.WatchUpdateStatus
import com.noisefit_commans.ui.*
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

@Deprecated("in use Watchface2CategoryFragment")
@AndroidEntryPoint
class WatchFaceFragment :
    BaseFragment<FragmentWatchFaceBinding>(FragmentWatchFaceBinding::inflate) {

    private val viewModel: WatchFaceCatListViewModel by viewModels()

    private var defaultEventProperty = HashMap<String, Any?>()


    private val recentAdapter: WatchFaceCategoryAdapter by lazy {
        WatchFaceCategoryAdapter(object : WatchCatActions {
            override fun onCategoryClicked(categoryId: Int, categoryName: String) {
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCHFACE_RECENTLY_USED_VIEW_ALL_CLICK)
            }

            override fun onWatchFaceClicked(watchFaceId: Int, watchFace: WatchFace) {
                viewModel.sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.WATCHFACE_RECENTLY_USED_ITEM_CLICK,
                    HashMap<String, Any>().apply {
                        this["watch_face_name"] = watchFace.name.toString()
                    }
                )
                this@WatchFaceFragment.onWatchFaceClicked(watchFaceId, watchFace)
            }

            override fun onMarkFavouriteClicked(
                favourite: Boolean,
                watchFace: WatchFace,
                innerPosition: Int,
                position: Int
            ) {
                viewModel.masterPosition = position
                viewModel.markFavourite(favourite, watchFace, innerPosition, FavMarkFrom.RECENT)
            }
        })
    }
    private val newlyAddedAdapter: WatchFaceCategoryAdapter by lazy {
        WatchFaceCategoryAdapter(object : WatchCatActions {
            override fun onCategoryClicked(categoryId: Int, categoryName: String) {
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCHFACE_NEWLY_ADDED_VIEW_ALL_CLICK)
                navigate(
                    WatchFaceCategoryListingFragmentDirections.actionWatchFaceCategoryListingFragmentToWatchFaceCategoryFragment(
                        categoryId,
                        categoryName
                    )
                )
            }

            override fun onWatchFaceClicked(watchFaceId: Int, watchFace: WatchFace) {
                viewModel.sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.WATCHFACE_NEWLY_ADDED_ITEM_CLICK,
                    HashMap<String, Any>().apply {
                        this["watch_face_name"] = watchFace.name.toString()
                    }
                )
                this@WatchFaceFragment.onWatchFaceClicked(watchFaceId, watchFace)
            }

            override fun onMarkFavouriteClicked(
                favourite: Boolean,
                watchFace: WatchFace,
                innerPosition: Int,
                position: Int
            ) {
                viewModel.masterPosition = position
                viewModel.markFavourite(
                    favourite,
                    watchFace,
                    innerPosition,
                    FavMarkFrom.NEWLY_ADDED
                )
            }
        })
    }
    private val topDownloadedAdapter: WatchFaceCategoryAdapter by lazy {
        WatchFaceCategoryAdapter(object : WatchCatActions {
            override fun onCategoryClicked(categoryId: Int, categoryName: String) {
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCHFACE_TOPDOWNLOADED_VIEW_ALL_CLICK)
                navigate(
                    WatchFaceCategoryListingFragmentDirections.actionWatchFaceCategoryListingFragmentToWatchFaceCategoryFragment(
                        categoryId,
                        categoryName
                    )
                )
            }

            override fun onWatchFaceClicked(watchFaceId: Int, watchFace: WatchFace) {
                viewModel.sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.WATCHFACE_TOPDOWNLOAD_ITEM_CLICK,
                    HashMap<String, Any>().apply {
                        this["watch_face_name"] = watchFace.name.toString()
                    }
                )
                this@WatchFaceFragment.onWatchFaceClicked(watchFaceId, watchFace)
            }

            override fun onMarkFavouriteClicked(
                favourite: Boolean,
                watchFace: WatchFace,
                innerPosition: Int,
                position: Int
            ) {
                viewModel.masterPosition = position
                viewModel.markFavourite(
                    favourite,
                    watchFace,
                    innerPosition,
                    FavMarkFrom.TOP_DOWNLOADED
                )
            }
        })
    }
    private val categoryAdapter: WatchFaceCategoryAdapter by lazy {
        WatchFaceCategoryAdapter(object : WatchCatActions {
            override fun onCategoryClicked(categoryId: Int, categoryName: String) {
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCHFACE_DESIGNWISE_VIEW_ALL_CLICK)
                navigate(R.id.watchFaceCategoriesFragment)
            }

            override fun onWatchFaceClicked(watchFaceId: Int, watchFace: WatchFace) {
                viewModel.sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.WATCHFACE_DESIGNWISE_ITEM_CLICK,
                    HashMap<String, Any>().apply {
                        this["watch_face_name"] = watchFace.name.toString()
                    }
                )
                this@WatchFaceFragment.onWatchFaceClicked(watchFaceId, watchFace)
            }

            override fun onMarkFavouriteClicked(
                favourite: Boolean,
                watchFace: WatchFace,
                innerPosition: Int,
                position: Int
            ) {
                viewModel.masterPosition = position
                viewModel.markFavourite(
                    favourite,
                    watchFace,
                    innerPosition,
                    FavMarkFrom.CATEGORY_RANDOM
                )
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.getWatchFaceCustomData(false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCH_FACES_DETAILS_PAGE_VISIT)

        binding.containerCustomWatchFace.visible()
        if (viewModel.watchesSDK.getWatchForm() == WatchForm.CIRCLE) {
            binding.ivWatchFace.loadCircleImage(
                requireActivity(),
                R.drawable.circle_create_your_own_
            )
        }else{
            binding.ivWatchFace.loadImage(
                requireActivity(),
                R.drawable.rectangle_create_your_own_
            )
        }

        when (viewModel.sessionManager.connectedDevice.value?.deviceType) {
            DeviceType.NOISEFIT_EVOLVE.deviceType -> {
                binding.containerCustomWatchFace.gone()
            }
            DeviceType.NOISE_EVOLVE_2.deviceType -> {
                viewModel.watchDataStore.getDeviceFirmwareDetails()?.let { detail ->
                    if (detail.version >= 37) {
                        binding.containerCustomWatchFace.visible()
                    } else {
                        binding.containerCustomWatchFace.gone()
                    }
                }
            }

            else -> {

            }
        }


    }

    private fun setRecyclerView() {
        binding.rvNewlyAdded.layoutManager = LinearLayoutManager(context)
        binding.rvTopDownloaded.layoutManager = LinearLayoutManager(context)
        binding.rvRecent.layoutManager = LinearLayoutManager(context)
        binding.rvCatList.layoutManager = LinearLayoutManager(context)


        binding.rvNewlyAdded.adapter = newlyAddedAdapter
        binding.rvTopDownloaded.adapter = topDownloadedAdapter
        binding.rvRecent.adapter = recentAdapter
        binding.rvCatList.adapter = categoryAdapter
    }

    override fun initListener() {

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.getWatchFaceCustomData(true)
        }

        binding.bContinue.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCH_FACES_CUSTOM_CREATE_NOW_CLICK)

            val sdkWatchType = viewModel.watchesSDK.getWatchType()
            when (viewModel.sessionManager.connectedDevice.value?.deviceType) {
                DeviceType.COLORFIT_PRO_3.deviceType,
                DeviceType.NOISEFIT_AGILE.deviceType,
                DeviceType.NOISEFIT_AGILE_OTA.deviceType,
                DeviceType.NOISEFIT_AGILE_DFU.deviceType,
                DeviceType.NOISEFIT_ACTIVE.deviceType,
                DeviceType.NOISEFIT_ACTIVE_OTA.deviceType -> {
                    showImagePicker()
                }
                DeviceType.COLORFIT_VISION.deviceType ->{
                    navigate(R.id.visionCustomWatchFaceFragment)
                }
                DeviceType.COLORFIT_NAV.deviceType-> {
                    navigate(R.id.nfhCustomWatchFaceFragment)
                }
                else -> {
                    if (sdkWatchType == SDKWatchType.SDK_ZH ||
                        sdkWatchType == SDKWatchType.SDK_NAV_PLUS ||
                        sdkWatchType == SDKWatchType.SDK_RYEEX ||
                        sdkWatchType == SDKWatchType.SDK_EVOLVE
                    ) {
                        navigate(R.id.diyWatchFaceFragment).apply {

                        }
                    }
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

        viewModel.recentList.observe(this) {
            if (it.isEmpty()) {
                binding.rvRecent.gone()
            } else {
                if (it[0].faces.isEmpty()) {
                    binding.rvRecent.gone()
                } else {
                    binding.rvRecent.visible()
                }
                recentAdapter.setDataSet(it)
            }
        }
        viewModel.newlyAddedList.observe(this) {
            if (it.isEmpty()) {
                binding.rvNewlyAdded.gone()
            } else {
                if (it[0].faces.isEmpty()) {
                    binding.rvNewlyAdded.gone()
                } else {
                    binding.rvNewlyAdded.visible()
                }
                newlyAddedAdapter.setDataSet(it)
            }
        }

        viewModel.topDownloadedList.observe(this) {
            if (it.isEmpty()) {
                binding.rvTopDownloaded.gone()
            } else {
                if (it[0].faces.isEmpty()) {
                    binding.rvTopDownloaded.gone()
                } else {
                    binding.rvTopDownloaded.visible()
                }
                topDownloadedAdapter.setDataSet(it)
            }
        }

        viewModel.randomCategoryList.observe(this) {
            if (it.isEmpty()) {
                binding.rvCatList.gone()
            } else {
                if (it[0].faces.isEmpty()) {
                    binding.rvCatList.gone()
                } else {
                    binding.rvCatList.visible()
                }
                categoryAdapter.setDataSet(it)
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
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

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }


        viewModel.resetFavouriteState.observe(this) {
            it.getContent()?.let { state ->
                when (viewModel.favMarkedFrom) {
                    FavMarkFrom.RECENT -> {
                        recentAdapter.resetFavState(
                            state,
                            viewModel.currentPosition,
                            viewModel.masterPosition
                        )
                    }
                    FavMarkFrom.TOP_DOWNLOADED -> {
                        topDownloadedAdapter.resetFavState(
                            state,
                            viewModel.currentPosition,
                            viewModel.masterPosition
                        )
                    }
                    FavMarkFrom.NEWLY_ADDED -> {
                        newlyAddedAdapter.resetFavState(
                            state,
                            viewModel.currentPosition,
                            viewModel.masterPosition
                        )
                    }
                    FavMarkFrom.CATEGORY_RANDOM -> {
                        categoryAdapter.resetFavState(
                            state,
                            viewModel.currentPosition,
                            viewModel.masterPosition
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    fun onWatchFaceClicked(watchFaceId: Int, watchFace: WatchFace) {
        navigate(
            WatchFaceCategoryListingFragmentDirections.actionNavigationWatchfaceToWatchFaceUpdateFragment(
                watchFaceId
            )
        )
    }

    private fun showImagePicker() {
        parentFragment?.parentFragmentManager?.setFragmentResultListener(
            BottomSheetImagePicker.IMAGE_PICKER_RESULT,
            this
        ) { key, bundle ->
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

        navigate(WatchFaceCategoryListingFragmentDirections.actionNavigationWatchfaceToBottomSheetImagePicker())

    }

    private fun updateWatchFaceStatus(watchUpdateStatus: WatchUpdateStatus) {
        when (watchUpdateStatus.status) {
            UpdateStatus.STARTED -> {
                defaultEventProperty =
                    viewModel.analyticEventUtils.getEventProperty(type = AnalyticEventUtils.CustomWatchFace)

                progressBottomSheet?.setProgress(watchUpdateStatus.percentagePercentage ?: 0)
            }
            UpdateStatus.PROGRESS -> {
                progressBottomSheet?.setProgress(watchUpdateStatus.percentagePercentage ?: 0)
            }
            UpdateStatus.COMPLETED -> {

                progressBottomSheet?.dismiss()
                context.showShortToast("Watch Face Transferred")
                viewModel.deleteTempFile()
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

                //viewModel.saveCurrentWatchface()
                viewModel.sessionManager.transferInProgress = false
                //sessionManager.showReview.postValue(Event(true))

                val count = viewModel.localDataStore.getWatchFaceTransferCount()
                val newCount = count + 1
                viewModel.localDataStore.setWatchFaceTransferCount(newCount)
                viewModel.earnRewardsPoints()
            }
            UpdateStatus.ERROR -> {

                viewModel.sessionManager.transferInProgress = false
                progressBottomSheet?.dismiss()
                viewModel.sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.WATCH_FACES_UPDATED_CLICK,
                    HashMap<String, Any>().apply {
                        this["watch_face_name"] =
                            viewModel.watchFace.value?.name.toString()
                        this["watch_face_update_status"] = false
                        this["detail"] = viewModel.watchFace.value?.id.toString() + "\n" +
                                viewModel.watchFace.value?.name.toString() + "\n" +
                                UpdateStatus.ERROR.name
                    }
                )
                context.showShortToast("Failed")
                viewModel.deleteTempFile()
            }
            else -> {}
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
                    val resultUri = data?.let { UCrop.getOutput(it) }

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
            viewModel.watchFace.value?.name
        )
        progressBottomSheet?.setProgress(0)
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

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearCustomData()
    }

    fun logInsiderAppEventData() {

    }

}