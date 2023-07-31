package com.noisefit.ui.content.details

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.data.model.VideosList
import com.noisefit.databinding.FragmentWContentDetailsBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.content.player.ContentPlayerActivity
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WContentDetailsFragment :
    BaseFragment<FragmentWContentDetailsBinding>(FragmentWContentDetailsBinding::inflate) {
    private val mViewModel: WContentDetailsViewModel by viewModels()
    private val mArgs: WContentDetailsFragmentArgs by navArgs()
    private val mAdapter: WContentNeedAdapter by lazy {
        WContentNeedAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.id = mArgs.id
        setRecycler()

    }

    private fun setRecycler() {
        with(binding.rvNeed) {
            adapter = mAdapter
        }

    }

    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.ivBanner.setOnClickListener {
            startActivity(
                ContentPlayerActivity.getStartIntent(
                    requireContext(),
                    mViewModel.videoUrl ?: "",
                    mViewModel.title ?: "",
                    mViewModel.videoId ?: -1,
                    0

                )
            )

        }

    }

    override fun subscribeObservers() {
        mViewModel.contentDetails.observe(this) {
            updateUi(it)
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

    private fun updateUi(it: VideosList?) {
        mViewModel.videoId = it?.id
        mViewModel.videoUrl = it?.videoUrl
        mViewModel.title = it?.title
        binding.lytToolbar.tvTitle.text = it?.title
        binding.tvTitle.text = it?.title
        binding.tvDescription.text = it?.detail
        binding.lytContentTrackTime.tvTime.text = ApplicationUtils.formatTimeMinuteSec(it?.duration ?: 0)
        binding.ivBanner.loadImage(requireContext(), it?.thumbnailUrl)

        binding.tvViews.text = "${it?.views} view${if ((it?.views ?: 0) > 1) "s" else ""}"

        tryCatch {
            val tempEquipment = it?.equipments?.split(",")?.toTypedArray()
            if (!tempEquipment.isNullOrEmpty()) {
                binding.tvNeedTitle.visible()
                binding.rvNeed.visible()
                binding.divider3.root.visible()
                mAdapter.setDataSet(tempEquipment.toList())
            } else {
                binding.tvNeedTitle.gone()
                binding.rvNeed.gone()
                binding.divider3.root.gone()
            }
        }
        tryCatch {
            binding.lytTrackData.tvTime.text =
                ApplicationUtils.formatTimeMinuteSec(it?.duration ?: 0)
            if (it?.tags.isNullOrEmpty()) return@tryCatch
            val temSplitTag = it?.tags?.split(",")?.toTypedArray()
            if (!temSplitTag.isNullOrEmpty()) {
                if (temSplitTag.size == 1) {
                    binding.imageView1.visible()
                    binding.tvTag2.visible()
                    binding.tvTag2.text = temSplitTag[0]
                    binding.imageView2.gone()
                    binding.tvTag3.gone()
                } else {
                    binding.tvTag2.visible()
                    binding.tvTag3.visible()
                    binding.imageView1.visible()
                    binding.imageView2.visible()
                    binding.tvTag2.text = temSplitTag[0]
                    binding.tvTag3.text = temSplitTag[1]
                }
            }
        }
    }

    override fun onResume() {
        mViewModel.getContentDetails()
        super.onResume()

    }


}