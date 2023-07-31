package com.noisefit.ui.diy

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.data.model.DiyBackground
import com.noisefit.data.model.DiyCustomWatchType
import com.noisefit.data.model.DiyWatchFaceModal
import com.noisefit.data.remote.response.DiyMyCreation
import com.noisefit.luna.databinding.FragmentDiyWatchFaceBinding
import com.noisefit.receiver.workManager.DiyWatchFaceTransferStates
import com.noisefit.ui.common.bottomSheet.ALERT_REQUEST_KEY
import com.noisefit_commans.ui.visible
import com.noisefit.ui.profile.BottomSheetImagePicker

import com.noisefit.ui.watchface.REQUEST_LAUNCH_IMAGE_CAPTURE
import com.noisefit.ui.watchface.REQUEST_LAUNCH_LIBRARY
import com.noisefit.ui.watchface2.bottom.MyDiyCreationDetailBottomSheet
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.models.DiyCustomWatchFace
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.CommonConstants
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.share.ShareUtil
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

@AndroidEntryPoint
class DiyWatchFaceFragment :
    BaseFragment<FragmentDiyWatchFaceBinding>(FragmentDiyWatchFaceBinding::inflate) {

    private var progressBottomSheet: MyDiyCreationDetailBottomSheet? = null
    private val viewModel: DiyWatchFaceViewModel by viewModels()
    private val args: DiyWatchFaceFragmentArgs? by navArgs()
    private val diyWatchFaceAdapter: DiyWatchFaceAdapter by lazy {
        DiyWatchFaceAdapter()
    }

    private val diyWatchTypeAdapter: DiyWatchTypeAdapter by lazy {
        DiyWatchTypeAdapter(object : DiyWatchTypeAdapter.DiyWatchTypeListener {
            override fun onWatchFaceClicked(diyCustomWatchType: DiyCustomWatchType, position: Int) {
                viewModel.updatePositionList.clear()
                viewModel.setFilterType(diyCustomWatchType.filterType)
            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        args?.let {
            viewModel.alreadyCreatedDiyMyCreation = args!!.wfData
        }
        setAdapter()
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)
    }

    private fun handleBackBtn() {
        if (viewModel.hasUserChangedSomething) {
            navigate(
                DiyWatchFaceFragmentDirections.actionDiyWatchFaceFragmentToAlertTextBottomSheet(
                    getString(R.string.text_progress_will_be_lost),
                    getString(R.string.text_are_you_want_leave),
                    getString(R.string.text_leave),
                    getString(R.string.text_continue)
                )
            )
            return
        }
        navigateUpSafe()
    }

    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackBtn()
            }
        }

    private fun setAdapter() {

        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = diyWatchFaceAdapter
            itemAnimator = null
        }

        binding.typeRv.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = diyWatchTypeAdapter
            itemAnimator = null
        }


    }

    private fun setBgImage(data: DiyBackground) {

        if (!data.diyCustomWatchFaceBg?.bgLink.isNullOrEmpty()) {
            viewModel.setFilter(data, binding.bgImv, data.diyCustomWatchFaceBg?.bgLink, false)
        }

        binding.lytLineSeparator.root.visible()
        binding.lytLineSeparator2.root.visible()
        data.diyCustomWatchFaceBg?.color?.let {
            if (it == 0) {
                binding.bgLayerImv.setColorFilter(Color.argb(255, 255, 255, 255))//WHITE
            } else {
                binding.bgLayerImv.setColorFilter(it)
            }
        }


        if (!data.diyCustomWatchFaceBg?.textLayerLink.isNullOrEmpty()) {
            viewModel.setFilter(
                data,
                binding.bgLayerImv,
                data.diyCustomWatchFaceBg?.textLayerLink,
                true
            )
        }
    }


    override fun initListener() {

        setFragmentResultListener(ALERT_REQUEST_KEY) { _, bundle ->
            val allow = bundle.getBoolean("allow")
            if (allow) {
                navigateUpSafe()
            }
        }

        binding.btnUpload.setOnClickListener {
            viewModel.pairingTimeTaken = System.currentTimeMillis()
            //viewModel.logApiStartEvent(InsiderAppEvents.WatchFaceEvents.wface_apply)

            if ((viewModel.sessionManager.batterPercent.value
                    ?: 0) <= viewModel.minimumBatteryLevel
            ) {
                showBatteryWarning()

                return@setOnClickListener
            }


            setWatchface()

        }
        binding.backBtn.setOnClickListener {
            handleBackBtn()
        }

        diyWatchFaceAdapter.itemClickListener = { item ->
            viewModel.updatePositionList.clear()
            viewModel.hasUserChangedSomething = true
            when (item) {
                is DiyClickEnum.BgListImageClick -> {
                    viewModel.updatePositionList.addAll(listOf(0, 1))
                    if (item.data.name.lowercase() == "upload") {
                        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CUSTOMWF_BACKGROUND_UPLOAD)
                        openImagePicker()
                    } else {
                        viewModel.reset()
                        viewModel.updateList(imageUri = item.data.bgLink, resetFeaturedImage = true)

                    }
                }

                is DiyClickEnum.FeaturedImageClick -> {
                    viewModel.updatePositionList.addAll(listOf(0))
                    viewModel.reset()
                    viewModel.updateList(imageUri = item.data.bgLink, resetMyImages = true)
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CUSTOMWF_FEATUREDIMAGE_ + item.data.name)
                    viewModel.selectedBackgroundType="Featured"
                    viewModel.selectedFeatureImageName=item.data.name
                }

                is DiyClickEnum.FilterImageClick -> {
                    viewModel.updatePositionList.add(-1)
                    var filterReset = false
                    if (item.data.colorMatrix == null) {
                        filterReset = true

                    }

                    viewModel.updateList(
                        filterName = item.data.name,
                        filterRest = filterReset
                    )
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CUSTOMWF_FILTERS_ + item.data.name)
                    viewModel.selectedFilterName=item.data.name

                }

                is DiyClickEnum.FilterIntensityClick -> {
                    viewModel.updatePositionList.add(0)
                    viewModel.updateList(colorIntensity = item.percentage)
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CUSTOMWF_FILTERS_INTENSITY)
                    viewModel.selectedFilterIntensityValue=item.percentage
                }

                is DiyClickEnum.ColorClick -> {
                    viewModel.updatePositionList.add(0)
                    viewModel.updateList(textColor = item.color)
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CUSTOMWF_TEXTCOLOUR_ + viewModel.colorHaxCodeList[item.position])
                    viewModel.selectedTextColorCode=viewModel.colorHaxCodeList[item.position]

                }

                is DiyClickEnum.PlacementImageClick -> {
                    viewModel.reset()
                    viewModel.currentDiyCustomWatchFaceBg.textLayerName = item.data.name
                    viewModel.updatePositionList.add(-1)
                    viewModel.updateList(
                        textLayer1 = item.data.textLayerLink,
                        binUrl = item.data.binUrl
                    )
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CUSTOMWF_PLACEMENT_ + item.data.name)
                    viewModel.selectedPlacement=item.data.name

                }

                is DiyClickEnum.FontClick -> {
                    viewModel.updatePositionList.add(0)
                    viewModel.currentDiyCustomWatchFaceBg.fontStyleName = item.data.name
                    viewModel.updateList(
                        textLayer1 = item.data.bgLink,
                        binUrl = item.data.binUrl,
                        selectedFontStyleChange = true
                    )
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CUSTOMWF_TEXTSTYLE_ + item.data.name)
                    viewModel.selectedTextStyle=item.data.name
                }
            }
        }

    }

    private fun openImagePicker() {
        navigate(DiyWatchFaceFragmentDirections.actionDiyWatchFaceFragmentToBottomSheetImagePicker())
    }


    override fun subscribeObservers() {

        viewModel.setDiyCustomWatchFace.observe(this) {
            it?.getContent()?.let {
                showProgressDialog(it.first, it.second)

            }
        }
        viewModel.bgDiyImage.observe(this) {
            it?.let {
                setBgImage(it)
            }
        }

        viewModel.sessionManager.diyWatchFaceTransferStates.observe(this) {
            it.getContent()?.let { event ->
                when (event) {
                    is DiyWatchFaceTransferStates.Default -> {
                        LOGS.d("WATCHSTATE default")
                    }

                    is DiyWatchFaceTransferStates.Failed -> {
                        viewModel.hasUserChangedSomething = false
                        dismissProgressBar()
                        LOGS.d("WATCHSTATE failed")
                        uiController.onDisplayError(event.reason)
                    }

                    is DiyWatchFaceTransferStates.Progress -> {
                        progressBottomSheet?.setProgress(event.percent)
                    }

                    is DiyWatchFaceTransferStates.Started -> {
                        LOGS.d("WATCHSTATE Started")

                    }

                    is DiyWatchFaceTransferStates.Success -> {
                        viewModel.hasUserChangedSomething = false
                        dismissProgressBar()
                        event.watchFace.screenType
                        uiController.onDisplayError("WatchFace Transferred")
                        viewModel.logInsiderEvent()

                        showRatingPopUp()
                    }

                    is DiyWatchFaceTransferStates.Downloading -> {
                        LOGS.d("WATCHSTATE Downloading")
                        if (event.isCompleted) {
                            progressBottomSheet?.setState(getString(R.string.text_transferring__))
                        } else {
                            progressBottomSheet?.setState(getString(R.string.text_downloading__))
                        }

                    }

                    DiyWatchFaceTransferStates.TransferStarted -> {
                        LOGS.d("WATCHSTATE TransferStarted")
                    }
                }

            }
        }








        setFragmentResultListener(BottomSheetImagePicker.IMAGE_PICKER_RESULT) { key, bundle ->
            val value = bundle.getInt("selectedValue")
            if (value == 0) {
                val libraryIntent = Intent(Intent.ACTION_PICK)
                libraryIntent.type = "image/*"
                startActivityForResult(
                    Intent.createChooser(libraryIntent, null),
                    viewModel.REQUEST_LAUNCH_LIBRARY
                )
            } else {
                checkCameraPermission {
                    dispatchTakePictureIntent()
                }
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.filterType.observe(this) {
            it?.let {
                if (diyWatchTypeAdapter.itemCount == 0) {
                    binding.btnUpload.visible()
                    diyWatchTypeAdapter.setDataSet(viewModel.getDiyType())
                }
                handleList(it)
            }
        }
        viewModel.updateRv.observe(this) {
            it?.let {
                if (it) {
                    handleList(viewModel.filterType.value!!)
                }
            }
        }
    }

    private fun handleList(filterType: FilterType) {
        val dataList = ArrayList<DiyWatchFaceModal>()
        viewModel.diyWatchFaceList.forEach { diyWatchFaceModal ->
            when (diyWatchFaceModal) {
                is DiyWatchFaceModal.BackgroundList -> {
                    if (filterType == diyWatchFaceModal.filterType) {
                        dataList.add(diyWatchFaceModal)
                    }
                }

                is DiyWatchFaceModal.FeaturedList -> {
                    if (filterType == diyWatchFaceModal.filterType) {
                        dataList.add(diyWatchFaceModal)
                    }
                }

                is DiyWatchFaceModal.ColorList -> {
                    if (filterType == diyWatchFaceModal.filterType) {
                        dataList.add(diyWatchFaceModal)
                    }
                }

                is DiyWatchFaceModal.FilterList -> {
                    if (filterType == diyWatchFaceModal.filterType) {
                        dataList.add(diyWatchFaceModal)
                    }
                }

                is DiyWatchFaceModal.FontList -> {
                    if (filterType == diyWatchFaceModal.filterType) {
                        dataList.add(diyWatchFaceModal)
                    }
                }

                is DiyWatchFaceModal.PlacementList -> {
                    if (filterType == diyWatchFaceModal.filterType) {
                        dataList.add(diyWatchFaceModal)
                    }
                }
            }
        }

        if (viewModel.updatePositionList.isNotEmpty()) {
            if (viewModel.updatePositionList.first() == -1) {

                return
            }

            diyWatchFaceAdapter.submitData(dataList, viewModel.updatePositionList)
        } else {
            diyWatchFaceAdapter.submitData(dataList)
        }
    }

    private fun showRatingPopUp() {
        if (viewModel.localDataStore.getCustomWatchFaceTransferCount() == 1) {
            setFragmentResultListener("RATE_NOW") { key, bundle ->
                val isSelected = bundle.getBoolean("isSelected")
                if (isSelected) {
                    viewModel.localDataStore.setShowReviewPopUp(false)
                    ShareUtil.openPlayStore(requireContext(), "com.noisefit")
                    navigateUpSafe()
                }
            }
            setFragmentResultListener("LATER") { key, bundle ->
                val isSelected = bundle.getBoolean("isSelected")
                if (isSelected) {
                    viewModel.localDataStore.setLaterNowLastReviewShownTimeStamp(System.currentTimeMillis())
                    viewModel.localDataStore.setShowReviewPopUp(true)
                    navigateUpSafe()
                }
            }
            navigate(R.id.rateNowBottomSheet,Bundle().apply {
                putString("cameFrom","customwatch")
            })
        }
    }


    private fun setWatchface() {


        val widthHeight = viewModel.widthHeight
        LOGS.d("setWatchfaceinside ${widthHeight.first} ------  ${widthHeight.second}")
        val watchFaceModal = viewModel.bgDiyImage.value
        val selectedDiyCustomWatchFaceBg = viewModel.currentDiyCustomWatchFaceBg

        if (selectedDiyCustomWatchFaceBg.color == 0 || selectedDiyCustomWatchFaceBg.color == -1) {
            selectedDiyCustomWatchFaceBg.color = Color.argb(255, 255, 255, 255)
        }
        val diyCustomWatchFace = DiyCustomWatchFace(
            binFile = viewModel.currentDiyCustomWatchFaceBg.binUrl,
            textLayerName = selectedDiyCustomWatchFaceBg.textLayerName!!,
            color = selectedDiyCustomWatchFaceBg.color,
            image = watchFaceModal?.bgBitmap,
            textLayer = watchFaceModal?.textBitmap,
            screenType = viewModel.screenType.type,
            width = widthHeight.first,
            height = widthHeight.second
        )

        LOGS.d("sdasdsdasda ${selectedDiyCustomWatchFaceBg.bgLink}")
        var filterName = selectedDiyCustomWatchFaceBg.filterName
        if (filterName.isNullOrEmpty()) {
            filterName = "none"
        }


        val diyCreation = DiyMyCreation(
            1,
            selectedDiyCustomWatchFaceBg.bgLink!!,
            selectedDiyCustomWatchFaceBg.filterIntensity,
            selectedDiyCustomWatchFaceBg.textLayerName!!,
            selectedDiyCustomWatchFaceBg.textLayerLink!!,
            selectedDiyCustomWatchFaceBg.binUrl!!,
            selectedDiyCustomWatchFaceBg.fontStyleName ?: "",
            java.lang.String.format("#%06X", 0xFFFFFF and selectedDiyCustomWatchFaceBg.color),
            filterName
        )

        viewModel.createDiyOnline(
            diyCreation,
            diyCustomWatchFace
        )


    }


    private fun showBatteryWarning() {
        val alertMessage =
            getString(R.string.text_battery_low_watchface, viewModel.minimumBatteryLevel)
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


    private fun showProgressDialog(
        diyMyCreation: DiyMyCreation,
        diyCustomWatchFace: DiyCustomWatchFace
    ) {
        if (progressBottomSheet == null) {
            progressBottomSheet = MyDiyCreationDetailBottomSheet.getInstance(
                diyMyCreation,
                true
            )

            progressBottomSheet?.isCancelable = false
            progressBottomSheet?.show(
                childFragmentManager,
                "DownloadBottomSheet"
            )

            viewModel.viewModelScope.launch {
                activity?.let {


                    val isStarted = ApplicationUtils.startDiyWatchFaceTransferWorker(it, diyCustomWatchFace)
                    if (!isStarted) {
                        context.showShortToast(getString(R.string.text_transfer_in_progress))
                    }
                }
            }
        }


    }

    private fun dismissProgressBar() {
        progressBottomSheet?.dismissAllowingStateLoss()
        progressBottomSheet = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressBottomSheet?.dismissAllowingStateLoss()
        progressBottomSheet = null
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
                    viewModel.photoURI = FileProvider.getUriForFile(
                        requireContext(), CommonConstants.FILE_PROVIDER, it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, viewModel.photoURI)
                    startActivityForResult(
                        takePictureIntent,
                        viewModel.REQUEST_LAUNCH_IMAGE_CAPTURE
                    )
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

        val randomChildName = "${System.currentTimeMillis()}.png"
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_LAUNCH_IMAGE_CAPTURE -> {
                    if (viewModel.photoURI == null) return
                    viewModel.outputUri = Uri.fromFile(File(context?.cacheDir, randomChildName))
                    UCrop.of(viewModel.photoURI!!, viewModel.outputUri!!)
                        .withAspectRatio(
                            viewModel.widthHeight.first.toFloat(),
                            viewModel.widthHeight.second.toFloat()
                        )
                        .withOptions(uCropOptions)
                        .withMaxResultSize(
                            viewModel.widthHeight.first,
                            viewModel.widthHeight.second
                        )
                        .start(requireActivity(), this)
                    viewModel.selectedImageType="Camera"
                    viewModel.selectedBackgroundType="Manual"
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CUSTOMWF_BACKGROUND_CAMERA)
                }

                REQUEST_LAUNCH_LIBRARY -> {
                    val selectedImageUri: Uri = data?.data ?: return

                    viewModel.outputUri = Uri.fromFile(File(context?.cacheDir, randomChildName))
                    UCrop.of(selectedImageUri, viewModel.outputUri!!)
                        .withAspectRatio(
                            viewModel.widthHeight.first.toFloat(),
                            viewModel.widthHeight.second.toFloat()
                        )
                        .withOptions(uCropOptions)
                        .withMaxResultSize(
                            viewModel.widthHeight.first,
                            viewModel.widthHeight.second
                        )
                        .start(requireActivity(), this)

                    viewModel.selectedImageType="Gallery"
                    viewModel.selectedBackgroundType="Manual"
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CUSTOMWF_BACKGROUND_GALLERY)
                }

                UCrop.REQUEST_CROP -> {
                    val resultUri = data?.let { UCrop.getOutput(it) }
                    if (resultUri != null) {
                        viewModel.updateList(
                            resultUri.toString(),
                            null,
                            isCustomImage = true,
                            resetFeaturedImage = true
                        )

                    }
                }
            }
        }
    }

}