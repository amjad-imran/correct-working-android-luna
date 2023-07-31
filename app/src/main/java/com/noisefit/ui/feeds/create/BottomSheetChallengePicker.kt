package com.noisefit.ui.feeds.create

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.R
import com.noisefit.databinding.BottomSheetWorkoutPickerBinding
import com.noisefit_commans.data.response.ChallengeModel
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetChallengePicker : BaseBottomSheetWithTransparent<BottomSheetWorkoutPickerBinding>(
    BottomSheetWorkoutPickerBinding::inflate
) {
    private val viewModel: PostExternalDataViewModel by viewModels()

    private val recentChallengeAdapter by lazy {
        RecentChallengesAdapter(object : ChallengeActions {
            override fun onChallengeSelected(challenge: ChallengeModel) {
                setFragmentResult(
                    CHALLENGE_PICKER_RESULT,
                    bundleOf("challenge" to challenge)
                )
                navigateUpSafe()
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textView73.text = getString(R.string.text_completed_challenges)

        binding.rvWorkouts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentChallengeAdapter
        }
        viewModel.getChallenges()
    }


    companion object {
        const val CHALLENGE_PICKER_RESULT = "CHALLENGE_PICKER_RESULT"
    }

    override fun initListener() {
        binding.btnDone.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.visible()
            } else {
                binding.progressBar.gone()
            }
        }

        viewModel.getApiErrors().observe(this) {
            it.getContent()?.let {
                context.showShortToast(getString(R.string.text_something_went_wrong))
            }
        }

        viewModel.challenge.observe(this) {
            recentChallengeAdapter.setDataSet(it ?: ArrayList())

            if (it.isNullOrEmpty()) {
                binding.tvNoData.text = getString(R.string.text_no_challenge)
                binding.tvNoData.visible()
                binding.btnDone.visible()
            } else {
                binding.tvNoData.gone()
                binding.btnDone.gone()
            }
        }
    }
}