package com.oreo.ui.lifeos

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsInsightCardBinding
import com.noisefit_commans.ui.loadImage
import com.oreo.ui.lifeos.charts.InsightCardUiModel

class LifeOsInsightCardFragment : Fragment(R.layout.fragment_life_os_insight_card) {

    private var _binding: FragmentLifeOsInsightCardBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLifeOsInsightCardBinding.bind(view)

        val insightData = arguments?.getParcelable<InsightCardUiModel>(ARG_INSIGHT_DATA)

        binding.tvTitle.text = insightData?.raw?.title
        if(insightData?.raw?.insightIcon==null){
            binding.ivInsight.setImageResource(R.drawable.ic_lifeos_star_small)
        }else{
            binding.ivInsight.loadImage(requireContext(), insightData.raw.insightIcon)
        }
//        if (time.isNotEmpty()) binding.tvTime.text = time

        binding.root.setOnClickListener {
            val bundle = Bundle().apply {
                putString("insightId", "")
            }
            findNavController().navigate(R.id.lifeOsInsightDetailsFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_INSIGHT_DATA = "arg_insight_data"

        fun newInstance(
            data: InsightCardUiModel
        ): LifeOsInsightCardFragment {
            val f = LifeOsInsightCardFragment()
            f.arguments = Bundle().apply {
                putParcelable(ARG_INSIGHT_DATA, data)
            }
            return f
        }
    }
}
