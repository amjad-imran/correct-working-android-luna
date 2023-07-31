package com.noisefit.ui.challengeNew.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit_commans.data.response.ChallengeModel
import com.noisefit.luna.databinding.FragmentNewChallengeBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.AndroidEntryPoint
import me.dkzwm.widget.srl.RefreshingListenerAdapter


@AndroidEntryPoint
class JoinedChallengeFragment :
    BaseFragment<FragmentNewChallengeBinding>(FragmentNewChallengeBinding::inflate) {

    private val viewModel: ChallengeListingViewModel by viewModels()
    private val tabSharedViewModel: MyChallengeTabSharedViewModel by activityViewModels()
    private val mAdapter: JoinedChallengeListAdapter by lazy {
        JoinedChallengeListAdapter(viewModel.unit)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabSharedViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CHALLENGE_JOIN_LIST_PAGE_VISIT)
        binding.animationView.setAnimation(R.raw.loading_swipe_anim)
        viewModel.shouldReloadDetailData()


        setRecycler()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getCurrentChallenges()
    }

    private fun setRecycler() {
        with(binding.rvNewChallenge) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        mAdapter.itemClickListener = { it, challengeModel ->
            tabSharedViewModel.sessionManager.logInsiderAppEvent(
                InsiderAppEvents.CHALLENGE_JOINED_ITEM_CLICK, HashMap<String, Any>().apply {
                    this["challenges_joined_name"] = challengeModel.title.toString()
                    this["challenges_joined_type"] = challengeModel.type.toString()
                    this["challenges_joined_ID"] = challengeModel.challenge_id.toString()
                }
            )
            navigate(
                ChallengeListingFragmentDirections.actionNavigationChallengeToChallengeDetailsFragment(
                    it
                )
            )
        }
    }


    override fun initListener() {
        binding.swipeToRefresh.setOnRefreshListener(object : RefreshingListenerAdapter() {
            override fun onRefreshing() {
                super.onRefreshing()
                binding.swipeToRefresh.refreshComplete()
                if (viewModel.serverReload.value == true) {
                    viewModel.getCurrentChallenges(true)
                }
            }
        })

    }

    override fun subscribeObservers() {

        viewModel.serverReload.observe(this) {
            if (it) {
                if (viewModel.challengeLastSyncTime == 0L) {
                    tabSharedViewModel.setUpdatedText("")
                } else{
                    tabSharedViewModel.setUpdatedText("Updated ${DateFormats.getRelativeTime(viewModel.challengeLastSyncTime)}")
                }

            } else {
                tabSharedViewModel.setUpdatedText("")
            }
        }

        viewModel.currentCount.observe(this) {
            tabSharedViewModel.newCount.value = it.first
            tabSharedViewModel.joinedCount.value = it.second
            tabSharedViewModel.setSelected()
        }

        viewModel.joinedChallenges.observe(this) {
            mAdapter.setDataSet(it)
            updateUI(it)
        }
        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getApiErrors().observe(this) {
            it.getContent()?.let { res ->
                uiController.onApiErrorReceived(res)
            }
        }

        binding.lytNoChallenge.lytJoin.setOnClickListener {
            tabSharedViewModel.moveToNewChallenge()
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message->
                context.showShortToast(message)
            }
        }
    }


    private fun updateUI(it: List<com.noisefit_commans.data.response.ChallengeModel>) {
        if (it.isNotEmpty()) {
            binding.rvNewChallenge.visible()
            binding.lytNoChallenge.root.gone()
            binding.lytNoChallenge.lytJoin.gone()
        } else {
            binding.rvNewChallenge.gone()
            binding.lytNoChallenge.root.visible()

            if ((tabSharedViewModel.newCount.value ?: 0) > 0) {
                binding.lytNoChallenge.lytJoin.visible()
            } else {
                binding.lytNoChallenge.lytJoin.gone()
            }
            binding.lytNoChallenge.textViewTitle.text =
                getString(R.string.text_no_challenges_joined_title)
            binding.lytNoChallenge.textViewMsg.text =
                getString(R.string.text_no_upcoming_challenges_msg)
        }
    }
}