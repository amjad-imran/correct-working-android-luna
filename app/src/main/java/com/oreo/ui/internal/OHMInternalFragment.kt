package com.oreo.ui.internal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOHMInternalBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.OHMDataModel
import com.oreo.ui.sleep2.SleepDashViewModel
import com.oreo.ui.sleep2.internal.SleepInternalDetailsFragment
import com.oreo.ui.sleep2.internal.SleepInternalLaunchState
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class OHMInternalFragment :
    BaseFragment<FragmentOHMInternalBinding>(FragmentOHMInternalBinding::inflate) {

    private val sleepDashViewModel: SleepDashViewModel by viewModels()
    private val viewModel: OHMInternalViewModel by viewModels()
    private val args: OHMInternalFragmentArgs by navArgs()

    private val mAdapter: OHMInternalAdapter by lazy {
        OHMInternalAdapter(object : OHMInternalAdapter.HMItemClickListener {
            override fun onItemClick(resultData: OHMDataModel, position: Int) {
                val launchType = sleepDashViewModel.getLaunchState(resultData.type)
              /*  val (frag, bundle) = SkinTempInternalDetailsFragment.getStartData(
                    launchType
                )
                navigate(frag, bundle)*/

                showInternalTrend(launchType)

            }
        })
    }

    fun showInternalTrend(state: SleepInternalLaunchState) {
        val (frag, bundle) = SleepInternalDetailsFragment.getStartData(
            state,LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        )
        navigate(frag, bundle)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            viewModel.healthTrend = args.healthTrend
        }
        setupUI()
    }

    private fun setupUI() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_health_monitor)
        with(binding.rvHm) {
            adapter = mAdapter
        }
        mAdapter.setData(viewModel.getHealthMonitorData())
    }

    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }


}