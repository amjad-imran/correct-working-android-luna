package com.oreo.ui.stress

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOStressDataMovementBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OStressDataMovementFragment :
    BaseFragment<FragmentOStressDataMovementBinding>(FragmentOStressDataMovementBinding::inflate) {
    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private val viewModel: OStressDetailViewModel by viewModels()
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


        val date = arguments?.getString(ARGS_DATE)
        viewModel.date = date

        handleMovementViews()

    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        viewModel.date?.let {
            mainViewModel.getStressData(it)?.let { dayData ->

                initCombineChart(it)
                setMovementData(dayData.activity?.daytimeMovement?.movement)
                handleStressProgressView()
                setNudge(60)

            }
        }
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

    private fun setNudge(stressValue: Int) {
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

    private fun setMovementData(movementList: List<Int>?) {

        val combinedData = viewModel.getCombinedMovementData(movementList, true)

        binding.lytHighMovement.tvHeader.text = getString(R.string.text_high_movement)
        binding.lytHighMovement.compareChart.setDrawData(
            combinedData, 3, requireContext().getColor(R.color.white)
        )
        binding.lytMediumMovement.tvHeader.text = getString(R.string.text_medium_movement)
        binding.lytMediumMovement.compareChart.setDrawData(
            combinedData, 2, requireContext().getColor(R.color.medium_movement_color)
        )
        binding.lytLowMovement.tvHeader.text = getString(R.string.text_low_movement)
        binding.lytLowMovement.compareChart.setDrawData(
            combinedData, 1, requireContext().getColor(R.color.low_movement_color)
        )
        binding.lytNoMovement.tvHeader.text = getString(R.string.text_no_movement)
        binding.lytNoMovement.compareChart.setDrawData(
            combinedData, 0, requireContext().getColor(R.color.no_movement_color)
        )
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
    }

    private fun initCombineChart(date: String) {
        binding.lytStressMidGraph.updateData(mainViewModel.getStressCombinedData(date))
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