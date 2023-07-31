package com.noisefit.ui.watchface

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.luna.databinding.FragmentWatchFaceUpdateBinding
import com.noisefit.receiver.service.FeedbackSubmitService
import com.noisefit.receiver.service.ProblemType
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.watchface.adapter.WatchFaceActions
import com.noisefit.ui.watchface.adapter.WatchFaceListAdapter
import com.noisefit.util.AnalyticEventUtils
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.UpdateStatus
import com.noisefit_commans.models.WatchFace
import com.noisefit_commans.models.WatchUpdateStatus
import com.noisefit_commans.utils.LOGS
import com.varunest.sparkbutton.SparkEventListener
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@AndroidEntryPoint
class WatchFaceUpdateFragment :
    BaseFragment<FragmentWatchFaceUpdateBinding>(FragmentWatchFaceUpdateBinding::inflate),
    WatchFaceActions {

    private val viewModel: WatchFaceUpdateViewModel by viewModels()

    private val sharedViewModel: WatchFaceCatListViewModel by viewModels()

    private var defaultEventProperty = HashMap<String, Any?>()

    @Inject
    lateinit var analyticEventUtils: AnalyticEventUtils

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var watchesSDK: WatchesSDK

    private var minimumBatteryLevel = 30

    private val adapter: WatchFaceListAdapter by lazy {
        WatchFaceListAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //binding.toolbar.tvTitle.text = getString(R.string.text_select_watchface).uppercase()

        viewModel.watchFaceId =
            arguments?.let { WatchFaceUpdateFragmentArgs.fromBundle(it).watchFaceId }

        minimumBatteryLevel = watchesSDK.getMinimumBatteryLevel()

        setRecycler()
        viewModel.watchFaceId?.let { viewModel.getWatchFaceData(it) }


        if (BuildConfig.DEBUG) {
            binding.tvFeedBack.visible()
            binding.tvFeedBack.setOnClickListener {
                navigate(R.id.feedbackFragment)
            }
        } else {
            binding.tvFeedBack.gone()
        }

    }

    private fun setRecycler() {
        binding.rvSimilarWatchFace.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvSimilarWatchFace.adapter = adapter
    }

    private fun logErrorEvent(eventName: String, status: String) {
        val timeTaken = System.currentTimeMillis() - viewModel.pairingTimeTaken
        sessionManager.logInsiderAppEvent(eventName,
            HashMap<String, Any>().apply {
                this["status"] = status
                this["wId"] = viewModel.watchFaceId ?: -1
                this["time"] = TimeUnit.MILLISECONDS.toSeconds(timeTaken)
                this["wName"] = viewModel.watchFaceName ?: ""
            })
    }

    private fun logApiStartEvent(eventName: String) {
        val connectedDevice = viewModel.localDataStore.getConnectedDevice() ?: return

        sessionManager.logInsiderAppEvent(eventName,
            HashMap<String, Any>().apply {
                this["wId"] = viewModel.watchFaceId ?: -1
                this["wName"] = viewModel.watchFaceName ?: ""
                this["dName"] = connectedDevice?.bluetoothName ?: ""
                this["dMac"] = connectedDevice?.address ?: ""
                this["supplier"] = viewModel.watchesSDK.getWatchType(connectedDevice).name
            })
    }

    override fun initListener() {

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.bUpdateWatchFace.setOnClickListener {
            viewModel.pairingTimeTaken = System.currentTimeMillis()
            logApiStartEvent(InsiderAppEvents.WatchFaceEvents.wn_face_apply)
            if (!sessionManager.isDeviceConnected()) {
                logErrorEvent(
                    InsiderAppEvents.WatchFaceEvents.wn_face_apply_failed_nc,
                    getString(R.string.text_device_not_connected)
                )
                context.showShortToast(getString(R.string.text_device_not_connected))
                return@setOnClickListener
            }
            viewModel.watchFaceId?.let { it1 ->

                viewModel.watchFace.value?.zip_file?.let { it2 ->
                    binding.bUpdateWatchFace.gone()
                    updateWatchface(it2)
                }

            }
        }

        binding.ivFavourite.setEventListener(object : SparkEventListener {
            override fun onEvent(button: ImageView?, buttonState: Boolean) {
                viewModel.watchFace.value?.let { viewModel.markFavourite(buttonState, it) }
            }

            override fun onEventAnimationEnd(button: ImageView?, buttonState: Boolean) {
            }

            override fun onEventAnimationStart(button: ImageView?, buttonState: Boolean) {}
        })

    }

    override fun subscribeObservers() {

        viewModel.markedFavourite.observe(this) {
            it.getContent()?.let { isFavourite ->
                sharedViewModel.markFavouriteLocally(
                    isFavourite,
                    viewModel.watchFaceIdFavourite ?: -1
                )
            }


        }

        viewModel.setWatchFaceButtonVisibility.observe(this) {
            it.getContent()?.let { visibility ->
                if (visibility) {
                    binding.bUpdateWatchFace.visible()
                } else {
                    binding.bUpdateWatchFace.gone()
                }
            }
        }

        viewModel.watchFace.observe(this) {
            binding.layoutMain.visible()
            binding.bUpdateWatchFace.visible()
            setWatchUi(it)

            sharedViewModel.updateDownloadCountLocally(it.downloads, viewModel.watchFaceId)

        }
        viewModel.similarWatchFace.observe(this) {
            if (it.isEmpty()) {
                binding.textView19.gone()
            } else {
                binding.textView19.visible()
            }
            adapter.setDataSet(it, 0)
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.watchFaceDownloadInfo.observe(this) {
            it.getContent()?.let { info ->
                //updateWatchface(info)
            }
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        viewModel.watchFaceDownloadProgress.observe(this) {
            it.getContent()?.let { progress ->

                if (progress == 100) {
                    progressBottomSheet?.dismiss()
                } else {
                    if (progressBottomSheet == null) {
                        showProgressDialog(
                            "Downloading Watchface",
                            getString(R.string.text_downloading_watchface),
                            "Downloading...",
                            viewModel.watchFace.value?.imageUrl,
                            viewModel.watchFace.value?.name
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
                sessionManager.sendUpdateQueryAction(UpdateDeviceAction.SetWatchFace(viewModel.watchFace.value!!.apply {
                    this.localFilePath = fileUri
                }))

                defaultEventProperty =
                    analyticEventUtils.getEventProperty(type = AnalyticEventUtils.CloudWatchFace)

                showProgressDialog(
                    getString(R.string.text_updating_your_watchface),
                    getString(R.string.text_updating_wf),
                    "Uploading…",
                    viewModel.watchFace.value?.imageUrl,
                    viewModel.watchFace.value?.name
                )
                progressBottomSheet?.setProgress(0)
                logApiStartEvent(InsiderAppEvents.WatchFaceEvents.wn_face_start)
            }
        }

        sessionManager.updateDeviceCallback.observe(this) { event ->
            event.getContent()?.let {
                if (it is UpdateDeviceDataCallback.CustomizeWatchFaceProgress) {

                    updateWatchFaceStatus(it.watchUpdateStatus)

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
                binding.ivFavourite.isChecked = state
            }
        }

        viewModel.downloadCancelled.observe(this) {
            it.getContent()?.let { isCancelled ->
                if (isCancelled) {
                    binding.bUpdateWatchFace.visible()
                    progressBottomSheet?.dismiss()
                    progressBottomSheet = null
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

                binding.bUpdateWatchFace.visible()
                progressBottomSheet?.dismiss()
                viewModel.setWatchFaceDownload()
                logErrorEvent(InsiderAppEvents.WatchFaceEvents.wn_face_complete, "complete")
                context.showShortToast("Watch Face Transferred")
                viewModel.deleteTempFile()
                sessionManager.transferInProgress = false
                //sessionManager.showReview.postValue(Event(true))
                viewModel.earnRewardsPoints()

                val count = localDataStore.getWatchFaceTransferCount()
                val newCount = count + 1
                localDataStore.setWatchFaceTransferCount(newCount)
                sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.WATCH_FACES_UPDATED_CLICK,
                    java.util.HashMap<String, Any>().apply {
                        this["watch_face_name"] =
                            viewModel.watchFaceName.toString()
                        this["watch_face_update_status"] = true
                        this["detail"] = viewModel.watchFace.value?.id.toString() + "\n" +
                                viewModel.watchFaceName + "\n" +
                                UpdateStatus.COMPLETED.name
                    }
                )

            }

            UpdateStatus.ERROR -> {

                binding.bUpdateWatchFace.visible()
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
                logErrorEvent(InsiderAppEvents.WatchFaceEvents.wn_face_failed, status)
                viewModel.deleteTempFile()
                sessionManager.transferInProgress = false
                sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.WATCH_FACES_UPDATED_CLICK,
                    java.util.HashMap<String, Any>().apply {
                        this["watch_face_name"] =
                            viewModel.watchFaceName.toString()
                        this["watch_face_update_status"] = false
                        this["detail"] = viewModel.watchFaceId.toString() + "\n" +
                                viewModel.watchFaceName.toString() + "\n" +
                                UpdateStatus.ERROR.name
                    }
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

            UpdateStatus.BATTERY_LOW -> {

                var status = "battery low"
                if (!watchUpdateStatus.wStatus.isNullOrEmpty()) {
                    status = watchUpdateStatus.wStatus!!
                }
                logErrorEvent(InsiderAppEvents.WatchFaceEvents.wn_face_failed, status)
                binding.bUpdateWatchFace.visible()
                progressBottomSheet?.dismiss()
                //context.showShortToast("Failed")
                viewModel.deleteTempFile()
                sessionManager.transferInProgress = false
                sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.WATCH_FACES_UPDATED_CLICK,
                    java.util.HashMap<String, Any>().apply {
                        this["watch_face_name"] =
                            viewModel.watchFaceName.toString()
                        this["watch_face_update_status"] = false
                        this["detail"] =
                            viewModel.watchFaceId.toString() + "\n" +
                                    viewModel.watchFaceName.toString() + "\n" +
                                    UpdateStatus.BATTERY_LOW.name
                    }
                )
                showBatteryWarning()

                viewModel.shouldSendWatchFailureLogs {
                    if (it) {
                        context?.let { context ->
                            val comment =
                                "${viewModel.watchFace.value?.name ?: ""} ${viewModel.watchFaceId} ${UpdateStatus.BATTERY_LOW.name}"
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


    fun sendWatchFeedbackLogs() {

    }

    private fun setWatchUi(watch: WatchFace) {
        Glide.with(requireContext())
            .load(watch.imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.ivWatchFace)

        //watch face name update on viewmodel
        viewModel.watchFaceName = watch.name
        binding.toolbar.tvTitle.text = watch.name
        binding.ivFavourite.isChecked = watch.isFav()

        binding.tvWatchFaceDownloads.text = watch.getDownloadsToDisplay()

        if (watch.inUseCount.isNullOrEmpty()) {
            binding.tvCurrentInUse.gone()
            binding.textInUse.gone()
            binding.imageView25.gone()
        } else {
            binding.tvCurrentInUse.visible()
            binding.textInUse.visible()
            binding.imageView25.visible()
            binding.tvCurrentInUse.text = watch.getInUseToDisplay()
        }

        binding.tvAbout.text = watch.description
    }

    override fun onWatchFaceClicked(watchFaceId: Int, watch: WatchFace) {
        viewModel.watchFaceId = watchFaceId
        viewModel.getWatchFaceData(watchFaceId)
    }

    override fun onMarkFavouriteClicked(favourite: Boolean, watchFace: WatchFace, position: Int) {
        viewModel.markFavourite(favourite, watchFace)
    }


    private fun updateWatchface(url: String) {

        if ((sessionManager.batterPercent.value ?: 0) <= minimumBatteryLevel) {
            binding.bUpdateWatchFace.visible()
            logErrorEvent(
                InsiderAppEvents.WatchFaceEvents.wn_face_apply_failed_lb,
                "battery low warning"
            )
            showBatteryWarning()
            return
        }


        var fileName: String? = "watchface.zip"
        val extension: String = url.substring(url.lastIndexOf("."))

        if (extension == ".tar") {
            fileName = viewModel.getZipFileNameForRyeex(url)
            LOGS.d("updateWatchface ${viewModel.getZipFileNameForRyeex(url)} $url")
            if (fileName.isNullOrEmpty()) {
                logErrorEvent(
                    InsiderAppEvents.WatchFaceEvents.wn_face_apply_failed_ei,
                    "file name empty - ryeex"
                )
                uiController.onDisplayError(getString(R.string.text_something_went_wrong))
                return
            }

        }

        context?.let {
            viewModel.downloadWatchFace(
                url,
                it.externalCacheDir ?: it.cacheDir,
                fileName!!/*response.zip.originalFileName*/
            )
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
        title: String,
        message: String,
        typeText: String,
        watchfaceImageUrl: String?,
        watchFaceName: String?
    ) {
        progressBottomSheet = WatchFaceProgressBottomDialog.getInstance(
            title,
            message,
            typeText,
            watchfaceImageUrl,
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