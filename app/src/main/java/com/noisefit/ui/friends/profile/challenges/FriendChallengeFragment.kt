package com.noisefit.ui.friends.profile.challenges

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieDrawable
import com.noisefit.BottomNavOption
import com.noisefit.MainViewModel
import com.noisefit.R
import com.noisefit.databinding.FragmentFriendChallengeBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.friends.profile.FriendProfileSharedViewModel
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendChallengeFragment :
    BaseFragment<FragmentFriendChallengeBinding>(FragmentFriendChallengeBinding::inflate) {

    val sharedViewModel: FriendProfileSharedViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    val ongoingAdapter by lazy {
        FriendOngoingChallengeAdapter()
    }
    val bestPerformAdapter by lazy {
        FriendBestPerformChallengeAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecycler()

        sharedViewModel.getChallenges()
    }

    private fun setRecycler() {
        with(binding.lytOngoing.rvChallenge) {
            layoutManager = LinearLayoutManager(context)
            adapter = ongoingAdapter
        }
        with(binding.lytBestPerform.rvChallenge) {
            layoutManager = LinearLayoutManager(context)
            adapter = bestPerformAdapter
        }

    }

    override fun initListener() {
        binding.lytOngoing.tvTitle.text = getString(R.string.text_ongoing_challenge)
        binding.lytBestPerform.tvTitle.text = getString(R.string.text_best_performed_challenges)

        binding.lytJoinChallenge.lytJoin.setOnClickListener {
            sharedViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FRIENDPROFILE_CHALLENGES_JOINCHALLENGE_CLICK)
            mainViewModel.navigateTo(BottomNavOption.EXPLORE)
        }
    }

    override fun subscribeObservers() {

        sharedViewModel.getLoading().observe(this){
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        sharedViewModel.challengeListingResponse.observe(this) {
            val ongoingChallenges = it?.ongoingChallenges
            val bestPerformed = it?.bestPerformedChallenges

            ongoingAdapter.setDataSet(ongoingChallenges ?: ArrayList())
            bestPerformAdapter.setDataSet(bestPerformed ?: ArrayList())

            if (ongoingChallenges.isNullOrEmpty()) {
                binding.lytOngoing.root.gone()
                if (sharedViewModel.isMyProfile()) {
                    binding.lytJoinChallenge.lytJoin.visible()
                } else {
                    binding.lytJoinChallenge.tvTitle.text =
                        getString(R.string.text_no_challenges_to_show)
                    binding.lytJoinChallenge.lytJoin.gone()
                }
                binding.divider11.root.gone()
                binding.divider1.root.gone()
                binding.lytJoinChallenge.root.visible()
            } else {
                binding.lytOngoing.root.visible()
                binding.lytJoinChallenge.root.gone()
            }

            if (bestPerformed.isNullOrEmpty()) {
                binding.lytBestPerform.root.gone()
                if (sharedViewModel.canCompete)
                    binding.divider1.root.visible()
                else
                    binding.divider1.root.invisible()
            } else {
                binding.lytBestPerform.root.visible()
                binding.divider1.root.visible()
            }


        }
    }


}