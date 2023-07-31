package com.noisefit.ui.friends.request

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.R
import com.noisefit_commans.data.response.Requests
import com.noisefit.databinding.FragmentCompeteRequestListBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.friends.request.received.CPRReceivedAdapter
import com.noisefit.ui.friends.request.received.OnItemClickListener
import com.noisefit.ui.friends.request.received.RequestReceivedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CompeteRequestListFragment :
    BaseFragment<FragmentCompeteRequestListBinding>(FragmentCompeteRequestListBinding::inflate) {

    private val viewModel: RequestReceivedViewModel by viewModels()

    private val cprReceivedAdapter: CPRReceivedAdapter by lazy {
        CPRReceivedAdapter(object : OnItemClickListener {

            override fun onAcceptClicked(requests: Requests,position: Int) {
                requireActivity().supportFragmentManager.setFragmentResultListener(
                    COMPETITION_ACCEPT_KEY,
                    this@CompeteRequestListFragment
                ) { _, bundle ->
                    val acceptChallenge = bundle.getBoolean("accept")
                    if (acceptChallenge) {
                        viewModel.updateCompetitionRequestStatus(requests, "accept") {
                            cprReceivedAdapter.removeItem(requests)
                            updateUiState()
                        }
                    } else {
                        viewModel.updateCompetitionRequestStatus(requests, "decline") {
                            cprReceivedAdapter.removeItem(requests)
                            updateUiState()
                        }
                    }
                }
                navigate(
                    CompeteRequestListFragmentDirections.actionNavigationCompeteRequestListFragmentToCompeteRuleAcceptBottomSheet(
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
               /* navigate(RequestFragmentDirections.actionRequestFragmentToFriendProfileFragment().apply{
                    friendId = requests.user_id
                })*/
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
        viewModel.getAllCompetitionRequests()
    }

    override fun initListener() {
        binding.layoutToolbar.tvTitle.text = getString(R.string.text_competetion_request)
        binding.layoutToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    fun updateUiState(){
        val requestCount = cprReceivedAdapter.itemCount

        if(requestCount==0){
            binding.lytNoRequestReceived.root.visible()
        }else{
            binding.lytNoRequestReceived.root.gone()
        }
    }

    override fun subscribeObservers() {

        viewModel.allCompetitionRequests.observe(this) {
            cprReceivedAdapter.setDataSet(it)
            updateUiState()
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

    }

    private fun setRecycler() {
        with(binding.rvCompeteRequest) {
            layoutManager = LinearLayoutManager(context)
            adapter = cprReceivedAdapter
        }
    }


}