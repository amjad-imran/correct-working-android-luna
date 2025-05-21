package com.oreo.ui.heartrate

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import com.google.android.material.chip.Chip
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentIrregularityEventsOHeartRateDataBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MiscUtil
import com.oreo.data.model.IrregularEventsChipModel
import com.oreo.data.model.IrregularEventsChipsListModel
import com.oreo.data.model.health.Nudges
import com.oreo.ui.activity.OreoActivityBannerFragment
import com.oreo.ui.activity.SLEEP_ACTIVITY_BANNER
import com.oreo.ui.readiness.NudgeBannerListener
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

const val HEART_RATE_IRREGULARITY_EVENTS_BANNER = "HEART_RATE_IRREGULARITY_EVENTS_BANNER"

@AndroidEntryPoint
class IrregularityEventsOHeartRateDataFragment :
    BaseFragment<FragmentIrregularityEventsOHeartRateDataBinding>(
        FragmentIrregularityEventsOHeartRateDataBinding::inflate
    ) {

    private var data: IrregularEventsChipsListModel? = null
    private var listener: IrregularityEventsBannerListener? = null

    private val selectedChips = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            data = it.getParcelable(HEART_RATE_IRREGULARITY_EVENTS_BANNER)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        data?.let {
            setUi(it)
        }
    }

    private fun minutesToAmPm(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        val time = LocalTime.of(hours, mins)
        val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
        return time.format(formatter)
    }

    private fun setUi(data: IrregularEventsChipsListModel) {
        binding.tvMessage.text =
            getString(R.string.text_hr_spiked_by_value, data.alert.spikePercent)
        binding.tvTime.text = minutesToAmPm(data.alert.minutes)

        setIrregularityEventsChips(data.irregularEventsChipsList)
    }

    override fun initListener() {
        binding.btnSubmit.setOnClickListener {
            data?.let {
                if (selectedChips.isEmpty()) return@setOnClickListener

                listener?.onSubmitBtnClicked(it, selectedChips)
            }
        }

        binding.imgChatEtx.setOnClickListener {
            data?.let {

                if (binding.chatEtx.text.toString().isEmpty()) return@setOnClickListener

                listener?.onSubmitBtnClicked(
                    it,
                    selectedChips,
                    binding.chatEtx.text.toString()
                )
            }
        }

        binding.btnClose.setOnClickListener {
            data?.let {
                listener?.onCrossClicked(it)
            }
        }
    }

    override fun subscribeObservers() {

    }

    fun setClickListener(listener: IrregularityEventsBannerListener) {
        this.listener = listener
    }

    companion object {
        @JvmStatic
        fun newInstance(data: IrregularEventsChipsListModel) =
            IrregularityEventsOHeartRateDataFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(HEART_RATE_IRREGULARITY_EVENTS_BANNER, data)
                }
            }
    }

    fun setIrregularityEventsChips(category: List<IrregularEventsChipModel>?) {
        binding.chipsPrograms.removeAllViews()
        if (category != null) {
            for (item in category) {
                val mChip: Chip =
                    layoutInflater.inflate(R.layout.item_chip_feedback, null, false) as Chip
                mChip.text = item.displayName
                mChip.tag = item.key

                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),  // Checked state
                    intArrayOf(-android.R.attr.state_checked)   // Unchecked state
                )

                // Set your colors here (replace with your desired colors)
                val colors = intArrayOf(
                    "#7C404E".toColorInt(),  // Checked color
                    "#0affffff".toColorInt()   // Unchecked color
                )

                val colorStateList = ColorStateList(states, colors)

                // Apply the color state list to the chip
                mChip.chipBackgroundColor = colorStateList

                /*val paddingDp = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 4F, resources.displayMetrics
                )
                mChip.setPadding(paddingDp.toInt(), 0, paddingDp.toInt(), 0)*/
                mChip.setOnCheckedChangeListener { compoundButton, isChecked ->
//                    mViewModel.updateChipSelection(item.key, isChecked)
                    if (mChip.tag.toString().equals("others")) {
                        if (isChecked) {
                            binding.chatEtx.visible()
                            binding.imgChatEtx.visible()
                            binding.btnSubmit.gone()
                        } else {
                            binding.chatEtx.gone()
                            binding.imgChatEtx.gone()
                            binding.btnSubmit.visible()
                        }
                    }
                    if (isChecked) {
                        LOGS.d("Checked Chips ${mChip.text}")
                        val name = MiscUtil.addUnderscore(mChip.text.toString())
                        selectedChips.add(mChip.tag.toString())
                    } else {
                        selectedChips.remove(mChip.tag.toString())
                    }
                }
                binding.chipsPrograms.addView(mChip)

            }
        }
    }

}

interface IrregularityEventsBannerListener {
    fun onSubmitBtnClicked(
        data: IrregularEventsChipsListModel,
        selectedChips: List<String>,
        other: String? = null
    )

    fun onCrossClicked(data: IrregularEventsChipsListModel)
}
