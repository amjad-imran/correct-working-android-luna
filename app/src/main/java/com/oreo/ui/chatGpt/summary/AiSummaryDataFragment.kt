package com.oreo.ui.chatGpt.summary

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.databinding.FragmentAiSummaryDataBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.loadImage
import com.oreo.data.model.AiDailySummaryModel
import com.oreo.data.model.DataMetrics
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AiSummaryDataFragment :
    BaseFragment<FragmentAiSummaryDataBinding>(FragmentAiSummaryDataBinding::inflate) {

    @Inject
    lateinit var sessionManager: SessionManager


    companion object {
        fun getInstance(data: AiDailySummaryModel): AiSummaryDataFragment {
            return AiSummaryDataFragment().apply {
                this.arguments = Bundle().apply {
                    this.putParcelable("data", data)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = arguments?.getParcelable<AiDailySummaryModel>("data")

        data?.let {
            initUI(it)
        }
    }

    private fun initUI(data: AiDailySummaryModel) {
        binding.tvTitle.text = data.title
        binding.tvSubtext.text = data.subTitle
        binding.ivBackground.loadImage(binding.ivBackground.context, data.bgImage)

        setRecycler(data.metrics)
    }

    private fun setRecycler(metrics: List<DataMetrics>?) {
        binding.rvDataMetrics.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDataMetrics.adapter = DataMetricsAdapter(sessionManager.isMetric()).apply {
            this.setDataSet(metrics ?: ArrayList())
        }
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}