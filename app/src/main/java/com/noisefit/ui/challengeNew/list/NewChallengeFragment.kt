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
import kotlin.collections.HashMap


@AndroidEntryPoint
class NewChallengeFragment :
    BaseFragment<FragmentNewChallengeBinding>(FragmentNewChallengeBinding::inflate) {

    private val viewModel: ChallengeListingViewModel by viewModels()
    private val tabSharedViewModel: MyChallengeTabSharedViewModel by activityViewModels()

    private val mAdapter: NewChallengeListAdapter by lazy {
        NewChallengeListAdapter(object : OnChallengeClickListener {
            override fun onChallengeClicked(challengeId: Int, challengeModel: com.noisefit_commans.data.response.ChallengeModel) {
                navigate(
                    ChallengeListingFragmentDirections.actionNavigationChallengeToChallengeDetailsFragment(
                        challengeId
                    )
                )
                tabSharedViewModel.sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.CHALLENGE_NEW_ITEM_CLICK, HashMap<String, Any>().apply {
                        this["challenges_new_name"] = challengeModel.title.toString()
                        this["challenges_new_type"] = challengeModel.type.toString()
                        this["challenges_new_ID"] = challengeModel.challenge_id.toString()
                    }
                )

            }
        })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabSharedViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CHALLENGE_NEW_LIST_PAGE_VISIT)
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
                } else {
                    tabSharedViewModel.setUpdatedText(
                        "Updated ${
                            DateFormats.getRelativeTime(
                                viewModel.challengeLastSyncTime
                            )
                        }"
                    )
                }
            } else {
                tabSharedViewModel.setUpdatedText("")
            }
        }

        viewModel.currentCount.observe(this) {
            tabSharedViewModel.newCount.value = it.first
            tabSharedViewModel.joinedCount.value = it.second
            //
            if(it.second>0 && it.first==0){
                tabSharedViewModel.moveToJoinChallenge()
            }
            tabSharedViewModel.setSelected()
        }

        viewModel.newChallenges.observe(this) {
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
        viewModel.getMessages().observe(this) {
            it.getContent()?.let {message->
                context.showShortToast(message)
            }
        }


    }

    private fun updateUI(it: List<com.noisefit_commans.data.response.ChallengeModel>) {
        if (it.isNotEmpty()) {
            binding.rvNewChallenge.visible()
            binding.lytNoChallenge.root.gone()
        } else {
            binding.rvNewChallenge.gone()
            binding.lytNoChallenge.root.visible()
            binding.lytNoChallenge.textViewTitle.text =
                getString(R.string.text_no_upcoming_challenges_title)
            binding.lytNoChallenge.textViewMsg.text =
                getString(R.string.text_new_challenge_message)
        }
    }
}