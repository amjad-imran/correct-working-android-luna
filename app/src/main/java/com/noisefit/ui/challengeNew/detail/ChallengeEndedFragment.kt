package com.noisefit.ui.challengeNew.detail

import android.animation.Animator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.os.HandlerCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.noisefit.BottomNavOption
import com.noisefit.MainViewModel
import com.noisefit.R
import com.noisefit_commans.data.response.ChallengeModel
import com.noisefit_commans.data.response.Rewards
import com.noisefit.databinding.FragmentChallengeEndedBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.feeds.create.CREATE_POST_KEY
import com.noisefit.ui.feeds.create.PostContent
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.Presets
import com.noisefit_commans.utils.share.ShareUtil
import com.noisefit_commans.utils.ImageUtil
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt

@AndroidEntryPoint
class ChallengeEndedFragment :
    BaseFragment<FragmentChallengeEndedBinding>(FragmentChallengeEndedBinding::inflate) {

    private val viewModel: ChallengeDetailsViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            viewModel.challengeId = it.getInt("challengeId")
        }

        binding.lytAboutChallenge.include27.root.invisible()

        viewModel.getChallengeDetailsByID()


    }


    override fun initListener() {
        binding.layoutChallengeWinner.lottieAnimViews.addAnimatorListener(object :
            Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                when (viewModel.rankAnimationValue) {
                    1 -> {
                        animateView(
                            R.raw.anim_first_badge_rays,
                            LottieDrawable.INFINITE
                        )
                    }
                    2 -> {
                        animateView(
                            R.raw.anim_second_badge_rays,
                            LottieDrawable.INFINITE
                        )
                    }
                    3 -> {
                        animateView(
                            R.raw.anim_third_badge_rays,
                            LottieDrawable.INFINITE
                        )
                    }
                    4 -> {
                        animateView(
                            R.raw.anim_achieved_badge_rays,
                            LottieDrawable.INFINITE
                        )
                    }
                    else -> {
                        animateView(
                            R.raw.anim_missed_badge_rays,
                            LottieDrawable.INFINITE
                        )
                    }
                }

            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationRepeat(animation: Animator?) {

            }

        })


        binding.viewShare.setOnClickListener {

            val challenge = viewModel.challengeDetails.value ?: return@setOnClickListener

            setFragmentResultListener(CREATE_POST_KEY) { _, bundle ->
                val updated = bundle.getBoolean("updated")
                if (updated) {
                    mainViewModel.navigateTo(BottomNavOption.COMMUNITY)
                }
            }

            navigate(ChallengeEndedFragmentDirections.actionChallengeEndedFragmentToCreatePostFragment()
                .apply {
                    this.shareContent = PostContent.CHALLENGES
                    this.challenge = ChallengeModel(
                        challenge_id = challenge.challenge_id,
                        image_url = challenge.image_url,
                        title = challenge.title,
                        type = challenge.type,
                        start_date = challenge.start_date,
                        end_date = challenge.end_date,
                        progress = challenge.progress,
                        user_rank = challenge.user_rank,
                        participants = challenge.participants
                    )
                })

            //shareImageSocial(0)
        }
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
        viewModel.getLoading().observe(this) {
            uiController.displayProgressBar(it, "")
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.challengeDetails.observe(this) { challengeModel ->
            challengeModel?.let {
                setData(it)
            }
        }
    }

    private fun setData(it: com.noisefit_commans.data.response.ChallengeModel) {
        binding.textViewTitle.text = it.title
        //in case of rank null
        if (it.user_rank == 0) {
            binding.layoutChallengeProgress.root.gone()
            binding.layoutChallengeProgressRankNull.root.visible()
            binding.layoutChallengeProgressRankNull.textViewProgressValue.text =
                it.getFormattedProgress()
            binding.layoutChallengeProgressRankNull.textViewProgressUnit.text =
                ApplicationUtils.getChallengeTypeUnit(
                    it.type.toString(),
                    viewModel.unit
                )
            binding.layoutChallengeProgressRankNull.tvTotalParticipantsValue.text =
                (it.participants ?: 0L).numberFormatter()
        } else {
            binding.layoutChallengeProgress.root.visible()
            binding.layoutChallengeProgressRankNull.root.gone()
            binding.layoutChallengeProgress.tvTotalParticipantsValue.text =
                (it.participants ?: 0L).numberFormatter()
            binding.layoutChallengeProgress.tvMyRankValue.text =
                if (it.user_rank == 0) "-" else it.user_rank.toString()

            binding.layoutChallengeProgress.textViewProgressValue.text = it.getFormattedProgress()
            binding.layoutChallengeWinner.tvEndMessage.text = it.completed_msg

            binding.layoutChallengeProgress.textViewProgressUnit.text =
                ApplicationUtils.getChallengeTypeUnit(
                    it.type.toString(),
                    viewModel.unit
                )
            binding.layoutChallengeProgress.textViewProgressAvgDayValue.text =
                if (it.progress_avg != null) {
                    it.progress_avg?.roundToInt().toString()
                } else {
                    "-"
                }

            binding.layoutChallengeProgress.textViewProgressAvgDayUnit.text =
                ApplicationUtils.getChallengeTypeUnit(
                    it.type.toString(),
                    viewModel.unit
                )
        }

        if (it.detail.isNullOrEmpty()) {
            binding.lytAboutChallenge.root.gone()
        } else {
            binding.lytAboutChallenge.root.visible()
            binding.lytAboutChallenge.tvAboutChallenge.text =
                HtmlCompat.fromHtml(it.detail ?: "", 0)
        }


        if ((it.user_rank ?: 0) > 3 || it.user_rank == 0) {
            if (it.is_achieved != null && it.is_achieved == "1") {
                viewModel.rankAnimationValue = 4
                if (!viewModel.isInitAnimPlayed) {
                    animateView(
                        R.raw.anim_achieved_badge, 0
                    )
                }

            } else {
                viewModel.rankAnimationValue = 5
                if (!viewModel.isInitAnimPlayed) {
                    animateView(
                        R.raw.anim_missed_badge, 0
                    )
                }
            }
            binding.layoutChallengeReward.root.gone()

        } else {
            binding.layoutChallengeReward.root.visible()
            if ((it.rewards?.size ?: 0) > 0) {
                when (it.user_rank) {
                    1 -> {
                        viewModel.rankAnimationValue = 1
                        if (!viewModel.isInitAnimPlayed) {
                            animateView(
                                R.raw.anim_first_badge, 0
                            )
                        }

                        updateRewardUI(it.rewards, 1)
                    }
                    2 -> {
                        viewModel.rankAnimationValue = 2
                        if (!viewModel.isInitAnimPlayed) {
                            animateView(
                                R.raw.anim_second_badge,
                                0
                            )
                        }


                        updateRewardUI(it.rewards, 2)
                    }
                    3 -> {
                        viewModel.rankAnimationValue = 3
                        if (!viewModel.isInitAnimPlayed) {
                            animateView(
                                R.raw.anim_third_badge, 0
                            )
                        }
                        updateRewardUI(it.rewards, 3)
                    }
                }
            }
        }


    }

    fun shareImageSocial(shareOn: Int) {

        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        val mainThreadHandler: Handler = HandlerCompat.createAsync(Looper.getMainLooper())
        binding.shareScrollview.setBackgroundColor(android.R.color.black)
        val view = binding.shareScrollview
        executorService.execute {
            mainThreadHandler.post {
                binding.progressBar.root.visible()
            }
            val shareBitmap = ImageUtil.getBitmapFromView(view)
            shareBitmap?.let {
                val shareUri = ImageUtil.getTempImageUri(it, requireContext())
                mainThreadHandler.post {
                    binding.progressBar.root.gone()
                    binding.shareScrollview.setBackgroundColor(android.R.color.transparent)

                    when (shareOn) {
                        0 -> {

                            ShareUtil.shareOthers(requireContext(), shareUri)
                        }
                        1 -> {

                            ShareUtil.shareOnFacebook(requireContext(), shareUri)
                        }
                        2 -> {

                            ShareUtil.shareOnInsta(requireContext(), shareUri)
                        }
                        3 -> {

                            ShareUtil.shareOnWhatsapp(requireContext(), shareUri)
                        }
                    }
                }
            }
            mainThreadHandler.post {
                binding.progressBar.root.gone()
                binding.shareScrollview.setBackgroundColor(android.R.color.transparent)
            }
        }
    }


    private fun updateRewardUI(rewards: List<com.noisefit_commans.data.response.Rewards>?, rank: Int) {

        val reward = rewards?.find {
            it.rank == rank
        }

        if (reward == null) {
            binding.layoutChallengeReward.root.gone()
            return
        }

        binding.layoutChallengeReward.textViewRewardTitle.text = reward.reward_title.toString()
        binding.layoutChallengeReward.textViewRewardMsg.text = reward.message.toString()
        binding.layoutChallengeReward.imageViewReward.loadImage(
            binding.layoutChallengeReward.imageViewReward.context, reward.reward_url
        )
        binding.konfettiViewLeft.start(Presets.festiveLeft())
        binding.konfettiViewRight.start(Presets.festiveRight())
    }

    private fun animateView(resourceName: Int, repeatCountValue: Int) {
        viewModel.isInitAnimPlayed = true
        tryCatch {
            nullableBinding?.layoutChallengeWinner?.lottieAnimViews?.apply {
                repeatCount = repeatCountValue
                setAnimation(resourceName)
                playAnimation()
            }
        }
    }


}