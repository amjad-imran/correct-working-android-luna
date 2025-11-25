package com.oreo.ui.lifeos

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsInsightCardBinding
import com.oreo.ui.lifeos.charts.InsightCardUiModel

class LifeOsInsightCardFragment : Fragment(R.layout.fragment_life_os_insight_card) {

    private var _binding: FragmentLifeOsInsightCardBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLifeOsInsightCardBinding.bind(view)

        val text = arguments?.getString(ARG_TEXT).orEmpty()
        val time = arguments?.getString(ARG_TIME).orEmpty()
        val insightList = arguments?.getStringArrayList(ARG_LIST)

        binding.tvTitle.text = text
        if (time.isNotEmpty()) binding.tvTime.text = time

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
        private const val ARG_TEXT = "arg_text"
        private const val ARG_TIME = "arg_time"
        private const val ARG_LIST = "arg_list"

        fun newInstance(
            data: InsightCardUiModel
        ): LifeOsInsightCardFragment {
            val f = LifeOsInsightCardFragment()
            f.arguments = Bundle().apply {
                /*putString(ARG_TEXT, text)
                putString(ARG_TIME, time)
                if (!list.isNullOrEmpty()) putStringArrayList(ARG_LIST, list)*/
            }
            return f
        }
    }
}
