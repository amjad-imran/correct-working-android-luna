package com.noisefit.ui.npl.dashboard

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.noisefit.R
import com.noisefit.databinding.FragmentNplCollectBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.response.LiveMatch
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.displayToast
import com.noisefit_commans.ui.loadCircleImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val NPL_COLLECT_MATCH = "NPL_COLLECT_MATCH"

@AndroidEntryPoint
class NplCollectFragment :
    BaseFragment<FragmentNplCollectBinding>(FragmentNplCollectBinding::inflate) {

    private var liveMatch: LiveMatch? = null
    private val collectViewModel: NplCollectSharedViewModel by activityViewModels()

    @Inject
    lateinit var sessionManager: SessionManager

    companion object {
        @JvmStatic
        fun newInstance(liveMatch: LiveMatch) =
            NplCollectFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(NPL_COLLECT_MATCH, liveMatch)

                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            liveMatch = it.getParcelable(NPL_COLLECT_MATCH)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        liveMatch?.let { setUi(it) }

    }

    private fun setUi(liveMatch: LiveMatch) {

        binding.lytWin.apply {

            layoutPredictWin.tvTeam1.text = liveMatch.teamA?.getTeamNames()
            layoutPredictWin.lytTeam1.ivTeam1.loadCircleImage(
                layoutPredictWin.lytTeam1.ivTeam1.context,
                liveMatch.teamA?.imageUrl
            )
            layoutPredictWin.tvTeam2.text = liveMatch.teamB?.getTeamNames()
            layoutPredictWin.lytTeam2.ivTeam1.loadCircleImage(
                layoutPredictWin.lytTeam2.ivTeam1.context,
                liveMatch.teamB?.imageUrl
            )

            tvDate.text = DateFormats.formatTimeNpl(liveMatch.startAt)

            if (liveMatch.winningTeam == liveMatch.teamA?.teamId) {
                layoutPredictWin.tvWon.text = "Won"
                layoutPredictWin.tvLost.text = "Lost"
                layoutPredictWin.tvWon.setTextColor(
                    ContextCompat.getColor(
                        layoutPredictWin.tvWon.context,
                        R.color.steps_arc
                    )
                )
                layoutPredictWin.tvLost.setTextColor(
                    ContextCompat.getColor(
                        layoutPredictWin.tvWon.context,
                        R.color.white_64
                    )
                )
                layoutPredictWin.tvTeam1.setTextColor(ContextCompat.getColor(
                    layoutPredictWin.tvTeam1.context,
                    R.color.white
                ))
                layoutPredictWin.tvTeam2.setTextColor(ContextCompat.getColor(
                    layoutPredictWin.tvTeam1.context,
                    R.color.white_48
                ))
                layoutPredictWin.lytTeam1.viewStroke.visible()
                layoutPredictWin.lytTeam1.ivTop.visible()
                layoutPredictWin.lytTeam1.ivTop.setImageResource(R.drawable.ic_accept_duotone)
            } else {

                layoutPredictWin.tvLost.text = "Won"
                layoutPredictWin.tvWon.text = "Lost"
                layoutPredictWin.tvLost.setTextColor(
                    ContextCompat.getColor(
                        layoutPredictWin.tvWon.context,
                        R.color.steps_arc
                    )
                )
                layoutPredictWin.tvWon.setTextColor(
                    ContextCompat.getColor(
                        layoutPredictWin.tvWon.context,
                        R.color.white_64
                    )
                )
                layoutPredictWin.tvTeam1.setTextColor(ContextCompat.getColor(
                    layoutPredictWin.tvTeam1.context,
                    R.color.white_48
                ))
                layoutPredictWin.tvTeam2.setTextColor(ContextCompat.getColor(
                    layoutPredictWin.tvTeam1.context,
                    R.color.white
                ))

                layoutPredictWin.lytTeam2.viewStroke.visible()
                layoutPredictWin.lytTeam2.ivTop.visible()
                layoutPredictWin.lytTeam2.ivTop.setImageResource(R.drawable.ic_accept_duotone)
            }


            this.layoutPredictWin.bPredictWinner.setOnClickListener {
                if (liveMatch.prediction_id != null) {
                    sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_CLAIMWINS_COLLECT_REWARD_CLICK)
                    collectViewModel.collectReward(liveMatch.prediction_id)
                } else {
                    requireActivity().displayToast("Something went wrong")
                }
            }

        }

    }

    override fun initListener() {

    }

    override fun subscribeObservers() {


    }


}