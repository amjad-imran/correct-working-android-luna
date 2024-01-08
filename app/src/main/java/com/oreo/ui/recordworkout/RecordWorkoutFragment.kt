package com.oreo.ui.recordworkout

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentRecordWorkoutBinding
import com.noisefit.ui.common.bottomSheet.ALERT_REQUEST_KEY
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecordWorkoutFragment :
    BaseFragment<FragmentRecordWorkoutBinding>(FragmentRecordWorkoutBinding::inflate) {

    val navArgs: RecordWorkoutFragmentArgs by navArgs()
    val viewModel: RecordWorkoutViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        navArgs.workout.let {
            viewModel.workout = it
            binding.tvWorkoutTitle.text = it.activityType
            binding.ivWorkoutImage.loadImage(binding.ivWorkoutImage.context, it.iconUrl)
        }

        binding.btnEndWorkout.isEnabled = false

        startTimer()
    }

    private fun startTimer() {

    }


    private fun startWorkout() {
        viewModel.sportStartTime = System.currentTimeMillis() / 1000

        val sportId = viewModel.workout?.ringId ?: -1
        viewModel.currentWorkoutState = 1

        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.StartWorkout(
                sportId,
                viewModel.sportStartTime
            )
        )

        binding.btnStartWorkout.gone()
        binding.btnPauseResume.apply {
            this.text = getString(R.string.pause)
            this.visible()
        }
        binding.btnEndWorkout.isEnabled = true
        viewModel.starTimer()
    }

    private fun pauseWorkout() {
        val sportId = viewModel.workout?.ringId ?: -1

        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.UpdateOngoingWorkout(
                sportId,
                viewModel.sportStartTime,
                2
            )
        )
        viewModel.currentWorkoutState = 2

        binding.btnPauseResume.text = getString(R.string.resume)
        viewModel.pauseTimer()

    }

    private fun resumeWorkout() {
        val sportId = viewModel.workout?.ringId ?: -1

        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.UpdateOngoingWorkout(
                sportId,
                viewModel.sportStartTime,
                3
            )
        )
        viewModel.currentWorkoutState = 3
        binding.btnPauseResume.text = getString(R.string.pause)
        viewModel.resumeTimer()
    }

    private fun stopWorkout() {

        val sportId = viewModel.workout?.ringId ?: -1

        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.UpdateOngoingWorkout(
                sportId,
                viewModel.sportStartTime,
                4
            )
        )
        viewModel.currentWorkoutState = 4
        navigateUpSafe()
        viewModel.stopTimer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopTimer()
    }

    override fun initListener() {

        binding.ivWorkoutImage.setOnClickListener {
            viewModel.sessionManager.sendUpdateQueryAction(UpdateDeviceAction.CheckOngoingWorkout())
        }

        binding.btnStartWorkout.setOnClickListener {

            //TODO check if device is connected
            if (!viewModel.isDeviceConnected()) {
                return@setOnClickListener
            }

            startWorkout()

        }

        binding.btnPauseResume.setOnClickListener {
            if (viewModel.currentWorkoutState == 1 || viewModel.currentWorkoutState == 3) {
                pauseWorkout()
            } else if (viewModel.currentWorkoutState == 2) {
                resumeWorkout()
            }
        }

        binding.btnEndWorkout.setOnClickListener {

            if (!viewModel.isDeviceConnected()) {
                return@setOnClickListener
            }

            setFragmentResultListener(
                END_WORKOUT_KEY
            ) { _, bundle ->
                val allow = bundle.getBoolean("allow")

                if (allow) {
                    stopWorkout()
                }
            }
            navigate(R.id.bottomSheetEndWorkout)
        }

        binding.ivCross.setOnClickListener {
            if (viewModel.currentWorkoutState == 0) {
                navigateUpSafe()
            } else {
                setFragmentResultListener(
                    END_WORKOUT_KEY
                ) { _, bundle ->
                    val allow = bundle.getBoolean("allow")

                    if (allow) {
                        stopWorkout()
                    }
                }
            }
            navigate(R.id.bottomSheetEndWorkout)
        }

        viewModel.sessionManager.connectStateRing.observe(this) { connectedState ->
            when (connectedState) {
                is ConnectState.ConnectFailed -> {
                    setConnectingState()
                }

                is ConnectState.Connecting -> {
                    setConnectingState()
                }

                is ConnectState.ConnectSuccess -> {
                    setStateConnected()
                }

                is ConnectState.UnPaired -> {
                    //TODO remove workout temp data
                    navigateUpSafe()
                }

                else -> {}
            }
        }

    }

    private fun setStateConnected() {
        binding.lytRingConnecting.root.gone()
        binding.groupRingStatus.visible()
        val batteryPercentage = viewModel.watchDataStore.getBatteryPercentRing()
        binding.batteryStatus.progress = batteryPercentage


        if (batteryPercentage <= 20) {
            binding.oreoStatus.loadImage(
                requireContext(),
                R.drawable.ic_ring_low_battery
            )
            binding.batteryStatus.setIndicatorColor(resources.getColor(R.color.color_error))
        } else {
            binding.oreoStatus.setBackgroundResource(R.drawable.back_modal_new_round)
            binding.batteryStatus.setIndicatorColor(resources.getColor(R.color.white))
        }
    }

    private fun setConnectingState() {
        binding.lytRingConnecting.root.visible()
        binding.groupRingStatus.gone()
    }

    override fun subscribeObservers() {

        viewModel.displayTimer.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.tvTimer.text = "00:00"
            } else {
                binding.tvTimer.text = it
            }
        }


        viewModel.sessionManager.updateDeviceCallback.observe(this) {
            it.getContent()?.let {

                when (it) {
                    is UpdateDeviceDataCallback.WorkoutStartState -> {
                        context.showShortToast("Workout started : ${it.success}")
                    }

                    /*is UpdateDeviceDataCallback.OngoingWorkoutData -> {
                        setWorkoutData(it.data)
                    }*/

                    is UpdateDeviceDataCallback.WorkoutStopped -> {
                        context.showShortToast("Workout Stopped : ${it.success}")
                    }

                    is UpdateDeviceDataCallback.WorkoutPaused -> {
                        context.showShortToast("Workout Paused : ${it.success}")
                    }

                    is UpdateDeviceDataCallback.WorkoutResumed -> {
                        context.showShortToast("Workout Resumed : ${it.success}")
                    }


                    else -> {}
                }

            }

        }

    }
}