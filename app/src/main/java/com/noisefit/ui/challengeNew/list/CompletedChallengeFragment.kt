package com.noisefit.ui.challengeNew.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.R
import com.noisefit_commans.data.response.ChallengeModel
import com.noisefit.databinding.FragmentCompletedChallengeBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CompletedChallengeFragment :
    BaseFragment<FragmentCompletedChallengeBinding>(FragmentCompletedChallengeBinding::inflate) {

    private val viewModel: ChallengeListingViewModel by viewModels()
    private val tabSharedViewModel: MyChallengeTabSharedViewModel by activityViewModels()

    private val mAdapter: CompletedChallengeListAdapter by lazy {
        CompletedChallengeListAdapter(object : ChallengeCompleteRewardInteractionListener {
            override fun onItemClicked(challengeId: Int, challengeModel: com.noisefit_commans.data.response.ChallengeModel) {
                tabSharedViewModel.sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.CHALLENGE_COMPLETED_ITEM_CLICK, HashMap<String, Any>().apply {
                        this["challenges_completed_name"] = challengeModel.title.toString()
                        this["challenges_completed_type"] = challengeModel.type.toString()
                        this["challenges_completed_ID"] = challengeModel.challenge_id.toString()
                    }
                )
                navigate(
                    ChallengeListingFragmentDirections.actionNavigationChallengeToChallengeEndedFragment(
                        challengeId
                    )
                )
            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabSharedViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CHALLENGE_COMPLETED_LIST_PAGE_VISIT)
        setRecycler()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getCompletedChallenges()
    }

    private fun setRecycler() {
        with(binding.rvCompletedChallenge) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {
        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.completedChallenges.observe(this) {
            mAdapter.setDataSet(it)
            updateUI(it)

            /*   tabSharedViewModel.completedCount.value = it.size
               tabSharedViewModel.setSelected()*/

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
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
    }

    private fun updateUI(it: List<com.noisefit_commans.data.response.ChallengeModel>) {
        if (it.isNotEmpty()) {
            binding.rvCompletedChallenge.visible()
            binding.lytNoChallenge.root.gone()
            binding.lytNoChallenge.lytJoin.gone()
        } else {
            binding.rvCompletedChallenge.gone()
            binding.lytNoChallenge.root.visible()

            if ((tabSharedViewModel.newCount.value ?: 0) > 0) {
                binding.lytNoChallenge.lytJoin.visible()
            } else {
                binding.lytNoChallenge.lytJoin.gone()
            }
            binding.lytNoChallenge.textViewTitle.text =
                getString(R.string.text_no_challenges_completed_title)
            binding.lytNoChallenge.textViewMsg.text =
                getString(R.string.text_no_challenges_completed_msg)
        }
    }


}