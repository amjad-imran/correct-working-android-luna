package com.noisefit.ui.friends.request.sent


import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.R
import com.noisefit_commans.data.response.Requests
import com.noisefit.databinding.FragmentRequestSentBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.friends.request.RequestFragmentDirections
import com.noisefit.ui.friends.request.received.FRIEND_STATUS_REMOVE_STRING
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RequestSentFragment :
    BaseFragment<FragmentRequestSentBinding>(FragmentRequestSentBinding::inflate) {
    private val viewModel: RequestSentViewModel by viewModels()
    private val cprSentAdapter: CPRSentAdapter by lazy {
        CPRSentAdapter(object : OnItemClickListener {
            override fun onRemoveClicked(requests: Requests) {}

            override fun onItemClicked(requests: Requests) {
                navigate(
                    RequestFragmentDirections.actionRequestFragmentToFriendProfileFragment().apply {
                        friendId = requests.user_id
                    })
            }
        })
    }
    private val pfrSentAdapter: PFRSentAdapter by lazy {
        PFRSentAdapter(object : OnItemClickListener {
            override fun onRemoveClicked(requests: Requests) {
                viewModel.updateFriendRequestStatus(requests, FRIEND_STATUS_REMOVE_STRING) {
                    pfrSentAdapter.removeItem(requests)
                    updateUiState()
                }
            }

            override fun onItemClicked(requests: Requests) {
                navigate(
                    RequestFragmentDirections.actionRequestFragmentToFriendProfileFragment().apply {
                        friendId = requests.user_id
                    })
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
        viewModel.getSentRequests()
    }

    private fun setRecycler() {
        with(binding.rvPfr) {
            layoutManager = LinearLayoutManager(context)
            adapter = pfrSentAdapter
        }
        with(binding.rvCpr) {
            layoutManager = LinearLayoutManager(context)
            adapter = cprSentAdapter
        }
    }

    override fun initListener() {

        binding.lytNoSentRequest.bJoinChallenge.setOnClickListener {
            navigate(R.id.addFriendsFragment)
        }
    }

    private fun updateUiState() {

        val competitionCount = cprSentAdapter.itemCount
        val friendRequestCount = pfrSentAdapter.itemCount


        if (competitionCount == 0 && friendRequestCount == 0) {
            binding.lytNestedScroll.gone()
            binding.lytNoSentRequest.root.visible()
        } else {
            binding.lytNestedScroll.visible()
            binding.lytNoSentRequest.root.gone()
        }

        if (competitionCount == 0) {
            binding.tvPfr.gone()
            binding.rvCpr.gone()
            binding.divider1.root.gone()
        } else {
            binding.tvPfr.visible()
            binding.rvCpr.visible()
            binding.divider1.root.visible()
        }

        if (friendRequestCount == 0) {
            binding.tvCpr.gone()
            binding.rvPfr.gone()
        } else {
            binding.tvCpr.visible()
            binding.rvPfr.visible()
        }


    }

    override fun subscribeObservers() {

        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.friendRequests.observe(this) {
            pfrSentAdapter.setDataSet(it)
            updateUiState()
        }
        viewModel.competitionRequests.observe(this) {
            cprSentAdapter.setDataSet(it)
            updateUiState()
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
    }


}