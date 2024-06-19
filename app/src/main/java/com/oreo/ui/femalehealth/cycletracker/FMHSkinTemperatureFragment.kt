package com.oreo.ui.femalehealth.cycletracker

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentFMHSkinTemperatureBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.PeriodTempChartModel
import com.oreo.data.model.femaleh.FemaleTempResponse
import com.oreo.data.model.femaleh.TempPeriodData
import com.oreo.ui.custom.female.ScrollListenerPeriodTemp
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class FMHSkinTemperatureFragment :
    BaseFragment<FragmentFMHSkinTemperatureBinding>(FragmentFMHSkinTemperatureBinding::inflate) {
    private val viewModel: SkinTemperatureViewModel by viewModels()
    private val args: FMHSkinTemperatureFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.selectedDate = args.selectedDate

        binding.lytToolbar.apply {
            tvTitle.text = getString(R.string.text_ovulation_temperature_variation)
            view1.invisible()
            ivAddFriend.invisible()
            view1.loadImage(requireActivity(), R.drawable.ic_info_oreo)
        }
        initDefault()


        binding.lytSkinTemp.vGraph.setOnChartScrollChangedListener(graphListener)
    }

    private fun setPredictionUI(data: FemaleTempResponse) {
        if (data.pendingNights == null) {
            binding.lytPrediction.apply {
                tvMoreNight.gone()
                divider1.root.gone()
                ivInfo.gone()
            }
        } else {
            binding.lytPrediction.apply {
                tvMoreNight.visible()
                divider1.root.visible()
                ivInfo.visible()
                tvMoreNight.text ="Temperature data for upcoming 3 cycles is required"
                    //"Data for ${data.pendingNights} more nights is required"
            }
        }

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
        val moveToPos = viewModel.getSelectedPosition(temp)

        //val tempList = viewModel.getDummyTempList()
        val topGraphData = viewModel.getPrefixAndSuffixList(temp)


        temp.getOrNull(0)?.let {
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
            lytOvulation.tvText.text = getText(R.string.text_fertile_days)
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
        binding.lytPrediction.ivInfo.setOnClickListener {
            navigate(
                R.id.dialogCtOvulationInfo, Bundle().apply {
                    this.putString("launchMode", "Ovulation Graph Details")
                }
            )
        }
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
        viewModel.cycleHistoryData.observe(this) {
            viewModel.getTempData(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))

            val days = viewModel.getNextPeriodDays()
            if (days == null) {
                binding.lytPrediction.tvValue.text = "-"
                binding.lytPrediction.tvUnit.gone()
            } else {
                binding.lytPrediction.tvValue.text = days
                binding.lytPrediction.tvUnit.visible()
            }

        }

        viewModel.tempData.observe(this) {
            if(it.nudge == null){
                binding.tvDescription.gone()
            }else{
                binding.tvDescription.visible()
            }
            binding.tvDescription.text = it.nudge?.message
            updateGraph(it.temp ?: ArrayList())
            setPredictionUI(it)
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