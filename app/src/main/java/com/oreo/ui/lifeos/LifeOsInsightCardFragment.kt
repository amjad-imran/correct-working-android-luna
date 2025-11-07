package com.oreo.ui.lifeos

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsInsightCardBinding

class LifeOsInsightCardFragment : Fragment(R.layout.fragment_life_os_insight_card) {

    private var _binding: FragmentLifeOsInsightCardBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLifeOsInsightCardBinding.bind(view)

        val text = arguments?.getString(ARG_TEXT).orEmpty()
        val time = arguments?.getString(ARG_TIME).orEmpty()

        binding.tvTitle.text = text
        if (time.isNotEmpty()) binding.tvTime.text = time
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TEXT = "arg_text"
        private const val ARG_TIME = "arg_time"

        fun newInstance(text: String, time: String = "4 hrs ago"): LifeOsInsightCardFragment {
            val f = LifeOsInsightCardFragment()
            f.arguments = Bundle().apply {
                putString(ARG_TEXT, text)
                putString(ARG_TIME, time)
            }
            return f
        }
    }
}

