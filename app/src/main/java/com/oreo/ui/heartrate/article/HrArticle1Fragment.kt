package com.oreo.ui.heartrate.article

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentHrArticle1Binding
import com.noisefit_commans.ui.BaseFragment


class HrArticle1Fragment :
    BaseFragment<FragmentHrArticle1Binding>(FragmentHrArticle1Binding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.tvTitle.text = getString(R.string.text_hra1_title)

        setRecycler()
    }

    private fun setRecycler() {
        binding.rvPoints.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPoints.adapter = HrArticle1Adapter(getData())
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }

    fun getData(): List<HrArticlePoint> {
        return ArrayList<HrArticlePoint>().apply {
            this.add(
                HrArticlePoint(
                    "1.",
                    getString(R.string.text_hra_1),
                    getString(R.string.text_hra_1_h)
                )
            )
            this.add(
                HrArticlePoint(
                    "2.",
                    getString(R.string.text_hra_2),
                    getString(R.string.text_hra_2_h)
                )
            )
            this.add(
                HrArticlePoint(
                    "3.",
                    getString(R.string.text_hra_3),
                    getString(R.string.text_hra_3_h)
                )
            )
            this.add(
                HrArticlePoint(
                    "4.",
                    getString(R.string.text_hra_4),
                    getString(R.string.text_hra_4_h)
                )
            )
            this.add(
                HrArticlePoint(
                    "•",
                    getString(R.string.text_hra_5),
                    getString(R.string.text_hra_5_h)
                )
            )
            this.add(
                HrArticlePoint(
                    "•",
                    getString(R.string.text_hra_6),
                    getString(R.string.text_hra_6_h)
                )
            )
            this.add(
                HrArticlePoint(
                    "•",
                    getString(R.string.text_hra_7),
                    getString(R.string.text_hra_7_h)
                )
            )
            this.add(
                HrArticlePoint(
                    "•",
                    getString(R.string.text_hra_8),
                    getString(R.string.text_hra_8_h)
                )
            )
            this.add(
                HrArticlePoint(
                    "•",
                    getString(R.string.text_hra_9),
                    getString(R.string.text_hra_9_h)
                )
            )
            this.add(
                HrArticlePoint(
                    "5.",
                    getString(R.string.text_hra_10),
                    getString(R.string.text_hra_10_h)
                )
            )
        }
    }


}