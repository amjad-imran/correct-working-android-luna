package com.noisefit.ui.friends.compete

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCompetitionsListBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.friends.FriendsFragmentDirections
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint
import me.dkzwm.widget.srl.RefreshingListenerAdapter

@AndroidEntryPoint
class CompetitionsListFragment :
    BaseFragment<FragmentCompetitionsListBinding>(FragmentCompetitionsListBinding::inflate) {

    private val sharedViewModel: FriendSharedViewModel by activityViewModels()
    private val viewModel: CompeteListViewModel by viewModels()

    private val mAdapter: CompeteListAdapter by lazy {
        CompeteListAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.animationView.setAnimation(R.raw.loading_swipe_anim)
        setRecycler()

    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchCompeteList(false)
    }

    private fun setRecycler() {
        with(binding.rvCompeteList) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
    }


    override fun initListener() {

        binding.swipeToRefresh.setOnRefreshListener(object : RefreshingListenerAdapter() {
            override fun onRefreshing() {
                super.onRefreshing()
                binding.textSyncingData.visible()
                viewModel.fetchCompeteList(true)
            }
        })
        binding.tvCompeteWithFriends.setOnClickListener {
            navigate(R.id.competeFriendListFragment)
        }
        binding.lytEmptyList.bAddFriends.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.COMPETITION_ADDFRIENDS_CLICK)
            navigate(FriendsFragmentDirections.actionNavigationFriendsFragmentToAddFriendsFragment())
        }

        mAdapter.itemClickListener = { _, data ->
            when (data.status) {
                "me" -> {
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.COMPETE_COMPETEFRIENDS_CLICK)
                    navigate(R.id.competeFriendListFragment)
                }
            }

        }

    }

    override fun subscribeObservers() {

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
                stopRefresh()
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.competeList.observe(this) {
            it?.let {
                stopRefresh()
                mAdapter.setDataSet(it, viewModel.currentTime)
                updateUI()
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                stopRefresh()
                uiController.onApiErrorReceived(response)
            }
        }
    }

    fun stopRefresh(){
        binding.swipeToRefresh.refreshComplete()
        binding.textSyncingData.gone()
    }
    private fun updateUI() {

        binding.lytEmptyList.tvMessage.text=getString(R.string.text_get_fit_with_your_friends)
        binding.lytEmptyList.vExercise.setAnimation(R.raw.anim_compete_empty_rings)
        binding.lytEmptyList.vExercise.playAnimation()
        binding.lytEmptyList.vExercise.repeatCount = 0

        /*binding.lytEmptyList.vJoin.setAnimation(R.raw.anim_join_challenge)
        binding.lytEmptyList.vJoin.playAnimation()
        binding.lytEmptyList.vJoin.repeatCount = LottieDrawable.INFINITE*/

        if (sharedViewModel.friendsListSize == 0) {
            binding.lytEmptyList.root.visible()
            binding.rvCompeteList.gone()
        } else {
            binding.rvCompeteList.visible()
            binding.lytEmptyList.root.gone()
        }

    }



}


