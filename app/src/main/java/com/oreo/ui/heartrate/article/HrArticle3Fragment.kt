package com.oreo.ui.heartrate.article

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentHrArticle3Binding
import com.noisefit_commans.ui.BaseFragment


class HrArticle3Fragment :
    BaseFragment<FragmentHrArticle3Binding>(FragmentHrArticle3Binding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.tvTitle.text = getString(R.string.what_are_heart_rate_zones)

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
                    "•",
                    "Zone 1 (50-60% MHR): Light cardio, like brisk walking. This zone enhances aerobic capacity and encourages fat-burning, ideal for beginners or warm-ups.",
                    "Zone 1 (50-60% MHR):"
                )
            )
            this.add(
                HrArticlePoint(
                    "•",
                    "Zone 2 (60-70% MHR): Improves cardiovascular endurance, like jogging. This zone boosts heart health and overall stamina, making it suitable for moderate-intensity workouts.",
                    "Zone 2 (60-70% MHR):"
                )
            )
            this.add(
                HrArticlePoint(
                    "•",
                    "Zone 3 (70-80% MHR): Enhances aerobic fitness and endurance, similar to running or fast cycling. This zone is effective for improving overall cardiovascular performance.",
                    "Zone 3 (70-80% MHR):"
                )
            )
            this.add(
                HrArticlePoint(
                    "•",
                    "Zone 4 (80-90% MHR): Increases maximum performance and speed, ideal for high-intensity interval training (HIIT). This zone should be approached with caution and is best for short bursts of intense activity.",
                    "Zone 4 (80-90% MHR):"
                )
            )
            this.add(
                HrArticlePoint(
                    "•",
                    "Zone 5 (90-100% MHR): Maximum effort for short bursts, used in sprinting or very high-intensity workouts. This zone pushes your limits and is typically used by advanced athletes.",
                    "Zone 5 (90-100% MHR):"
                )
            )
        }
    }

}