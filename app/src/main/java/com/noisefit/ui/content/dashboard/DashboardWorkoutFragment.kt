package com.noisefit.ui.content.dashboard

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.data.model.LastWatchedList
import com.noisefit.data.model.SubCategoriesList
import com.noisefit.data.model.VideosList
import com.noisefit.luna.databinding.FragmentDashboardWorkoutBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.content.player.ContentPlayerActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardWorkoutFragment :
    BaseFragment<FragmentDashboardWorkoutBinding>(FragmentDashboardWorkoutBinding::inflate) {
    private val mViewModel: DashboardWorkoutViewModel by viewModels()
    private val mLastWatchedAdapter: LastWatchedAdapter by lazy {
        LastWatchedAdapter(object : LastWatchedAdapter.OnLastItemClickListener {
            override fun onItemClick(resultData: LastWatchedList) {
                startActivity(
                    ContentPlayerActivity.getStartIntent(
                        requireContext(),
                        resultData.videoUrl ?: "",
                        resultData.title ?: "",
                        resultData.videoId ?: -1,
                        resultData.progress ?: 0

                    )
                )
            }
        })
    }

    private val mDashboardWorkoutAdapter: WorkoutAdapter by lazy {
        WorkoutAdapter(object : WorkoutAdapter.OnWorkoutClickListener {
            override fun onItemSubCategoriesClick(resultData: SubCategoriesList, position: Int) {
                resultData.subcategoryId?.let {
                    mViewModel.getSubCatVidList(it) {videos->
                        mDashboardWorkoutAdapter.updateSubCategoryData(videos,position)
                    }
                }
            }

            override fun onISCatVideoClick(resultData: VideosList) {
                navigate(R.id.wContentDetailsFragment, Bundle().apply {
                    putInt("id", resultData.id ?: -1)
                })
            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
        mViewModel.getContentList()
    }

    private fun setRecycler() {
        with(binding.rvLastWatched) {
            adapter = mLastWatchedAdapter
        }

        with(binding.rvWorkout) {
            adapter = mDashboardWorkoutAdapter
        }
    }

    override fun initListener() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_explore_workout)
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
        mViewModel.contentList.observe(this) {
            if (it.lastWatched.isNullOrEmpty()) {
                binding.textView12.gone()
                binding.rvLastWatched.gone()
                binding.divider2.root.gone()
                binding.textView12.gone()
            } else {
                binding.textView12.visible()
                binding.rvLastWatched.visible()
                binding.textView12.visible()
                binding.divider2.root.visible()
                mLastWatchedAdapter.setDataSet(it.lastWatched)
            }

            if (it.videoCategories.isNullOrEmpty()) {
                binding.rvWorkout.gone()
            } else {
                binding.rvWorkout.visible()
                mDashboardWorkoutAdapter.setDataSet(it.videoCategories)
            }
        }

        mViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        mViewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }

}