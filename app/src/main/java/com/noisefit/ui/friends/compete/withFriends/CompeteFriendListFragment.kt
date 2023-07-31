package com.noisefit.ui.friends.compete.withFriends

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.R
import com.noisefit_commans.data.response.CompeteFriend
import com.noisefit.databinding.FragmentCompeteFriendListBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CompeteFriendListFragment :
    BaseFragment<FragmentCompeteFriendListBinding>(FragmentCompeteFriendListBinding::inflate) {

    private val viewModel: CompeteFriendListViewModel by viewModels()

    private val mAdapter: CompeteFriendListAdapter by lazy {
        CompeteFriendListAdapter(object :
            CompeteFriendListAdapter.OnCompeteFriendInteractionListener {
            override fun onSendRequest(competeFriend: CompeteFriend, position: Int) {
                viewModel.updateCompetitionRequestStatus(competeFriend.id ?: -1, "sent") {
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.COMPETE_REQUESTSENT_CLICK)
                    mAdapter.setStatusPending(position)
                }
            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.layoutToolbar.tvTitle.text = getString(R.string.text_compete_with_your_friend)
        setRecycler()
    }

    private fun setRecycler() {
        with(binding.rvCompeteList) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

    }

    private fun setCompetitionRule() {
        binding.tvMessage.text = viewModel.rule
    }

    override fun initListener() {
        binding.layoutToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }


        viewModel.competeFriendList.observe(this) {
            it?.let {
                setCompetitionRule()
                mAdapter.setDataSet(it)

                if (it.isEmpty()) {
                    binding.lytEmpty.root.visible()
                    binding.lytEmpty.textViewMsg.gone()
                    binding.lytEmpty.tvEmptyMessage.text =
                        getString(R.string.text_compete_with_friend_empty_msg)
                    binding.rvCompeteList.gone()
                } else {
                    binding.divider1.root.visible()
                    binding.lytEmpty.root.gone()
                    binding.rvCompeteList.visible()
                }
            }
        }
    }


}