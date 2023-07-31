package com.noisefit.ui.watchface

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.noisefit.R
import com.noisefit.databinding.FragmentFavouriteWatchFacesBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible

import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.watchface.adapter.WatchFaceActions
import com.noisefit.ui.watchface.adapter.WatchFaceAdapter

import com.noisefit.ui.watchface2.sub.Watchface2SubViewModel
import com.noisefit.ui.watchfacenew.WatchFaceCategoryListingFragmentDirections
import com.noisefit_commans.models.WatchFace
import dagger.hilt.android.AndroidEntryPoint

@Deprecated("use Watchface2SubListFragment with fav type")
@AndroidEntryPoint
class FavouriteWatchFacesFragment :
    BaseFragment<FragmentFavouriteWatchFacesBinding>(FragmentFavouriteWatchFacesBinding::inflate),
    WatchFaceActions {

    private val viewModel: Watchface2SubViewModel by viewModels()

    private val adapter: WatchFaceAdapter by lazy {
        WatchFaceAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getFavouriteWatchFaces(false)
    }


    private fun setRecyclerView() {
        binding.rvFavourites.layoutManager = GridLayoutManager(context, 2)
        binding.rvFavourites.adapter = adapter
    }


    override fun initListener() {
        binding.lytToolbar.apply {
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
            tvTitle.text = getString(R.string.text_my_favourites)
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.getFavouriteWatchFaces(true)
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

        viewModel.favouriteWatchFaceList.observe(this) {
            setPlaceholder(it.isEmpty())
            adapter.setDataSet(it)
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.favouriteMarked.observe(this) {
            it.getContent()?.let { position ->
//                if (viewModel.favMarkedFrom == WF2FavMarkFrom.MY_FAV) {
//                    adapter.removeItem(position)
//                    setPlaceholder(adapter.itemCount == 0)
//                }
            }
        }
        viewModel.resetFavouriteStateFavourite.observe(this) {
            it.getContent()?.let { state ->
//                if (viewModel.favMarkedFrom == WF2FavMarkFrom.MY_FAV) {
//                    adapter.resetFavState(state, viewModel.currentPosition)
//                }
            }
        }
    }

    fun setPlaceholder(show: Boolean) {
        if (show) {
//            binding.textNoFav.visible()
//            binding.textNoFavMessage.visible()
        } else {
//            binding.textNoFav.gone()
//            binding.textNoFavMessage.gone()
        }
    }

    override fun onWatchFaceClicked(watchFaceId: Int, watchface: WatchFace) {
        navigate(
            WatchFaceCategoryListingFragmentDirections.actionNavigationWatchfaceToWatchFaceUpdateFragment(
                watchFaceId
            )
        )
    }

    override fun onMarkFavouriteClicked(favourite: Boolean, watchface: WatchFace, position: Int) {
//        viewModel.markFavourite(favourite, watchface, position, WF2FavMarkFrom.MY_FAV)
        //viewModel.removeFavouriteItem(position)
    }
}