package com.oreo.ui.stress

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOStressDataMovementBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.model.Stress
import com.oreo.data.model.StressNudge
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OStressDataMovementFragment :
    BaseFragment<FragmentOStressDataMovementBinding>(FragmentOStressDataMovementBinding::inflate) {
    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private val sharedViewModel: StressDetailSharedViewModel by activityViewModels()
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

                viewModel.dayTimeMovement = dayData.activity?.daytimeMovement?.movement
                viewModel.isSelectedMode = false

                initCombineChart(dayData)
                setMovementData(viewModel.dayTimeMovement, viewModel.isSelectedMode, -1)
                handleStressProgressView(dayData.stress)
                viewModel.prepareStressActivityData(dayData)

                setNudge(dayData.stress?.nudges)

            }
        }
    }

    override fun initListener() {
        //sharedViewModel.setSelectedType(StressType.NO_DATA)


        binding.ivOpen.setOnClickListener {
            handleMovementViews(true)
        }
        binding.ivClose.setOnClickListener {
            handleMovementViews(false)
        }
        /*binding.lytHighMovement.root.setOnClickListener {
            //todo open activity bottomsheet for testing
            viewModel.stressActivityData?.toTypedArray()?.let { it1 ->
                navigate(
                    OStressDetailsFragmentDirections.actionStressDetailFragmentToBottomSheetStressActivity(
                        it1
                    )
                )
            }
        }*/

        binding.lytHighMovement.root.setOnClickListener {
            handleMovementClick(3)
        }
        binding.lytMediumMovement.root.setOnClickListener {
            handleMovementClick(2)
        }
        binding.lytLowMovement.root.setOnClickListener {
            handleMovementClick(1)
        }
        binding.lytNoMovement.root.setOnClickListener {
            handleMovementClick(0)
        }
    }

    private fun handleMovementClick(type: Int) {
        val lastSelected = viewModel.lastSelectedType

        if (lastSelected == type) {
            setMovementData(viewModel.dayTimeMovement, false, -1)
            viewModel.isSelectedMode = false
            viewModel.lastSelectedType = -1
        } else {
            setMovementData(viewModel.dayTimeMovement, true, type)
            viewModel.isSelectedMode = true
            viewModel.lastSelectedType = type
        }
    }

    private fun handleStressProgressView(stress: Stress?) {

        val (calm, focused, stressed) = viewModel.getStressMinutes(stress)
        val total = calm + focused + stressed


        binding.lytStressHeader.apply {
            val (hourCalm, minuteCalm) = ApplicationUtils.getFormattedSleepDuration(
                calm
            )
            lytCalm.lytHrMn.tvHour.text = "$hourCalm"
            lytCalm.lytHrMn.tvMinute.text = "$minuteCalm"
            lytCalm.tvCalm.setTextColor(Color.parseColor("#3fe8b5"))
            lytCalm.tvCalm.text = getString(R.string.text_calm)


            val (hourFocused, minuteFocused) = ApplicationUtils.getFormattedSleepDuration(
                focused
            )
            lytFocussed.lytHrMn.tvHour.text = "$hourFocused"
            lytFocussed.lytHrMn.tvMinute.text = "$minuteFocused"
            lytFocussed.tvCalm.setTextColor(Color.parseColor("#ffed91"))
            lytFocussed.tvCalm.text = getString(R.string.text_focussed)


            val (hourStressed, minuteStressed) = ApplicationUtils.getFormattedSleepDuration(
                stressed
            )
            lytStressed.lytHrMn.tvHour.text = "$hourStressed"
            lytStressed.lytHrMn.tvMinute.text = "$minuteStressed"
            lytStressed.tvCalm.setTextColor(Color.parseColor("#ffad60"))
            lytStressed.tvCalm.text = getString(R.string.text_stressed)

        }

        binding.lytStressHeader.lytStressProgress.apply {

            viewCalm.layoutParams =
                viewCalm.layoutParams.apply {
                    (this as LinearLayout.LayoutParams).weight =
                        calculateWeightPercent(calm, total)
                }

            viewFocused.layoutParams =
                viewFocused.layoutParams.apply {
                    if (calm == 0 || focused == 0) {
                        (this as LinearLayout.LayoutParams).marginStart = 0
                    } else {
                        (this as LinearLayout.LayoutParams).marginStart =
                            viewModel.screenUtils.dpToPx(2, viewFocused.context).toInt()
                    }
                    (this as LinearLayout.LayoutParams).weight =
                        calculateWeightPercent(focused, total)
                }
            viewStressed.layoutParams =
                viewStressed.layoutParams.apply {
                    if (focused == 0 || stressed == 0) {
                        (this as LinearLayout.LayoutParams).marginStart = 0
                    } else {
                        (this as LinearLayout.LayoutParams).marginStart =
                            viewModel.screenUtils.dpToPx(2, viewFocused.context).toInt()
                    }

                    (this as LinearLayout.LayoutParams).weight =
                        calculateWeightPercent(stressed, total)
                }
        }

    }

    private fun calculateWeightPercent(progress: Int, total: Int): Float {
        return (progress.toFloat() / total).times(100)
    }

    private fun setNudge(nudges: List<StressNudge>?) {
        if (nudges.isNullOrEmpty()) {
            binding.lytStressBanner.rootView.gone()
            return
        }
        binding.lytStressBanner.rootView.visible()

        when (nudges.first().value) {
            in 1..34 -> {
                binding.lytStressBanner.rootView.setBackgroundResource(R.drawable.ic_st_calm_cue_bg)
            }

            in 35..69 -> {
                binding.lytStressBanner.rootView.setBackgroundResource(R.drawable.ic_st_focus_cue_bg)
            }

            else -> {
                binding.lytStressBanner.rootView.setBackgroundResource(R.drawable.ic_st_stress_cue_bg)
            }
        }

        binding.

    }

    override fun subscribeObservers() {

    }

    private fun setMovementData(
        movementList: List<Int>?,
        isSelectedMode: Boolean,
        selectedType: Int
    ) {

        val combinedData = viewModel.getCombinedMovementData(movementList, true)

        binding.lytHighMovement.apply {
            tvHeader.text = getString(R.string.text_high_movement)
            compareChart.updateInitData(3, requireContext().getColor(R.color.white))
            compareChart.setDrawData(
                combinedData, isSelectedMode
            )

            if (selectedType == 3) {
                this.lytMain.setBackgroundResource(R.drawable.back_modal_new_10)
                this.root.alpha = 1.0f
            } else {
                this.lytMain.setBackgroundResource(R.drawable.back_modal_new_10_normal)
                this.root.alpha = 0.5f
            }
        }

        binding.lytMediumMovement.apply {
            binding.lytMediumMovement.tvHeader.text = getString(R.string.text_medium_movement)
            compareChart.updateInitData(2, requireContext().getColor(R.color.medium_movement_color))
            binding.lytMediumMovement.compareChart.setDrawData(
                combinedData, isSelectedMode
            )

            if (selectedType == 2) {
                this.lytMain.setBackgroundResource(R.drawable.back_modal_new_10)
                this.root.alpha = 1.0f
            } else {
                this.lytMain.setBackgroundResource(R.drawable.back_modal_new_10_normal)
                this.root.alpha = 0.5f
            }
        }


        binding.lytLowMovement.apply {
            binding.lytLowMovement.tvHeader.text = getString(R.string.text_low_movement)
            compareChart.updateInitData(1, requireContext().getColor(R.color.low_movement_color))
            binding.lytLowMovement.compareChart.setDrawData(
                combinedData, isSelectedMode
            )

            if (selectedType == 1) {
                this.lytMain.setBackgroundResource(R.drawable.back_modal_new_10)
                this.root.alpha = 1.0f
            } else {
                this.lytMain.setBackgroundResource(R.drawable.back_modal_new_10_normal)
                this.root.alpha = 0.5f
            }
        }

        binding.lytNoMovement.apply {
            binding.lytNoMovement.tvHeader.text = getString(R.string.text_no_movement)
            compareChart.updateInitData(0, requireContext().getColor(R.color.no_movement_color))
            binding.lytNoMovement.compareChart.setDrawData(
                combinedData, isSelectedMode
            )

            if (selectedType == 0) {
                this.lytMain.setBackgroundResource(R.drawable.back_modal_new_10)
                this.root.alpha = 1.0f
            } else {
                this.lytMain.setBackgroundResource(R.drawable.back_modal_new_10_normal)
                this.root.alpha = 0.5f
            }
        }

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

    private fun initCombineChart(dayData: ServerUserHealthData) {
        binding.lytStressMidGraph.updateData(viewModel.getStressCombinedData(dayData))
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