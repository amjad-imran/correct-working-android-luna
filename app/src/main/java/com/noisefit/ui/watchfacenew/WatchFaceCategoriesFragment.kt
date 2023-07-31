package com.noisefit.ui.watchfacenew

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.databinding.FragmentWatchFaceCategoriesBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.watchface.FavMarkFrom
import com.noisefit.ui.watchface.WatchFaceCatListViewModel
import com.noisefit.ui.watchface.adapter.WatchCatActions
import com.noisefit.ui.watchface.adapter.WatchFaceCategoryAdapter
import com.noisefit_commans.models.WatchFace
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WatchFaceCategoriesFragment :
    BaseFragment<FragmentWatchFaceCategoriesBinding>(FragmentWatchFaceCategoriesBinding::inflate),
    WatchCatActions {

    private val viewModel: WatchFaceCatListViewModel by viewModels()


    private val adapter: WatchFaceCategoryAdapter by lazy {
        WatchFaceCategoryAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecycler()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getWatchFaceCategories(false)
    }

    private fun setUpRecycler() {
        binding.rvCatList.layoutManager = LinearLayoutManager(context)
        binding.rvCatList.adapter = adapter
    }

    override fun initListener() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.getWatchFaceCategories(true)
        }

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
        viewModel.categoryList.observe(this) {
            adapter.setDataSet(it)
        }
        viewModel.getLoading().observe(this) {
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
        viewModel.resetFavouriteState.observe(this) {
            it.getContent()?.let { state ->
                if (viewModel.favMarkedFrom == FavMarkFrom.CATEGORIES_LISTING) {
                    adapter.resetFavState(
                        state,
                        viewModel.currentPosition,
                        viewModel.masterPosition
                    )
                }
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }

    override fun onCategoryClicked(categoryId: Int, categoryName: String) {
        navigate(
            WatchFaceCategoriesFragmentDirections.actionWatchFaceCategoriesFragmentToWatchFaceCategoryFragment(
                categoryId,
                categoryName
            )
        )
    }

    override fun onWatchFaceClicked(watchFaceId: Int,watchFace: WatchFace) {
        navigate(
            WatchFaceCategoriesFragmentDirections.actionWatchFaceCategoriesFragmentToWatchFaceUpdateFragment(
                watchFaceId
            )
        )
    }

    override fun onMarkFavouriteClicked(
        favourite: Boolean,
        watchFace: WatchFace,
        innerPosition: Int,
        position: Int
    ) {
        viewModel.masterPosition = position
        viewModel.markFavourite(favourite, watchFace, innerPosition, FavMarkFrom.CATEGORIES_LISTING)
    }

}