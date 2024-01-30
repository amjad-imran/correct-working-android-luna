package com.oreo.ui.stress

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOStressDataMovementBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.ui.custom.StressCombineModel
import com.oreo.ui.custom.StressCombinedChart


class OStressDataMovementFragment :
    BaseFragment<FragmentOStressDataMovementBinding>(FragmentOStressDataMovementBinding::inflate) {
    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private val movementViewModel: OStressDataMovementViewModel by viewModels()
    private val ARGS_DATE = "ARGS_DATE"

    companion object {

        @JvmStatic
        fun newInstance(date: String) = OStressDataMovementFragment().apply {
            arguments = Bundle().apply {
                putString(ARGS_DATE, date)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleMovementViews()
        handleBannerView(60)
        handleStressProgressView()
        initCombineChart()
    }

    override fun initListener() {
        binding.ivOpen.setOnClickListener {
            handleMovementViews(true)
        }
        binding.ivClose.setOnClickListener {
            handleMovementViews(false)
        }

    }

    private fun handleStressProgressView() {
        binding.lytStressHeader.lytStressProgress.viewCalm.layoutParams =
            binding.lytStressHeader.lytStressProgress.viewCalm.layoutParams.apply {
                (this as LinearLayout.LayoutParams).weight =
                    calculateWeightPercent(60)
            }

        binding.lytStressHeader.lytStressProgress.viewFocused.layoutParams =
            binding.lytStressHeader.lytStressProgress.viewFocused.layoutParams.apply {
                (this as LinearLayout.LayoutParams).weight =
                    calculateWeightPercent(20)
            }
        binding.lytStressHeader.lytStressProgress.viewStressed.layoutParams =
            binding.lytStressHeader.lytStressProgress.viewStressed.layoutParams.apply {
                (this as LinearLayout.LayoutParams).weight =
                    calculateWeightPercent(20)
            }

    }

    private fun calculateWeightPercent(progress: Int): Float {
        return (progress.toFloat() / 100).times(100)
    }

    private fun handleBannerView(stressValue: Int) {
        when (stressValue) {
            in 1..35 -> {
                binding.lytStressBanner.rootView.setBackgroundResource(R.drawable.ic_st_calm_cue_bg)
            }

            in 31..70 -> {
                binding.lytStressBanner.rootView.setBackgroundResource(R.drawable.ic_st_focus_cue_bg)
            }

            else -> {
                binding.lytStressBanner.rootView.setBackgroundResource(R.drawable.ic_st_stress_cue_bg)
            }
        }

    }

    override fun subscribeObservers() {

    }

    private fun handleMovementViews(isOpen: Boolean = false) {
        if (isOpen) {
            binding.viewOpen.gone()
            binding.ivOpen.gone()
            binding.tvCompareHeader.visible()
            binding.lytHighMovement.root.visible()
            binding.lytMediumMovement.root.visible()
            binding.lytLowMovement.root.visible()
            binding.lytNoMovement.root.visible()
            binding.viewClose.visible()
            binding.ivClose.visible()
        } else {
            binding.viewOpen.visible()
            binding.ivOpen.visible()
            binding.tvCompareHeader.gone()
            binding.lytHighMovement.root.gone()
            binding.lytMediumMovement.root.gone()
            binding.lytLowMovement.root.gone()
            binding.lytNoMovement.root.gone()
            binding.viewClose.gone()
            binding.ivClose.gone()
        }

        binding.lytHighMovement.tvHeader.text = getString(R.string.text_high_movement)
        binding.lytHighMovement.compareChart.setDrawData(
            movementViewModel.getCombinedMovementData(
                ArrayList(),
                false
            ) as ArrayList<Int>, 1, requireContext().getColor(R.color.white)
        )
        binding.lytMediumMovement.tvHeader.text = getString(R.string.text_medium_movement)
        binding.lytMediumMovement.compareChart.setDrawData(
            movementViewModel.getCombinedMovementData(
                ArrayList(),
                false
            ) as ArrayList<Int>, 1, requireContext().getColor(R.color.medium_movement_color)
        )
        binding.lytLowMovement.tvHeader.text = getString(R.string.text_low_movement)
        binding.lytLowMovement.compareChart.setDrawData(
            movementViewModel.getCombinedMovementData(
                ArrayList(),
                false
            ) as ArrayList<Int>, 1, requireContext().getColor(R.color.low_movement_color)
        )
        binding.lytNoMovement.tvHeader.text = getString(R.string.text_no_movement)
        binding.lytNoMovement.compareChart.setDrawData(
            movementViewModel.getCombinedMovementData(
                ArrayList(),
                false
            ) as ArrayList<Int>, 1, requireContext().getColor(R.color.no_movement_color)
        )
    }

    private fun initCombineChart() {
        val combineModel = StressCombineModel()
        combineModel.setHigh(70)
        combineModel.setMedium(30)
        val sections: MutableList<StressCombineModel.Section> =
            ArrayList()
        var section: StressCombineModel.Section = StressCombineModel.Section()
        section.setStart(0)
        section.setEnd(30)
        section.setColor(Color.parseColor("#C4A9F5"))
        section.setImageRes(R.drawable.icon_stress_sleep)
        sections.add(section)
        section = StressCombineModel.Section()
        section.setStart(40)
        section.setEnd(50)
        section.setColor(Color.parseColor("#00BCD4"))
        section.setImageRes(R.drawable.icon_stress_sport)
        sections.add(section)
        section = StressCombineModel.Section()
        section.setStart(60)
        section.setEnd(65)
        section.setColor(Color.parseColor("#00BCD4"))
        section.setImageRes(R.drawable.icon_stress_sport)
        sections.add(section)
        combineModel.setSections(sections)
        val items: MutableList<StressCombineModel.Item> = ArrayList<StressCombineModel.Item>()
        for (i in 0..95) {
            val item: StressCombineModel.Item = StressCombineModel.Item()
            item.setIndex(i)
            if (i > 40 && i < 60) {
                item.setValue(0)
                if (i == 50) {
                    item.setValue((Math.random() * 100).toInt())
                }
            } else {
                item.setValue((Math.random() * 100).toInt())
            }
            items.add(item)
        }

        combineModel.setItems(items)
        binding.lytStressMidGraph.updateData(combineModel)
        /* btnHigh.setOnClickListener {
             val highlights: MutableList<Int> =
                 ArrayList()
             highlights.add(10)
             highlights.add(11)
             highlights.add(12)
             highlights.add(13)
             highlights.add(43)
             highlights.add(44)
             highlights.add(50)
             highlights.add(74)
             highlights.add(75)
             combineLineChart.updateHighlight(highlights, Color.RED)
         }
         btnMed.setOnClickListener {
             val highlights: MutableList<Int> =
                 ArrayList()
             highlights.add(20)
             highlights.add(21)
             highlights.add(22)
             highlights.add(23)
             highlights.add(53)
             highlights.add(54)
             combineLineChart.updateHighlight(highlights, Color.YELLOW)
         }
         btnLow.setOnClickListener {
             val highlights: MutableList<Int> =
                 ArrayList()
             highlights.add(70)
             highlights.add(71)
             highlights.add(72)
             highlights.add(73)
             highlights.add(74)
             combineLineChart.updateHighlight(highlights, Color.parseColor("#FF009688"))
         }*/
    }

}