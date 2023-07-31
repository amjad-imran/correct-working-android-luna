package com.noisefit.ui.npl.htp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentHowToPlayBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit.ui.reward.voucher.voucherdetails.TCRedeemAdapter
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HowToPlayFragment :
    BaseFragment<FragmentHowToPlayBinding>(FragmentHowToPlayBinding::inflate) {

    @Inject
    lateinit var sessionManager: SessionManager

    private val mTCRedeemAdapter: TCRedeemAdapter by lazy {
        TCRedeemAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_HOWTOPLAY_PAGE_VISIT)

        initUi()

    }

    private fun initUi() {

        binding.lytData.lyt1.apply {
            tvTitle.text = "Predict the winner"
            tvMsg1.text = "Select your team and submit your prediction before the timer runs out."
            imageView.loadImage(
                imageView.context,
                R.drawable.predict_now_not_predicted,
            )
            tvMsg2.text =
                "The prediction window opens at 12am and closes one minute before the match starts so make sure you get your prediction in on time."
        }

        binding.lytData.lyt2.apply {
            tvTitle.text = "Claim your wins"
            tvMsg1.text = "Get exciting rewards with every correct predictions"
            imageView.loadImage(
                imageView.context,
                R.drawable.scoreboard_predicted,
            )
            tvMsg2.gone()
        }

        binding.lytData.lyt3.apply {
            tvTitle.text = "Bonus points"
            tvMsg1.text =
                "Completing your daily step goal along with the right prediction wins you bonus Noise Coins."
            imageView.loadImage(
                imageView.context,
                R.drawable.streak_1,
            )
            tvMsg2.gone()
        }

        binding.lytData.lyt4.apply {
            tvTitle.text = "Fastest correct prediction"
            tvMsg1.text =
                "The fastest correct prediction daily wins Fancode voucher worth Rs 1,000."
            imageView.loadImage(
                imageView.context,
                R.drawable.ic_fancode_voucher,
            )
            tvMsg2.gone()
        }

        with(binding.lytData.rvTerms) {
            adapter = mTCRedeemAdapter
        }

        mTCRedeemAdapter.setDataSet(
            arrayListOf(
                "Every 5 matches, two lucky winner will get Puma vouchers worth Rs. 4,000, and after 10 matches, two lucky winner will get Noise products.",
                "The top 10 predictors at the end of the league will win a kit of Puma vouchers & Man Company merchandise."
            )
        )


        binding.lytData.lyt6.apply {
            tvTitle.text = "NPL champion"
            tvMsg1.text =
                "At the end of the tournament, users who correctly predict 30 matches stand the chance to win an iPhone 14 Pro via a lucky draw. Exciting, right?"
            imageView.loadImage(
                imageView.context,
                R.drawable.npl_xwins_widget,
            )
            tvMsg2.gone()
        }
    }

    override fun initListener() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_how_to_play_title)
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }


}