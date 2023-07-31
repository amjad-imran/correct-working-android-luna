package com.noisefit.ui.npl.summary

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.noisefit.MainViewModel
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLiveScoreBinding
import com.noisefit.session.SessionManager
import com.noisefit.ui.common.*
import com.noisefit.ui.dashboard.summary.SummaryFragmentDirections
import com.noisefit.ui.npl.NPL_TERMS_KEY
import com.noisefit.ui.npl.NplPrivacyBottomDialogFragment
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.response.LiveMatch
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadCircleImage
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.numberFormatter
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val LIVE_SCORE_KEY = "LIVE_SCORE_KEY"

@AndroidEntryPoint
class LiveScoreFragment :
    BaseFragment<FragmentLiveScoreBinding>(FragmentLiveScoreBinding::inflate) {

    @Inject
    lateinit var localDataStore: DataStoredInterface
    @Inject
    lateinit var sessionManager: SessionManager
    private var liveMatch: LiveMatch? = null
    private val mainViewModel: MainViewModel by activityViewModels()

    companion object {
        @JvmStatic
        fun newInstance(liveMatch: LiveMatch) =
            LiveScoreFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(LIVE_SCORE_KEY, liveMatch)

                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            liveMatch = it.getParcelable(LIVE_SCORE_KEY)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lytScore.apply {
            tvTeam1.text = liveMatch?.teamA?.getTeamNames()
            tvTeam2.text = liveMatch?.teamB?.getTeamNames()
            ivPowered.loadImage(requireContext(), liveMatch?.sponsorImage)
            tvPredictValue.text = (liveMatch?.predictionCount ?: 0L).numberFormatter()
            lytTeam1.ivTeam1.loadCircleImage(requireContext(), liveMatch?.teamA?.imageUrl)
            lytTeam2.ivTeam1.loadCircleImage(requireContext(), liveMatch?.teamB?.imageUrl)
            when (liveMatch?.userTeam) {
                liveMatch?.teamA?.teamId -> {
                    lytTeam1.ivTop.visible()
                    lytTeam2.ivTop.gone()
                    lytTeam1.viewStroke.visible()
                    lytTeam2.viewStroke.gone()
                }
                liveMatch?.teamB?.teamId -> {
                    lytTeam2.ivTop.visible()
                    lytTeam1.ivTop.gone()
                    lytTeam1.viewStroke.gone()
                    lytTeam2.viewStroke.visible()
                }
                else -> {
                    lytTeam1.ivTop.gone()
                    lytTeam1.viewStroke.gone()
                    lytTeam2.viewStroke.gone()
                    lytTeam2.ivTop.gone()
                }
            }

            if (liveMatch?.teamA?.score != null) {
                val run = "${liveMatch?.teamA?.score?.run}/${liveMatch?.teamA?.score?.wickets}"
                val over = "(${liveMatch?.teamA?.score?.over})"
                tvScore.text = run
                tvOver.text = over
                tvOver.visible()
                tvScore.visible()
                team1YetToBat.gone()
            } else {
                tvOver.invisible()
                tvScore.invisible()
                team1YetToBat.visible()
            }

            if (liveMatch?.teamB?.score != null) {
                val run = "${liveMatch?.teamB?.score?.run}/${liveMatch?.teamB?.score?.wickets}"
                val over = "(${liveMatch?.teamB?.score?.over})"
                tvScore2.text = run
                tvOver2.text = over
                tvOver2.visible()
                tvScore2.visible()
                team2YetToBat.gone()
            } else {
                tvOver2.invisible()
                tvScore2.invisible()
                team2YetToBat.visible()
            }


            when (liveMatch?.match_status?.lowercase()) {
                "live" -> {
                    tvMatchStatus.text = getString(R.string.text_vs_small)
                    tvMatchStatus.setTextColor(requireContext().getColor(com.noisefit_commans.R.color.purple_))
                    binding.lytScore.rootView.setBackgroundResource(R.drawable.back_modal_purple)
                }
                "paused" -> {
                    tvMatchStatus.text = "Match\nPaused"
                    tvMatchStatus.setTextColor(requireContext().getColor(com.noisefit_commans.R.color.purple_))
                    binding.lytScore.rootView.setBackgroundResource(R.drawable.back_modal_purple)
                }
                "abondoned" -> {
                    tvMatchStatus.text = "Abondoned"
                    tvMatchStatus.setTextColor(requireContext().getColor(R.color.color_error))
                    binding.lytScore.rootView.setBackgroundResource(R.drawable.back_modal_new_red)
                }
            }


            if (liveMatch?.isSuperOver == true) {
                tvMatchStatus.text = "Super\nOver"
                tvMatchStatus.setTextColor(requireContext().getColor(com.noisefit_commans.R.color.purple_))
                binding.lytScore.rootView.setBackgroundResource(R.drawable.back_modal_purple)
            }
        }

        binding.lytScore.rootView.setOnClickListener {

            val nplPrivacyAccepted = localDataStore.getNplPrivacyPolicyStatus()
            if (nplPrivacyAccepted) {
                sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_HOMEPAGE_SCOREBOARD_CLICK)
                mainViewModel.playNplAnim = true
                this@LiveScoreFragment.navigate(R.id.nplDashboardFragment)

            } else
                showPrivacyBottomSheet()
        }

    }

    private fun showPrivacyBottomSheet() {

        requireActivity().supportFragmentManager.setFragmentResultListener(
            NPL_TERMS_KEY,
            this
        ) { _, bundle ->
            val agree = bundle.getBoolean("agree")
            if (agree) {
                localDataStore.setNplPrivacyPolicyStatus(true)
                mainViewModel.playNplAnim = true
                this@LiveScoreFragment.navigate(R.id.nplDashboardFragment)
            }
        }
        navigate(SummaryFragmentDirections.actionSummaryFragmentToNplPrivacyBottomDialogFragment())

    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }


}