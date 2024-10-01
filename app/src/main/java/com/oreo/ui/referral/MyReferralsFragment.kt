package com.oreo.ui.referral

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.databinding.FragmentMyReferralsBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MyReferralsFragment :
    BaseFragment<FragmentMyReferralsBinding>(FragmentMyReferralsBinding::inflate) {

    private val viewModel: MyReferralViewModel by viewModels()
    private val adapter: MyReferralsAdapter by lazy {
        MyReferralsAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecycler()
    }

    private fun setRecycler() {
        binding.rvReferrals.layoutManager = LinearLayoutManager(this.context)
        binding.rvReferrals.adapter =adapter

    }

    override fun initListener() {

    }

    override fun subscribeObservers() {
        viewModel.myReferrals.observe(this){
            adapter.setDataSet(it)
        }

    }


}