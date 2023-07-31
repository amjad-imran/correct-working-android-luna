package com.noisefit.ui.common.bottomSheet

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentRepeatBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint

const val REPEAT_REQUEST_KEY = "REPEAT_REQUEST_KEY"
@AndroidEntryPoint
class RepeatBottomSheet :
    BaseBottomSheetWithTransparent<FragmentRepeatBottomSheetBinding>(FragmentRepeatBottomSheetBinding::inflate) {

    private var selectedWeekArray =
        arrayListOf(true, true, true, true, true, true, true)
    private val weekViewArray: List<TextView> by lazy {
        arrayListOf(
            binding.layoutWeek.bMon,
            binding.layoutWeek.bTues,
            binding.layoutWeek.bWed,
            binding.layoutWeek.bThurs,
            binding.layoutWeek.bFri,
            binding.layoutWeek.bSat,
            binding.layoutWeek.bSun
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val repeatDays = RepeatBottomSheetArgs.fromBundle(it).repeat
            repeatDays!!.forEachIndexed { index, i ->
                if (!i) {
                    selectedWeekArray[index] = false
                }

            }
        }
        setWeekArray()
    }


    override fun initListener() {

        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }

        binding.btnAllow.setOnClickListener {
            setFragmentResult(
                REPEAT_REQUEST_KEY,
                bundleOf("repeat" to selectedWeekArray)
            )
            navigateUpSafe()
        }

        binding.layoutWeek.bMon.setOnClickListener(onWeekSelectionListener)
        binding.layoutWeek.bTues.setOnClickListener(onWeekSelectionListener)
        binding.layoutWeek.bWed.setOnClickListener(onWeekSelectionListener)
        binding.layoutWeek.bThurs.setOnClickListener(onWeekSelectionListener)
        binding.layoutWeek.bFri.setOnClickListener(onWeekSelectionListener)
        binding.layoutWeek.bSat.setOnClickListener(onWeekSelectionListener)
        binding.layoutWeek.bSun.setOnClickListener(onWeekSelectionListener)
    }

    override fun subscribeObservers() {

    }

    private fun setWeekArray() {
        selectedWeekArray.forEachIndexed { index, selected ->
            try {
                if (selected) {
                    weekViewArray[index].setBackgroundResource(R.drawable.back_week_selected)
                } else {
                    weekViewArray[index].setBackgroundResource(R.drawable.back_week_un_selected)
                }
            } catch (exp: Exception) {
                exp.printStackTrace()
            }
        }
    }


    private val onWeekSelectionListener = View.OnClickListener { view ->
        val clickedView = view as TextView
        val tag = clickedView.tag.toString().toIntOrNull() ?: return@OnClickListener

        val lastState = selectedWeekArray[tag]
        if (!lastState) {
            clickedView.setBackgroundResource(R.drawable.back_week_selected)
            selectedWeekArray[tag] = true
        } else {
            clickedView.setBackgroundResource(R.drawable.back_week_un_selected)
            selectedWeekArray[tag] = false
        }
    }

}