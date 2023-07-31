package com.noisefit.ui.npl.wins

import android.animation.Animator
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentNPLWinListBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NPLWinListFragment :
    BaseFragment<FragmentNPLWinListBinding>(FragmentNPLWinListBinding::inflate) {
    private val mViewModel: NPLWinViewModel by viewModels()

    @Inject
    lateinit var sessionManager: SessionManager

    private val mAdapter: NPLWinAdapter by lazy {
        NPLWinAdapter(object : OnCollectRewardListener {
            override fun onCollectReward(predictionId: Long, title: String, position: Int) {
                mViewModel.collectReward(arrayListOf(predictionId.toInt())) { coins ->
                    sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_CLAIMWINS_COLLECT_REWARD_CLICK)
                    mViewModel.updateCoins(coins)
                    mViewModel.removeItem(predictionId)
                    showCollectAnimation()
                }
            }

        })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_CLAIMWINS_PAGE_VISIT)
        showHideView(View.GONE)
        setRecycler()
        mViewModel.getNplWinsData()
    }

    private fun setRecycler() {
        with(binding.rvWin) {
            adapter = mAdapter
        }
    }

    override fun initListener() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_your_wins)
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.tvCollectAll.setOnClickListener {
            sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_COLLECT_ALL_WINS_CLICK)
            mViewModel.collectReward(mViewModel.getPredictionId()) { coins ->
                mViewModel.updateCoins(coins)
                mViewModel.removeAll()
                showCollectAnimation()
            }
        }

    }

    override fun subscribeObservers() {
        mViewModel.userCoins.observe(this) {
            binding.lytToolbar.tvCoins.text = it.toString()
        }

        mViewModel.nplWinListData.observe(this) {
            mAdapter.setData(it)
            if (it.isEmpty()) {
                showEmptyState()
            } else {
                binding.lytEmpty.root.gone()
                showHideView(View.VISIBLE)
            }


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


    private fun showHideView(isVisible: Int) {
        binding.textView1.visibility = isVisible
        binding.tvCollectAll.visibility = isVisible
        binding.rvWin.visibility = isVisible
    }

    fun showEmptyState() {
        binding.lytEmpty.root.visible()
        binding.lytEmpty.tvEmptyMessage.text = "No wins to claim"
        /*binding.lytEmpty.textViewMsg.text =
            "Get exciting rewards with every correct prediction of the upcoming matches."*/
    }


    private fun showCollectAnimation() {

        binding.vAnimCollect.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {

                binding.vTouchBlock.gone()
                binding.vAnimCollect.gone()
                if (mAdapter.itemCount == 0) {
                    showEmptyState()
                }
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


}