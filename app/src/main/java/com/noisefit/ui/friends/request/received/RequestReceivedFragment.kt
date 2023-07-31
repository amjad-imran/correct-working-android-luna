package com.noisefit.ui.friends.request.received

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.R
import com.noisefit_commans.data.response.Requests
import com.noisefit.databinding.FragmentRequestReceivedBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit.ui.friends.request.COMPETITION_ACCEPT_KEY
import com.noisefit.ui.friends.request.RequestFragmentDirections
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RequestReceivedFragment :
    BaseFragment<FragmentRequestReceivedBinding>(FragmentRequestReceivedBinding::inflate) {
    private val viewModel: RequestReceivedViewModel by viewModels()
    private val cprReceivedAdapter: CPRReceivedAdapter by lazy {
        CPRReceivedAdapter(object : OnItemClickListener {

            override fun onAcceptClicked(requests: Requests, position: Int) {

                requireActivity().supportFragmentManager.setFragmentResultListener(
                    COMPETITION_ACCEPT_KEY,
                    this@RequestReceivedFragment
                ) { _, bundle ->
                    val acceptChallenge = bundle.getBoolean("accept")
                    if (acceptChallenge) {
                        viewModel.updateCompetitionRequestStatus(requests, "accept") {
                            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.RR_COMPETITION_ACCEPT_CLICK)
                            cprReceivedAdapter.updateItemStatus("Accepted", position)
                            updateUiState()
                        }
                    } else {
                        viewModel.updateCompetitionRequestStatus(requests, "decline") {
                            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.RR_COMPETITION_DECLINE_CLICK)
                            cprReceivedAdapter.updateItemStatus("Declined", position)
                            updateUiState()
                        }
                    }
                }

                navigate(
                    RequestFragmentDirections.actionRequestFragmentToAcceptRuleCompetitionBottomSheet(
                        requests
                    )
                )
            }

            override fun onDeclineClicked(requests: Requests) {
                viewModel.updateCompetitionRequestStatus(requests, "decline") {
                    cprReceivedAdapter.removeItem(requests)
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

    private val pfrReceivedAdapter: PFRReceivedAdapter by lazy {
        PFRReceivedAdapter(object : OnItemClickListener {
            override fun onAcceptClicked(requests: Requests, position: Int) {
                viewModel.updateFriendRequestStatus(requests, FRIEND_STATUS_ACCEPT_STRING) {
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.RR_FRIENDREQUEST_ACCEPT_CLICK)
                    pfrReceivedAdapter.removeItem(requests)
                    updateUiState()
                }
            }

            override fun onDeclineClicked(requests: Requests) {
                viewModel.updateFriendRequestStatus(requests, FRIEND_STATUS_REJECTED_STRING) {
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.RR_FRIENDREQUEST_DECLINE_CLICK)
                    pfrReceivedAdapter.removeItem(requests)
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
        viewModel.getReceivedRequests()
    }

    override fun initListener() {
        binding.ivCprViewAll.setOnClickListener {
            navigate(R.id.competeRequestListFragment)
        }

    }

    private fun setRecycler() {
        with(binding.rvCpr) {
            layoutManager = LinearLayoutManager(context)
            adapter = cprReceivedAdapter
        }
        with(binding.rvPfr) {
            layoutManager = LinearLayoutManager(context)
            adapter = pfrReceivedAdapter
        }


        //updateUI(viewModel.getListData())


    }

    private fun updateUiState() {
        if (nullableBinding == null) return
        val competitionCount = cprReceivedAdapter.itemCount
        val friendRequestCount = pfrReceivedAdapter.itemCount



        if (competitionCount == 0 && friendRequestCount == 0) {
            binding.lytNoRequestReceived.tvEmptyMessage.text =
                getString(R.string.text_no_requests_received)
            binding.lytNoRequestReceived.textViewMsg.text =
                getString(R.string.text_always_fun_to_approach_first)
            binding.lytNoRequestReceived.root.visible()
        } else {
            binding.lytNoRequestReceived.root.gone()
        }

        if (competitionCount == 0) {
            binding.tvCpr.gone()
            binding.ivCprViewAll.gone()
            binding.rvCpr.gone()
            binding.divider1.root.gone()
        } else {
            binding.tvCpr.visible()
            binding.rvCpr.visible()
            binding.divider1.root.visible()
            if (competitionCount >= 2) {
                binding.ivCprViewAll.visible()
                binding.ivCprViewAll.isEnabled = true
            } else {

                binding.ivCprViewAll.invisible()
                binding.ivCprViewAll.isEnabled = false
            }
        }

        if (friendRequestCount == 0) {
            binding.tvPfr.gone()
            binding.rvPfr.gone()
        } else {
            binding.tvPfr.visible()
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
            pfrReceivedAdapter.setDataSet(it)
            updateUiState()
        }
        viewModel.competitionRequests.observe(this) {
            cprReceivedAdapter.setDataSet(it)
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