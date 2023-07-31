package com.noisefit.ui.watchface2.sub

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.data.remote.response.Watchface2
import com.noisefit.luna.databinding.FragmentWatchface2SubListBinding
import com.noisefit.receiver.workManager.WatchFaceTransferStates
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.watchface2.WatchfaceHorizontalCategoryAdapter
import com.noisefit.ui.watchface2.bottom.UploadWatchfaceBottomSheet
import com.noisefit.ui.watchface2.bottom.UploadWatchfaceBottomSheetListener
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class Watchface2SubListFragment :
    BaseFragment<FragmentWatchface2SubListBinding>(FragmentWatchface2SubListBinding::inflate),
    Watchface2InteractionListener {

    private var progressBottomSheet: UploadWatchfaceBottomSheet? = null
    private val viewModel: Watchface2SubViewModel by viewModels()

    private val args: Watchface2SubListFragmentArgs by navArgs()
    private val adapter: Watchface2SubAdapter by lazy {
        Watchface2SubAdapter(this)
    }
    private val categoryAdapter: WatchfaceHorizontalCategoryAdapter by lazy {
        WatchfaceHorizontalCategoryAdapter { category ->
            viewModel.selectedCategoryId = category.id
            viewModel.getWatchFaceList(false, category.id)
            binding.lytToolbar.tvTitle.text = category.name.capitalizeWords()
            viewModel.sessionManager.logInsiderAppEvent(
                InsiderAppEvents.WATCHFACES_CATEGORY_CHANGECATEGORY,
                HashMap<String, Any>().apply {
                    this["cat_name"] = category.name
                })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.subListFrom = args.type
        viewModel.selectedCategoryId = args.categoryId

        setRecyclerView()
        setToolBar()
        if (viewModel.subListFrom == WF2SubListFrom.CATEGORIES) {
            viewModel.getWatchFaceCategories(true)
        } else {
            viewModel.getWatchFaceList(false, null)
        }
    }

    private fun setRecyclerView() {
        binding.rvFavourites.layoutManager = GridLayoutManager(context, 2)
        binding.rvFavourites.adapter = adapter

        binding.rvCategories.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategories.adapter = categoryAdapter


    }

    private fun setToolBar() {
        binding.lytToolbar.apply {
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
            tvTitle.text = when (viewModel.subListFrom) {
                WF2SubListFrom.Newly -> ""
                WF2SubListFrom.Popular -> getString(R.string.text_popular)
                WF2SubListFrom.Trending -> getString(R.string.text_trending)
                WF2SubListFrom.MyCreation -> ""
                WF2SubListFrom.CATEGORIES -> ""
                WF2SubListFrom.Favourite -> {
                    binding.backFav.gone()
                    getString(R.string.text_my_favourites)
                }

                WF2SubListFrom.None -> ""
            }

        }
    }

    override fun initListener() {


//        binding.swipeRefreshLayout.setOnRefreshListener {
//            binding.swipeRefreshLayout.isRefreshing = false
//        }

        binding.backFav.setOnClickListener {
            navigate(R.id.watchface2SubListFragment, Bundle().apply {
                this.putSerializable("type", WF2SubListFrom.Favourite)
            })
            //navigate(R.id.favouriteWatchFacesFragment)
        }
    }

    override fun subscribeObservers() {

        viewModel.sessionManager.watchFaceTransferStates.observe(this) {
            it.getContent()?.let { event ->
                when (event) {
                    is WatchFaceTransferStates.Default -> {
                        LOGS.d("WATCHSTATE default")
                    }

                    is WatchFaceTransferStates.Failed -> {
                        dismissProgressBar()
                        LOGS.d("WATCHSTATE failed")
                        uiController.onDisplayError(event.reason)
                    }

                    is WatchFaceTransferStates.Progress -> {
                        progressBottomSheet?.setProgress(event.percent)
                    }

                    is WatchFaceTransferStates.Started -> {
                        LOGS.d("WATCHSTATE Started")

                    }

                    is WatchFaceTransferStates.Success -> {
                        dismissProgressBar()
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
                                this.putString("watchFaceOpenType", "category")
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
        viewModel.categoryList.observe(this) {
            if (it.isEmpty()) {
                binding.rvCategories.gone()
            } else {
                binding.line2.root.visible()
                binding.rvCategories.visible()
                val selectedId = viewModel.selectedCategoryId ?: it.first().id
                categoryAdapter.setDataSet(it, selectedId)
                val (item, position) = categoryAdapter.getItemDetailsById(selectedId)
                binding.lytToolbar.tvTitle.text =
                    item?.name?.capitalizeWords() ?: ""

                binding.rvCategories.scrollToPosition(position)
            }

        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.favouriteMarked.observe(this) {
            it.getContent()?.let { position ->
                if (viewModel.subListFrom == WF2SubListFrom.Favourite) {
                    adapter.removeItem(position)
                    setPlaceholder(adapter.itemCount == 0)
                }
            }
        }
        viewModel.resetFavouriteStateFavourite.observe(this) {
            it.getContent()?.let { state ->
                if (viewModel.subListFrom == WF2SubListFrom.Favourite) {
                    adapter.resetFavState(state, viewModel.currentPosition)
                }
            }
        }

        viewModel.subWfList.observe(this) {
            it?.let {
                adapter.setDataSet(it)
                updateToolbarForNewWf(it.size)
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
    }

    private fun updateToolbarForNewWf(count: Int) {
        binding.lytToolbar.tvTitle.apply {

            if (viewModel.subListFrom == WF2SubListFrom.Newly) {
                val title = "New ($count)"
                this.text = title
            }

        }
    }

    private fun setPlaceholder(show: Boolean) {
        if (show) {
//            binding.textNoFav.visible()
//            binding.textNoFavMessage.visible()
        } else {
//            binding.textNoFav.gone()
//            binding.textNoFavMessage.gone()
        }
    }

    override fun onWatchFaceClicked(watchFaceId: Int, watchface: Watchface2, position: Int) {

        if ((viewModel.sessionManager.batterPercent.value ?: 0) <= viewModel.minimumBatteryLevel) {
            showBatteryWarning()
            return
        }

        viewModel.refreshPosition = position
        var fromFav = false
        if (viewModel.subListFrom == WF2SubListFrom.Favourite) {
            fromFav = true
        }

        viewModel.sessionManager.logInsiderAppEvent(
            InsiderAppEvents.WATCHFACES_CATEGORY_OPEN,
            HashMap<String, Any>().apply {
                this["wf_name"] = watchface.name
            })

        showProgressDialog(watchface, fromFav)


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


    override fun onMarkFavouriteClicked(favourite: Boolean, watchface: Watchface2, position: Int) {
        viewModel.markFavourite(favourite, watchface, position)
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

    private fun showProgressDialog(watchFace: Watchface2, fromFav: Boolean) {

//        if (progressBottomSheet != null) {
//            return
//        }
        progressBottomSheet = UploadWatchfaceBottomSheet.getInstance(
            watchFace,
            fromFav
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

                    if (viewModel.refreshPosition != -1) {
                        LOGS.d("sdasadadsads inside fav ${viewModel.refreshPosition} $isFav ${watchFace.isFavourite}")
                        adapter.notifyItemChanged(viewModel.refreshPosition)
                    }

                }

            }

        })
    }
}


