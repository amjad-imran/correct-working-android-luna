package com.oreo.ui.femalehealth.cycletracker

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentFMHSkinTemperatureBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.data.model.PeriodTempChartModel
import com.oreo.data.model.femaleh.TempPeriodData
import com.oreo.ui.custom.female.ScrollListenerPeriodTemp
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class FMHSkinTemperatureFragment :
    BaseFragment<FragmentFMHSkinTemperatureBinding>(FragmentFMHSkinTemperatureBinding::inflate) {
    private val viewModel: SkinTemperatureViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lytToolbar.apply {
            tvTitle.text = getString(R.string.text_skin_temperature_variation)
            view1.visible()
            ivAddFriend.invisible()
            view1.loadImage(requireActivity(), R.drawable.ic_info_oreo)
        }
        initDefault()


        binding.lytSkinTemp.vGraph.setOnChartScrollChangedListener(graphListener)
    }

    private val graphListener = object : ScrollListenerPeriodTemp {
        override fun onPositionSelected(position: Int, chartModel: PeriodTempChartModel?) {
            updateTopUi(chartModel?.date, chartModel?.value)
        }

        override fun onScrolling(position: Int, chartModel: PeriodTempChartModel?) {

        }

    }

    fun updateTopUi(date: String?, value: Float?) {

        if (date.isNullOrEmpty()) return

        binding.tvDate.text = LocalDate.parse(date)
            .format(DateTimeFormatter.ofPattern("EEE, dd MMM"))
        binding.tvValue.text = if (value == null) {
            "-"
        } else if (value == 0.0f) {
            "0°F"
        } else {
            if (value > 0.0f) "+${value}" else "${value}°F"
        }
    }


    private fun updateGraph(temp: List<TempPeriodData>) {

        binding.lytSkinTemp.vGraph.visible()
        val moveToPos = -1

        val tempList = viewModel.getDummyTempList()
        val topGraphData = viewModel.getPrefixAndSuffixList(tempList)


        tempList.getOrNull(0)?.let {
            updateTopUi(it.date, it.temperature)
        }

        binding.lytSkinTemp.vGraph.updateData(
            topGraphData.second.first,
            topGraphData.second.third,
            topGraphData.second.second,
            topGraphData.third,
            moveToPos,
            topGraphData.first,
        )
    }

    private fun initDefault() {
        binding.lytSkinTemp.lytLegendView.apply {

            lytPeriod.tvText.text = getText(R.string.text_period)
            lytPeriod.viewColor.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    binding.tvDate.context, R.color.color_period
                )
            )
            lytOvulation.tvText.text = getText(R.string.text_ovulation)
            lytOvulation.viewColor.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    binding.tvDate.context, R.color.color_ovulation
                )
            )
            lytFollicular.tvText.text = getText(R.string.text_follicular)
            lytFollicular.viewColor.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    binding.tvDate.context, R.color.color_follicular
                )
            )
            lytLuteal.tvText.text = getText(R.string.text_luteal)
            lytLuteal.viewColor.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    binding.tvDate.context, R.color.color_luteal
                )
            )

        }
    }

    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    private fun updateUI() {


    }

    override fun subscribeObservers() {
        viewModel.cycleHistoryData.observe(this) {
            viewModel.getTempData(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        }

        viewModel.tempData.observe(this) {

            binding.tvDescription.text = it.nudge?.message
            updateGraph(it.temp ?: ArrayList())
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar1.root.visible()
            } else {
                binding.progressBar1.root.gone()
            }
        }

    }


}