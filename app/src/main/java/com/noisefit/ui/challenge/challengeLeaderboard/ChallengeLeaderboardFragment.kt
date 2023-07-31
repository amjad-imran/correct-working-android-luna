package com.noisefit.ui.challenge.challengeLeaderboard

import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit_commans.data.model.challenge.ChallengeIds
import com.noisefit_commans.data.response.Leadership
import com.noisefit.luna.databinding.FragmentChallengeLeaderboardBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.friends.request.received.FRIEND_STATUS_ADD_STRING
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ChallengeLeaderboardFragment : BaseFragment<FragmentChallengeLeaderboardBinding>(
    FragmentChallengeLeaderboardBinding::inflate
) {

    private val viewModel: ChallengeLeaderboardViewModel by viewModels()

    private lateinit var allAdapter: LeaderboardAdapter
    private var ids: ChallengeIds? = null


    @Inject
    lateinit var localDataStore: DataStoredInterface

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ids = arguments?.let {
            ChallengeLeaderboardFragmentArgs.fromBundle(it).ids
        }

        initUI()
        setRecycler()
        ids?.let {
            viewModel.getLeaderboard(false, it)
        }
    }


    private fun initUI() {
        binding.toolbar.tvTitle.text = getString(R.string.leaderboard)

    }

    private fun setRecycler() {


        allAdapter = LeaderboardAdapter(object : AllCardClickListener {
            override fun onCardClicked(leadership: Leadership, position: Int) {
                viewModel.mLastClickTime?.let {
                    if (SystemClock.elapsedRealtime() - it < 1000) {
                        LOGS.d("Returning from Add Buddy click")
                        return
                    }
                }
                viewModel.mLastClickTime = SystemClock.elapsedRealtime()
                viewModel.addFriendRequest(leadership.user_id ?: -1, FRIEND_STATUS_ADD_STRING) {
                    allAdapter.updateStatus(position)
                }
            }
        }, context)
        with(binding.rvAll) {
            layoutManager = LinearLayoutManager(context)
            adapter = allAdapter
        }
    }


    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (it) {
                binding.pbLoading.visible()
            } else {
                binding.pbLoading.gone()
            }
        }



        viewModel.allLeaders.observe(viewLifecycleOwner) {
            ids?.let { ids ->

                allAdapter.setDataSet(it, ids.unit, ids.challengeType)
            }
        }
        viewModel.getMessages().observe(viewLifecycleOwner) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }
}