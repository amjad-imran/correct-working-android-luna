package com.noisefit.ui.watchface2.myCreation

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import com.noisefit.R
import com.noisefit.data.remote.response.DiyMyCreation
import com.noisefit.databinding.FragmentMyCreationWfBinding
import com.noisefit.receiver.workManager.DiyWatchFaceTransferStates
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadCircleCacheWithProgress
import com.noisefit_commans.ui.loadImageCacheWithProgress
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.diy.DiyWatchFaceViewModel
import com.noisefit.ui.watchface2.bottom.MyDiyCreationDetailBottomSheet
import com.noisefit.ui.watchface2.bottom.MyDiyCreationDetailBottomSheetListener
import com.noisefit.ui.watchface2.sub.Watchface2SubViewModel
import com.noisefit.util.ApplicationUtils
import com.noisefit.watch.WatchForm
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.models.DiyCustomWatchFace
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyCreationWfFragment :
    BaseFragment<FragmentMyCreationWfBinding>(FragmentMyCreationWfBinding::inflate),
    MyCreationInteractionListener {

    private var progressBottomSheet: MyDiyCreationDetailBottomSheet? = null
    private val diyWatchFaceViewModel: DiyWatchFaceViewModel by viewModels()
    private val viewModel: Watchface2SubViewModel by viewModels()
    private val adapter: MyCreationWfAdapter by lazy {
        MyCreationWfAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.getDiyMyCreationWatchFaces(false)
        setRecyclerView()
        setToolBar()
    }

    private fun setRecyclerView() {
        binding.rv.layoutManager = GridLayoutManager(context, 2)
        binding.rv.adapter = adapter


    }


    private fun setToolBar() {

        binding.lytToolbar.apply {
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
            tvTitle.text = getString(R.string.text_my_creations)

        }

        binding.btnUpload.setOnClickListener {
            if (binding.btnUpload.text == getString(R.string.text_save)) {
                viewModel.setEditMode(false)
            } else {
                viewModel.setEditMode(true)
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCHFACES_MY_CREATIONS_EDIT)
            }
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
        }

    }

    private fun setWatchface(watchFaceModal: DiyMyCreation) {


//        val widthHeight = viewModel.widthHeight

//        val selectedDiyCustomWatchFaceBg = viewModel.selectedDiyCustomWatchFaceBg
        var color = Color.argb(255, 255, 255, 255)

        if (watchFaceModal.colour.isNotEmpty()) {
            color = Color.parseColor(watchFaceModal.colour)
        }

       val diyCustomWatchFace = DiyCustomWatchFace(
           binFile = watchFaceModal.textBin,
           color = color,
           image = watchFaceModal.bgBitmap,
           textLayer = watchFaceModal.textBitmap,
           screenType = viewModel.screenType.type,
           width = diyWatchFaceViewModel.widthHeight.first,
           height = diyWatchFaceViewModel.widthHeight.second,
           textLayerName = watchFaceModal.textLayerName
       )

       // LOGS.d("setWatchface_inside ${Gson().toJson(diyCustomWatchFace)}")
        viewModel.viewModelScope.launch {
            activity?.let {


                val isStarted = ApplicationUtils.startDiyWatchFaceTransferWorker(it, diyCustomWatchFace)
                if (!isStarted) {
                    context.showShortToast(getString(R.string.text_transfer_in_progress))
                }
            }
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


    private fun showEmptyScreen(showEmpty: Boolean) {
        if (showEmpty) {
            val url = viewModel.watchesSDK.getWatchFaceGif()
            if (viewModel.screenType == WatchForm.CIRCLE) {
                binding.lytEmptyMyCreation.bgImv.apply {
                    loadCircleCacheWithProgress(
                        this.context,
                        url.ifEmpty { R.drawable.circle_create_your_own })
                }
            } else {
                binding.lytEmptyMyCreation.bgImv.apply {
                    loadImageCacheWithProgress(
                        this.context,
                        url.ifEmpty { R.drawable.rectangle_create_your_own })
                }
            }
            binding.rv.gone()
            binding.line1.root.gone()
            binding.textView80.gone()

            binding.btnUpload.gone()
            binding.lytEmptyMyCreation.root.visible()
        } else {
            binding.rv.visible()
            binding.btnUpload.visible()
            binding.line1.root.visible()
            binding.textView80.visible()
            binding.lytEmptyMyCreation.root.gone()
        }
    }

    override fun initListener() {
        binding.lytEmptyMyCreation.btnCreate.setOnClickListener {
            navigate(R.id.diyWatchFaceFragment)
        }
    }

    private fun setEditState(state: Boolean) {
        if (state) {
            binding.btnUpload.text = getString(R.string.text_save)
        } else {
            binding.btnUpload.text = getString(R.string.text_edit)
        }

        adapter.setEditMode(state)
    }

    override fun subscribeObservers() {
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.sessionManager.diyWatchFaceTransferStates.observe(this) {
            it.getContent()?.let { event ->
                when (event) {
                    is DiyWatchFaceTransferStates.Default -> {
                        LOGS.d("WATCHSTATE default")
                    }

                    is DiyWatchFaceTransferStates.Failed -> {
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
                        dismissProgressBar()
                        uiController.onDisplayError("WatchFace Transferred")

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
        viewModel.editMode.observe(this) {
            setEditState(it)
        }

        viewModel.openEditDiyScreen.observe(this) {
            it.getContent()?.let {


                navigate(
                    MyCreationWfFragmentDirections.actionMyCreationWfFragmentToDiyWatchFaceFragment()
                        .apply {
                            this.wfData = viewModel.temporaryDiyMyCreation
                        }
                )
            }
        }
        viewModel.diyMyCreationList.observe(this) {
            it?.let {
                showEmptyScreen(it.isEmpty())
                adapter.setDataSet(
                    diyWatchFaceViewModel.widthHeight,
                    it,
                    viewModel.editMode.value!!,
                    viewModel.screenType
                )
            }
        }
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.deleteDiyCreationSuccess.observe(this) {
            it.getContent()?.let {
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCHFACES_MY_CREATIONS_EDIT_REMOVE)
            }
        }


    }





    override fun onWatchFaceClicked(watchFaceId: Int, watchface: DiyMyCreation) {

        showProgressDialog(watchface)


    }

    override fun onDeleteClicked(watchface: DiyMyCreation, position: Int) {
        adapter.removeItem(position)
        LOGS.d("REMOVDIY_WATCH $position")
        if (adapter.itemCount == 0) {
            showEmptyScreen(true)
            viewModel.setEditMode(false)
        }
        viewModel.deleteDiyMyCreationWatchFaces(watchface.id)

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

    private fun showProgressDialog(diyMyCreation: DiyMyCreation) {
//        if (progressBottomSheet != null) {
//            return
//        }

        progressBottomSheet = MyDiyCreationDetailBottomSheet.getInstance(
            diyMyCreation,
            false
        )
        progressBottomSheet?.isCancelable = false
        progressBottomSheet?.show(
            childFragmentManager,
            "DownloadBottomSheet"
        )
        progressBottomSheet?.setMyDiyCreationDetailBottomSheetList(object :
            MyDiyCreationDetailBottomSheetListener {
            override fun startWatchfaceService() {
                if (!viewModel.sessionManager.isDeviceConnected()) {
                    context.showShortToast(getString(R.string.text_device_not_connected))
                    return
                }

                if ((viewModel.sessionManager.batterPercent.value
                        ?: 0) <= viewModel.minimumBatteryLevel
                ) {
                    showBatteryWarning()
                    return
                }

                LOGS.d("setWatchface_inside 1212")
                setWatchface(diyMyCreation)

            }

            override fun editWatchFace() {
                navigate(
                    MyCreationWfFragmentDirections.actionMyCreationWfFragmentToDiyWatchFaceFragment()
                        .apply {
                            this.wfData = diyMyCreation
                        }
                )

            }


        })
    }


}