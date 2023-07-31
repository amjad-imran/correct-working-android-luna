package com.noisefit.ui.challengeNew

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit_commans.data.model.challenge.ChallengeIds
import com.noisefit.luna.databinding.LayoutWatchfaceDeeplinkBinding
import com.noisefit.ui.challengeNew.detail.ChallengeDetailsViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.getDeeplinkPathArg
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HandleDeepLinkChallengeDetailFrag :
    BaseFragment<LayoutWatchfaceDeeplinkBinding>(LayoutWatchfaceDeeplinkBinding::inflate) {

    private val viewModel: ChallengeDetailsViewModel by viewModels()

    private var deepLinkType: String? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //findNavController().popBackStack(R.id.handleDeepLinkChallengeDetailsFragment, true)

        arguments?.let {
            try {
                val pathArgs = it.getDeeplinkPathArg()
                if (pathArgs.equals("challengedetails", true)) {
                    deepLinkType = pathArgs?.lowercase()
                } else if (pathArgs.equals("leaderboard", true)) {
                    deepLinkType = pathArgs?.lowercase()
                } else {
                    navigateUpSafe()
                }


                val deepLinkKey = it.keySet()?.firstOrNull()
                val uri = ((it.get(deepLinkKey) as Intent).data as Uri)
                val challengeId = uri.getQueryParameter("id")

                if (challengeId == null) navigateUpSafe()

                val parsedId = challengeId?.toIntOrNull()
                if (parsedId == null) navigateUpSafe()
                viewModel.challengeId = parsedId ?: 0
                viewModel.getChallengeDetailsByID(true)
            } catch (exp: Exception) {
                navigateUpSafe()
            }
        }
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

        viewModel.challengeDetails.observe(this) { challengeModel ->
            challengeModel?.let {


                if (deepLinkType.equals("challengedetails", true)) {
                    if (it.status.equals("completed", true)) {
                        navigate(
                            HandleDeepLinkChallengeDetailFragDirections.actionHandleDeepLinkChallengeDetailsFragmentToChallengeEndedFragment(
                                challengeModel.challenge_id
                            )
                        )
                    } else {
                        navigate(
                            HandleDeepLinkChallengeDetailFragDirections.actionHandleDeepLinkChallengeDetailsFragmentToChallengeDetailsFragment(
                                challengeModel.challenge_id
                            )
                        )
                    }
                } else if (deepLinkType.equals("leaderboard", true)) {
                    //TODO handling for leaderboard visibility

                    navigate(
                        HandleDeepLinkChallengeDetailFragDirections.actionHandleDeepLinkChallengeDetailsFragmentToChallengeLeaderboardFragment(
                            ChallengeIds(
                                id = it.challenge_id,
                                teamId = 0,
                                challengeType = it.type,
                                unit = viewModel.unit
                            )
                        )
                    )
                } else {
                    navigateUpSafe()
                }
            }
        }

        viewModel.getApiErrors().observe(this) {
            it.getContent()?.let {
                navigateUpSafe()
            }
        }
    }

}