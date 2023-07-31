package com.noisefit.ui.reward.about

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.databinding.FragmentAboutStreaksBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.reward.voucher.voucherdetails.TCRedeemAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutStreaksFragment :
    BaseFragment<FragmentAboutStreaksBinding>(FragmentAboutStreaksBinding::inflate) {

    private val viewModel: AboutStreaksViewModel by viewModels()

    private val streaksInfoAdapter: AboutStreaksAdapter by lazy {
        AboutStreaksAdapter()
    }

    private val mTCRedeemAdapter: TCRedeemAdapter by lazy {
        TCRedeemAdapter()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
        viewModel.getStreaksAboutData()
    }

    private fun setRecycler() {
        with(binding.rvStreaks) {
            layoutManager = LinearLayoutManager(context)
            adapter = streaksInfoAdapter
        }
        with(binding.rvTerms) {
            adapter = mTCRedeemAdapter
        }
    }

    override fun initListener() {
        binding.backBtn.apply {
            setOnClickListener {
                navigateUpSafe()
            }
        }
    }

    override fun subscribeObservers() {

        viewModel.aboutStreaks.observe(this) {
            streaksInfoAdapter.setDataSet(it)
        }
        viewModel.termsAndCondition.observe(this) {
            mTCRedeemAdapter.setDataSet(it)
        }


        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }

}