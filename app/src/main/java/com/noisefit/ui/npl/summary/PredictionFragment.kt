package com.noisefit.ui.npl.summary


import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import com.noisefit.MainViewModel
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentPredictionBinding
import com.noisefit.session.SessionManager
import com.noisefit.ui.common.*
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit.ui.dashboard.summary.SummaryFragmentDirections
import com.noisefit.ui.friends.location.search.CLOSED_SEARCH_STATE_KEY
import com.noisefit.ui.npl.NPL_TERMS_KEY
import com.noisefit.ui.npl.NplPrivacyBottomDialogFragment
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.response.PrizeInfo
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.numberFormatter
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val PREDICTION_KEY = "PREDICTION_KEY"

@AndroidEntryPoint
class PredictionFragment :
    BaseFragment<FragmentPredictionBinding>(FragmentPredictionBinding::inflate) {
    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var sessionManager: SessionManager
    private var prizeInfo: PrizeInfo? = null
    private val mainViewModel: MainViewModel by activityViewModels()

    companion object {
        @JvmStatic
        fun newInstance(prizeInfo: PrizeInfo) =
            PredictionFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PREDICTION_KEY, prizeInfo)

                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            prizeInfo = it.getParcelable(PREDICTION_KEY)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lytXWins.apply {
            ivMoreXWins.visible()
            tvTitle.visible()
            tvTitle.text = prizeInfo?.title
        }
        binding.lytXWins.rootView.setBackgroundResource(R.drawable.back_modal_purple)

        val percentage =
            prizeInfo?.userWins?.toFloat()?.calculatePercentage(prizeInfo?.rewardWins?.toFloat())


        if ((prizeInfo?.eligibleCount ?: 0) > 0) {
            binding.lytXWins.layoutPredictWin.textEligible.visible()
            binding.lytXWins.layoutPredictWin.tvEligibleCount.visible()

            binding.lytXWins.layoutPredictWin.tvEligibleCount.text =
                (prizeInfo?.eligibleCount ?: 0).numberFormatter()
        } else {
            binding.lytXWins.layoutPredictWin.textEligible.gone()
            binding.lytXWins.layoutPredictWin.tvEligibleCount.gone()
        }

        val requiredWins = (prizeInfo?.rewardWins ?: 0) - (prizeInfo?.userWins ?: 0)
        if (prizeInfo?.userWins!! >= prizeInfo?.rewardWins!!) {
            val predictAway = buildSpannedString {
                inSpans(
                    ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.white))
                ) {
                    append("Woohoo! You are now eligible for a lucky draw to win an iPhone 14 Pro")
                }
            }
            binding.lytXWins.layoutPredictWin.apply {
                tvTitle.text = predictAway
                tvMatchesLeft.gone()
                tvPowered.text = prizeInfo?.message
                pbSteps.progress = percentage?.toInt() ?: 0
                ivWinPrize.loadImage(
                    requireContext(),
                    prizeInfo?.imageUrl,
                    R.drawable.placeholder_banner
                )
            }
        } else {
            val predictAway = buildSpannedString {
                inSpans(
                    ForegroundColorSpan(ContextCompat.getColor(requireContext(), com.noisefit_commans.R.color.purple_))
                ) {
                    append(requiredWins.toString())
                    append(" wins")
                }
                append(" away from ")
                inSpans(
                    ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.white))
                ) {
                    append(prizeInfo?.prizeName.toString())
                }
            }
            val matchLeft = buildSpannedString {
                append("Matches left: ")
                inSpans(
                    ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.white))
                ) {
                    append(prizeInfo?.matchesLeft.toString())
                }
            }
            binding.lytXWins.layoutPredictWin.apply {
                tvTitle.text = predictAway
                tvMatchesLeft.text = matchLeft
                tvPowered.text = prizeInfo?.message
                pbSteps.progress = percentage?.toInt() ?: 0
                ivWinPrize.loadImage(
                    requireContext(),
                    prizeInfo?.imageUrl,
                    R.drawable.placeholder_banner
                )
            }

            binding.lytXWins.tvTitle.text = prizeInfo?.title
        }


        binding.lytXWins.rootView.setOnClickListener {
            val nplPrivacyAccepted = localDataStore.getNplPrivacyPolicyStatus()
            if (nplPrivacyAccepted) {
                sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_HOMEPAGE_NPL_CLICK)
                mainViewModel.playNplAnim = true
                this@PredictionFragment.navigate(R.id.nplDashboardFragment)
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
                this@PredictionFragment.navigate(R.id.nplDashboardFragment)
            }
        }
        navigate(SummaryFragmentDirections.actionSummaryFragmentToNplPrivacyBottomDialogFragment())
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {


    }


}
