package com.oreo.ui.recordworkout

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentRecordWorkoutBinding
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
    }

    override fun initListener() {

        binding.btnStartWorkout.setOnClickListener {

            //TODO check if device is connected


            viewModel.sportStartTime = System.currentTimeMillis() / 1000

            val sportId = viewModel.workout?.ringId ?: -1

            viewModel.sessionManager.sendUpdateQueryAction(
                UpdateDeviceAction.StartWorkout(
                    sportId,
                    viewModel.sportStartTime
                )
            )

        }

        binding.btnEndWorkout.setOnClickListener {

            val sportId = viewModel.workout?.ringId ?: -1

            viewModel.sessionManager.sendUpdateQueryAction(
                UpdateDeviceAction.UpdateOngoingWorkout(
                    sportId,
                    viewModel.sportStartTime,
                    4
                )
            )

            /*sessionManager.sendUpdateQueryAction(
                UpdateDeviceAction.UpdateOngoingWorkout(
                    sportType,
                    sportStartTime,
                    3
                )
            )



            sessionManager.sendUpdateQueryAction(
                UpdateDeviceAction.UpdateOngoingWorkout(
                    sportType,
                    sportStartTime,
                    2
                )
            )*/
        }

        binding.ivCross.setOnClickListener {
            navigateUpSafe()
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