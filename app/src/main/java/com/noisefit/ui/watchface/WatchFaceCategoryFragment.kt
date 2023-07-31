package com.noisefit.ui.watchface

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.noisefit.luna.databinding.FragmentWatchFaceCategoryBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.watchface.adapter.WatchFaceActions
import com.noisefit.ui.watchface.adapter.WatchFaceAdapter
import com.noisefit_commans.models.WatchFace
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@Deprecated("")
@AndroidEntryPoint
class WatchFaceCategoryFragment :
    BaseFragment<FragmentWatchFaceCategoryBinding>(FragmentWatchFaceCategoryBinding::inflate),
    WatchFaceActions {

    private val viewModel: WatchFaceCatListViewModel by viewModels()

    private val adapter: WatchFaceAdapter by lazy {
        WatchFaceAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            viewModel.categoryId = WatchFaceCategoryFragmentArgs.fromBundle(it).categoryId
            binding.toolbar.tvTitle.text =
                WatchFaceCategoryFragmentArgs.fromBundle(it).categoryName.replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(
                        Locale.getDefault()
                    ) else char.toString()
                }
        }
        setRecyclerView()

        viewModel.categoryId?.let {
            viewModel.getWatchFaceByCategory(it)
        }
    }

    private fun setRecyclerView() {
        binding.rvWatchFace.layoutManager = GridLayoutManager(context, 2)
        binding.rvWatchFace.adapter = adapter
    }

    override fun initListener() {

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {
        viewModel.categoryWatchFacesList.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.rvWatchFace.gone()
            } else {
                binding.rvWatchFace.visible()
            }
            adapter.setDataSet(it)
        }

        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.resetFavouriteState.observe(this){
            it.getContent()?.let { state ->
                if(viewModel.favMarkedFrom==FavMarkFrom.CATEGORY){
                    adapter.resetFavState(state,viewModel.currentPosition)
                }
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearWatchfaceListingDate()
    }

    override fun onWatchFaceClicked(watchFaceId: Int,watchFace: WatchFace) {
        navigate(
            WatchFaceCategoryFragmentDirections.actionWatchFaceCategoryFragmentToWatchFaceUpdateFragment(
                watchFaceId
            )
        )
    }

    override fun onMarkFavouriteClicked(favourite: Boolean, watchFace: WatchFace, position: Int) {
        viewModel.markFavourite(favourite, watchFace, position,FavMarkFrom.CATEGORY)
    }


}