package com.noisefit.ui.npl.history

import android.animation.Animator
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.noisefit.R
import com.noisefit.databinding.FragmentPredictionHistoryBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PredictionHistoryFragment :
    BaseFragment<FragmentPredictionHistoryBinding>(FragmentPredictionHistoryBinding::inflate) {
    private val mViewModel: PredictionHistoryViewModel by viewModels()

    @Inject
    lateinit var sessionManager: SessionManager
    private val mAdapter: PredictionHistoryAdapter by lazy {
        PredictionHistoryAdapter(object : OnCollectRewardListener {
            override fun onCollectReward(predictionId: Int) {
                mViewModel.collectReward(arrayListOf(predictionId)) {
                    showCollectAnimation()
                }
            }

        })
    }

    private fun showCollectAnimation() {

        binding.vAnimCollect.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {

                binding.vTouchBlock.gone()
                binding.vAnimCollect.gone()
                mViewModel.getPredictionHistory()
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationRepeat(p0: Animator?) {

            }
        })
        binding.vTouchBlock.visible()
        binding.vAnimCollect.visible()
        binding.vAnimCollect.setAnimation(R.raw.anim_coins)
        binding.vAnimCollect.playAnimation()
        binding.vAnimCollect.repeatCount = 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_PREDICTION_HISTORY_PAGE_VISIT)
        setRecycler()
    }

    private fun setRecycler() {
        with(binding.rvPHistory) {
            adapter = mAdapter
        }

    }

    override fun initListener() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_prh_title)
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {
        mViewModel.historyList.observe(this) {
            mAdapter.setData(it)
            updateUiState()

        }

        mViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        mViewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }

    private fun updateUiState() {
        val itemCount = mAdapter.itemCount
        if (itemCount > 0) {
            binding.lytEmpty.root.gone()
            binding.rvPHistory.visible()
            binding.textView1.visible()
            binding.textView2.visible()
            binding.textView3.visible()
        } else {
            binding.lytEmpty.root.visible()
            binding.rvPHistory.gone()
            binding.textView1.gone()
            binding.textView2.gone()
            binding.textView3.gone()
        }
    }

    override fun onResume() {
        super.onResume()
        mViewModel.getPredictionHistory()
    }

}