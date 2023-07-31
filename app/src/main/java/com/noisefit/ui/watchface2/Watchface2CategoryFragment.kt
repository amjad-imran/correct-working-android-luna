package com.noisefit.ui.watchface2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.data.remote.response.Watchface2
import com.noisefit.luna.databinding.FragmentWatchface2CategoryBinding
import com.noisefit.receiver.workManager.WatchFaceTransferStates
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.profile.BottomSheetImagePicker
import com.noisefit.ui.settings.helpAndSupport.HelpAndSupportType
import com.noisefit.ui.watchface.REQUEST_LAUNCH_IMAGE_CAPTURE
import com.noisefit.ui.watchface.REQUEST_LAUNCH_LIBRARY
import com.noisefit.ui.watchface2.bottom.UploadWatchfaceBottomSheet
import com.noisefit.ui.watchface2.bottom.UploadWatchfaceBottomSheetListener
import com.noisefit.ui.watchface2.sub.WF2SubListFrom
import com.noisefit.ui.watchfacenew.WatchFaceCategoryListingFragmentDirections
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.CommonConstants
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

@AndroidEntryPoint
class Watchface2CategoryFragment :
    BaseFragment<FragmentWatchface2CategoryBinding>(FragmentWatchface2CategoryBinding::inflate) {

    private var progressBottomSheet: UploadWatchfaceBottomSheet? = null
    private val viewModel: Watchface2CategoryViewModel by viewModels()

    private val watchface2CategoryAdapter: Watchface2CategoryAdapter by lazy {
        Watchface2CategoryAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)

        setAdapter()
        viewModel.getWatchfaceCategoriesV2()

        arguments?.getParcelable<Watchface2>("watchFace")?.let {
            startTransfer(it, -1)
            arguments?.clear()
        }
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

    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.lytToolbar.backBtn.performClick()
            }
        }

    private fun startTransfer(watchFace: Watchface2, position: Int) {

        if ((viewModel.sessionManager.batterPercent.value ?: 0) <= viewModel.minimumBatteryLevel) {
            showBatteryWarning()
            return
        }
        viewModel.refeshPosition = position
        viewModel.sessionManager.logInsiderAppEvent(
            InsiderAppEvents.WATCHFACES_OPEN,
            HashMap<String, Any>().apply {
                this["wf_name"] = watchFace.name
            })
        viewModel.sessionManager.logInsiderAppEvent(
            InsiderAppEvents.WATCHFACES_OPEN_CATEGORY,
            HashMap<String, Any>().apply {
                this["wf_cat_id"] = watchFace.cId
            })

        showProgressDialog(watchFace)

    }


    private fun setAdapter() {

        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = watchface2CategoryAdapter
        }

    }

    override fun initListener() {

        binding.lytToolbar.apply {
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
            tvTitle.text = getString(R.string.text_watchfaces)
        }
        binding.backFav.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCHFACES_MYFAVOURITES)
            moveToSubList(WF2SubListFrom.Favourite)
        }


        watchface2CategoryAdapter.itemClickListener = { item ->
            when (item) {
                is Watchface2ClickEnum.CreateYourOwnClick -> {
                    onCreateNowClicked()
                }

                is Watchface2ClickEnum.FilterClick -> {

                    val type = item.wF2SubListFrom
                    if (type == WF2SubListFrom.MyCreation) {
                        navigate(R.id.myCreationWfFragment)
                    } else {
                        moveToSubList(type)
                    }
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCHFACES_ + item.wF2SubListFrom.name)
                }

                is Watchface2ClickEnum.CategoryClicked -> {
                    moveToSubList(WF2SubListFrom.CATEGORIES, item.catId)
                    viewModel.sessionManager.logInsiderAppEvent(
                        InsiderAppEvents.WATCHFACES_CATEGORY_CLICK,
                        HashMap<String, Any>().apply {
                            this["category_name"] = item.catName
                        })

                }

                is Watchface2ClickEnum.LoadMoreWatchFaces -> {
                    if (viewModel.getLoading().value != true) {
                        viewModel.loadMoreWatchFaces(item.data)
                    }
                }

                is Watchface2ClickEnum.OnWatchMarkedFavourite -> {
                    viewModel.markFavourite(item.favourite, item.watchface)
                }

                is Watchface2ClickEnum.OnWatchfaceClicked -> {
                    startTransfer(item.watchface, item.position)
                }

                is Watchface2ClickEnum.HavingAnIssueClick -> {
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCHFACES_HAVINGISSUES)
                    navigate(
                        Watchface2CategoryFragmentDirections.actionWatchface2CategoryFragmentToHelpAndSupportListFragment(
                            null,
                            HelpAndSupportType.WATCHFACE_TRANSFER.name
                        )
                    )
                }
            }
        }

    }

    private fun onCreateNowClicked() {

        viewModel.viewModelScope.launch {
            activity?.let {
//                if (AppUtil.isWatchFaceTransferInProgress(it)) {
//                    context.showShortToast(getString(R.string.text_transfer_in_progress))//TODO change message
//                    return@launch
//                }


                when (viewModel.sessionManager.connectedDevice.value?.deviceType) {
                    DeviceType.COLORFIT_PRO_3.deviceType,
                    DeviceType.NOISEFIT_AGILE.deviceType,
                    DeviceType.NOISEFIT_AGILE_OTA.deviceType,
                    DeviceType.NOISEFIT_AGILE_DFU.deviceType,
                    DeviceType.NOISEFIT_ACTIVE.deviceType,
                    DeviceType.NOISEFIT_ACTIVE_OTA.deviceType -> {
                        showImagePicker()
                    }

                    DeviceType.COLORFIT_VISION.deviceType -> {
                        navigate(R.id.visionCustomWatchFaceFragment)
                    }

                    DeviceType.COLORFIT_NAV.deviceType -> {
                        navigate(R.id.nfhCustomWatchFaceFragment)
                    }

                    else -> {
                        if (viewModel.isWatchSupportDiy) {
                            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CUSTOMWF_CREATENOW)
                            navigate(R.id.diyWatchFaceFragment)
                        }
                    }
                }
            }
        }


    }

    private fun moveToSubList(wF2SubListFrom: WF2SubListFrom, catId: Int? = null) {
        navigate(
            Watchface2CategoryFragmentDirections.actionWatchface2CategoryFragmentToWatchface2SubListFragment(
                wF2SubListFrom
            ).apply {
                this.categoryId = catId ?: -1
            }
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


    override fun subscribeObservers() {
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.sessionManager.watchFaceTransferStates.observe(this) {
            it.getContent()?.let { event ->
                when (event) {
                    is WatchFaceTransferStates.Default -> {
                        LOGS.d("WATCHSTATE default")
                    }

                    is WatchFaceTransferStates.Failed -> {
                        dismissProgressBar()
                        LOGS.d("WATCHSTATE failed")
                        if (event.reason.isNotEmpty()) {
                            uiController.onDisplayError(event.reason)
                        }

                    }

                    is WatchFaceTransferStates.Progress -> {
                        progressBottomSheet?.setProgress(event.percent)
                    }

                    is WatchFaceTransferStates.Started -> {
                        LOGS.d("WATCHSTATE Started")

                    }

                    is WatchFaceTransferStates.Success -> {
                        dismissProgressBar()
                        LOGS.d("wf_name ${event.watchFace.wId}")
                        uiController.onDisplayError("WatchFace Transferred")
                        viewModel.sessionManager.logInsiderAppEvent(
                            InsiderAppEvents.WATCHFACES_TRANSFER,
                            HashMap<String, Any>().apply {
                                this["wf_name"] = event.watchFace.name
                                this["wf_cat_id"] = event.watchFace.cId
                                this["wf_timestamp"] =
                                    DateFormats.formatWfTransfer(System.currentTimeMillis()) ?: ""
                            })
                        if (!event.watchFace.isRated && !viewModel.localDataStore.checkWatchFaceRatedIdExist(event.watchFace.wId)) {
                            navigate(R.id.watchfaceRateBottomSheet, Bundle().apply {
                                this.putParcelable("watchFace", event.watchFace)
                                this.putString("watchFaceOpenType", "general")
                            })
                        }else{
                            viewModel.localDataStore.clearWatchFaceRatedId(event.watchFace.wId)
                        }

                    }

                    is WatchFaceTransferStates.Downloading -> {
                        LOGS.d("WATCHSTATE Downloading")
                        if (event.isCompleted) {
                            progressBottomSheet?.setState(getString(R.string.text_transferring__))
                        } else {
                            progressBottomSheet?.setState(getString(R.string.text_downloading__))
                        }

                    }

                    WatchFaceTransferStates.TransferStarted -> {
                        LOGS.d("WATCHSTATE TransferStarted")
                    }
                }

            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.categoryList.observe(this) {
            it?.let {
                watchface2CategoryAdapter.submitData(it)
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

    private fun showProgressDialog(watchFace: Watchface2) {

//        if (progressBottomSheet != null) {
//            return
//        }
        progressBottomSheet = UploadWatchfaceBottomSheet.getInstance(
            watchFace,
            false
        )
        progressBottomSheet?.isCancelable = false
        progressBottomSheet?.show(
            childFragmentManager,
            "DownloadBottomSheet"
        )

        progressBottomSheet?.setUploadWatchfaceBottomSheetListen(object :
            UploadWatchfaceBottomSheetListener {
            override fun startWatchfaceService() {
                viewModel.viewModelScope.launch {
                    activity?.let {

//                        if (AppUtil.isWatchFaceTransferInProgress(it)) {
//                            context.showShortToast(getString(R.string.text_transfer_in_progress))//TODO change message
//                            return@launch
//                        }

                        val isStarted = ApplicationUtils.startWatchFaceTransferWorker(it, watchFace)
                        if (!isStarted) {
                            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCHFACES_OPEN_UPLOAD)
                            context.showShortToast(getString(R.string.text_transfer_in_progress))
                        }
                    }
                }
            }

            override fun favMark(isFav: Boolean) {

                tryCatch {
                    if (isFav) {
                        watchFace.isFavourite = "1"
                        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCHFACES_OPEN_FAVOURITES_ADD)
                    } else {
                        watchFace.isFavourite = "0"
                        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCHFACES_OPEN_FAVOURITES_REMOVE)
                    }

                    if (viewModel.refeshPosition != -1) {
                        watchface2CategoryAdapter.notifyItemChanged(viewModel.refeshPosition)
                    }

                }

            }

        })
    }


}