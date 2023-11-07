package com.oreo.ui.info

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentRingCareBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RingCareFragment : BaseFragment<FragmentRingCareBinding>(FragmentRingCareBinding::inflate) {

    val viewModel: RingCareViewModel by viewModels()
    val navArgs: RingCareFragmentArgs by navArgs()
    private val adapter: RingCareAdapter by lazy {
        RingCareAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.tvTitle.text = navArgs.title

        setRecycler()
        viewModel.getRingCareData()


    }

    private fun setRecycler() {
        binding.rvCare.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCare.adapter = adapter
    }


    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        viewModel.ringCarePoints.observe(this) {
            adapter.setDataSet(it)
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

    }

    override fun subscribeObservers() {

    }


}