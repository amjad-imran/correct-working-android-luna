package com.oreo.ui.sleep2.sleepplanner.planner

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSleepPlannerBinding
import com.noisefit_commans.data.model.PlannerAlarmData
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.data.model.SleepPlannerData
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.ui.sleep2.sleepplanner.SADaysAdapter
import com.oreo.util.DateTimeUtil
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@AndroidEntryPoint
class SleepPlannerFragment :
    BaseFragment<FragmentSleepPlannerBinding>(FragmentSleepPlannerBinding::inflate) {
    private val viewModel: SleepPlannerViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()

        viewModel.getSleepPlanerDetails()
    }


    private fun setupUi() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_sleep_planner)
        binding.lytToolbar.view1.gone()

        binding.lytSetupGoal.tvTitle.text = getString(R.string.text_set_goal)


        binding.lytLegend1.tvTitle.text = getString(R.string.text_extra_sleep_need)
        binding.lytLegend1.ivColorBox.setBackgroundColor(android.graphics.Color.parseColor("#A477FF"))
        binding.lytLegend2.tvTitle.text = getString(R.string.text_avg_sleep_duration)
        binding.lytLegend2.ivColorBox.setBackgroundColor(android.graphics.Color.parseColor("#C5A8ED"))
    }


    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.lytToolbar.view1.setOnClickListener {
            //
        }

        binding.lytAddNewAlarm.root.setOnClickListener {
            navigate(
                R.id.setAlarmFragment,
                bundle = bundleOf("bed_time" to null, "wake_time" to null)
            )
        }

        binding.lytSetupGoal.root.setOnClickListener {
            setFragmentResultListener(SA_GOAL) { _, bundle ->
                val reload = bundle.getBoolean("reload")
                if (reload) {
                    viewModel.getSleepPlanerDetails()
                }
            }

            uiController.logAppEvent(
                MoEngageLunaAppEvents.sleep_planner_goal,
                hashMapOf("source" to "sleep")
            )

            navigate(R.id.dialogSaGoal)
        }
        binding.lytSetupAlarm.root.setOnClickListener {
            uiController.logAppEvent(
                MoEngageLunaAppEvents.sleep_planner_setup_alarm,
                hashMapOf("source" to "sleep")
            )
            navigate(
                R.id.setAlarmFragment,
                bundle = bundleOf("bed_time" to null, "wake_time" to null)
            )
        }
        binding.lytBreathExercise.root.setOnClickListener {
            uiController.logAppEvent(
                MoEngageLunaAppEvents.sleep_planner_exercise
            )
            navigate(R.id.fragmentBreathExercise)
        }


    }

    override fun subscribeObservers() {

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
        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.sleepPlannerCard.observe(this) {
            updateUiData(it)
        }
    }

    private fun updateUiData(data: SleepPlannerData?) {
        if (data == null) {
            navigateUpSafe()
            return
        }

        val bedTime = LocalTime.parse(
            data.planner?.bed_time ?: "10:00:00",
            DateTimeFormatter.ofPattern("HH:mm:ss")
        )
        val wakeTime = LocalTime.parse(
            data.planner?.wake_time ?: "06:00:00",
            DateTimeFormatter.ofPattern("HH:mm:ss")
        )


        binding.tvIdealTimeValue.text =
            "${bedTime.format(DateTimeFormatter.ofPattern("hh:mm a")).uppercase()} - ${
                wakeTime.format(DateTimeFormatter.ofPattern("hh:mm a")).uppercase()
            }"

        val goalText = viewModel.getGoalTextByKey(data.goal)

        if (goalText.isNotEmpty()) {
            binding.lytSetupGoal.tvGoalText.visible()
            binding.lytSetupGoal.ivMore.gone()
            binding.lytSetupGoal.tvGoalText.text = goalText
        } else {
            binding.lytSetupGoal.tvGoalText.gone()
            binding.lytSetupGoal.ivMore.visible()
        }

        /* binding.plannerClock.setData(
             LocalTime.of(22,0)*//*bedTime*//*,
            LocalTime.of(5,0)*//*wakeTime*//*,
            *//*(data.planner?.debt ?: 0) / 60*//*60
        )*/

        binding.plannerClock.setData(
            bedTime,
            wakeTime,
            (data.planner?.debt ?: 0) / 60
        )

        updateAlarmUi(data.alarms)

    }

    private fun updateAlarmUi(alarms: PlannerAlarmData?) {
        val allAlarmNull = viewModel.isAllAlarmNull(alarms)
        if (allAlarmNull) {
            binding.lytSetupAlarm.tvTitle.text = getString(R.string.text_setup_alarm)
            binding.lytSetupAlarm.root.visible()
            binding.lytAddNewAlarm.root.gone()
            binding.rvAlarms.gone()
        } else {
            binding.lytSetupAlarm.root.gone()
            binding.lytAddNewAlarm.root.visible()

            binding.rvAlarms.layoutManager = LinearLayoutManager(requireContext())

            val alarmsList = viewModel.generateAlarmData(alarms!!)

            binding.rvAlarms.visible()
            binding.rvAlarms.adapter = AlarmsAdapter(onAlarmClicked = { data ->
                navigate(
                    R.id.setAlarmFragment,
                    bundle = bundleOf("bed_time" to data.bedTime, "wake_time" to data.wakeTime)
                )
            }).apply {
                this.setData(alarmsList)
            }

        }
    }

}