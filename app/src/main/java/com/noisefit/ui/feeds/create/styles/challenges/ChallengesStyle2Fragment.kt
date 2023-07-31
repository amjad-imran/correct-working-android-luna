package com.noisefit.ui.feeds.create.styles.challenges

import android.os.Bundle
import android.view.View
import com.noisefit.luna.databinding.FragmentChallengeStyle2Binding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.response.ChallengeModel
import com.noisefit_commans.models.Units
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.prettyCount
import com.noisefit_commans.utils.prettyCountDecimal
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChallengesStyle2Fragment :
    BaseFragment<FragmentChallengeStyle2Binding>(FragmentChallengeStyle2Binding::inflate) {

    companion object {
        val SELECTED_CHALLENGE = "SELECTED_CHALLENGE"

        @JvmStatic
        fun newInstance(selectedChallenge: ChallengeModel) =
            ChallengesStyle2Fragment().apply {
                arguments = Bundle().apply {
                    this.putParcelable(SELECTED_CHALLENGE, selectedChallenge)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val challenge =
            arguments?.getParcelable(ChallengesStyle1Fragment.SELECTED_CHALLENGE) as? ChallengeModel

        challenge?.let {

            val days = it.getHistoryDays().size
            binding.tvDays.text = "$days"
            binding.textDays.text = if (days == 1) "day" else "days"

            binding.tvSteps.text = it.getFormattedProgress().toDouble().prettyCountDecimal()

            binding.tvUnit.text = ApplicationUtils.getChallengeTypeUnit(
                it.type.toString(),
                Units.METRIC
            )


            binding.tvUserRank.text = (it.user_rank ?: 0L).prettyCount()
            binding.tvTotal.text = "/" + (it.participants ?: 0L).prettyCount()
            binding.tvChallengeTitle.text = it.title

        }


    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}