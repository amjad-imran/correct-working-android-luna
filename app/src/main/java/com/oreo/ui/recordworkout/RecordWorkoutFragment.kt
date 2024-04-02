package com.oreo.ui.recordworkout

import android.animation.Animator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentRecordWorkoutBinding
import com.noisefit.ui.common.bottomSheet.DELETE_REQ_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.WORKOUT_STOP_KEY
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.data.UserActivityAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.playAnimation
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
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
            binding.tvWorkoutTitle.text = it.getFormattedActivityName()
            binding.ivWorkoutImage.loadImage(binding.ivWorkoutImage.context, it.iconUrl)
            viewModel.sportStartTime =
                viewModel.ringDataStore.getOngoingRecordWorkout()?.first ?: 0L
        }
        binding.btnEndWorkout.isEnabled = false

        navArgs.onGoingWorkout?.let {
            viewModel.workoutDuration = it.duration.toLong()
            if (it.sportStatus == 1 || it.sportStatus == 3) {
                startWorkout()
            } else if (it.sportStatus == 2) {
                pauseWorkout()
                viewModel.updateTimer()
            }
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)

    }

    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onCrossClicked()
            }
        }


    private fun startWorkout() {
        viewModel.currentWorkoutState = 1
        binding.btnStartWorkout.gone()
        binding.btnPauseResume.apply {
            this.text = getString(R.string.pause)
            this.visible()
        }
        binding.btnEndWorkout.isEnabled = true
        viewModel.starTimer()

        viewModel.saveOngoingRecordWorkout()
    }

    private fun pauseWorkout() {
        viewModel.currentWorkoutState = 2
        binding.btnStartWorkout.gone()
        binding.btnPauseResume.visible()
        binding.btnPauseResume.text = getString(R.string.resume)
        viewModel.pauseTimer()
        binding.btnEndWorkout.isEnabled = true
    }

    private fun resumeWorkout() {
        viewModel.currentWorkoutState = 3
        binding.btnStartWorkout.gone()
        binding.btnPauseResume.visible()
        binding.btnPauseResume.text = getString(R.string.pause)
        viewModel.resumeTimer()
        binding.btnEndWorkout.isEnabled = true
    }

    private fun stopWorkout() {
        viewModel.deleteOngoingRecordWorkout()
        viewModel.currentWorkoutState = 4
        viewModel.stopTimer()

        if (viewModel.markedDeleted) {
            navigateUpSafe()
        } else {
            binding.progressBar.root.visible()
            viewModel.sessionManager.lastOngoingWorkoutTimestamp = viewModel.sportStartTime
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopTimer()
    }

    private fun onCrossClicked() {
        if (viewModel.currentWorkoutState == 0) {
            navigateUpSafe()
        } else {

            if (viewModel.workoutDuration < 60) {

                setFragmentResultListener(
                    END_WORKOUT_KEY_SHORT
                ) { _, bundle ->
                    val end = bundle.getBoolean("end")

                    if (end) {
                        binding.progressBar.root.visible()
                        val sportId = viewModel.workout?.ringId ?: -1

                        viewModel.sessionManager.sendUpdateQueryAction(
                            UpdateDeviceAction.UpdateOngoingWorkout(
                                sportId,
                                viewModel.getCurrentTimeStamp(),
                                4
                            )
                        )
                        viewModel.markedDeleted = true
                        viewModel.markForDelete(viewModel.sportStartTime)
                    }
                }
                navigate(R.id.bottomSheetEndWorkoutShort)
                return
            }





            setFragmentResultListener(
                END_WORKOUT_KEY
            ) { _, bundle ->
                val allow = bundle.getBoolean("allow")
                val delete = bundle.getBoolean("delete")

                if (allow || delete) {
                    binding.progressBar.root.visible()
                    val sportId = viewModel.workout?.ringId ?: -1

                    viewModel.sessionManager.sendUpdateQueryAction(
                        UpdateDeviceAction.UpdateOngoingWorkout(
                            sportId,
                            viewModel.getCurrentTimeStamp(),
                            4
                        )
                    )

                    if (delete) {
                        viewModel.markedDeleted = true
                        viewModel.markForDelete(viewModel.sportStartTime)
                    }
                }
            }
            navigate(R.id.bottomSheetEndWorkout)
        }
    }


    private fun startWorkoutAnim() {
        binding.lottieAnim.visible()
        binding.lottieAnim.setAnimation(R.raw.anim_3_2_1_go)
        binding.lottieAnim.playAnimation()
        binding.lottieAnim.repeatCount = 0

        binding.lottieAnim.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {

            }

            override fun onAnimationEnd(p0: Animator) {
                binding.lottieAnim.gone()
                binding.btnStartWorkout.visible()
                sendStartWorkoutCommand()
            }

            override fun onAnimationCancel(p0: Animator) {

            }

            override fun onAnimationRepeat(p0: Animator) {

            }
        })
    }

    fun sendStartWorkoutCommand() {
        binding.progressBar.root.visible()
        viewModel.sportStartTime = viewModel.getCurrentTimeStamp()
        val sportId = viewModel.workout?.ringId ?: -1

        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.StartWorkout(
                sportId,
                viewModel.sportStartTime
            )
        )
    }

    override fun initListener() {

        binding.ivWorkoutImage.setOnClickListener {
            viewModel.sessionManager.sendUpdateQueryAction(UpdateDeviceAction.CheckOngoingWorkout())
        }

        binding.btnStartWorkout.setOnClickListener {

            if (!viewModel.isDeviceConnected()) {
                return@setOnClickListener
            }

            binding.btnStartWorkout.gone()


            startWorkoutAnim()

        }

        binding.btnPauseResume.setOnClickListener {
            binding.progressBar.root.visible()

            if (viewModel.currentWorkoutState == 1 || viewModel.currentWorkoutState == 3) {
                val sportId = viewModel.workout?.ringId ?: -1

                viewModel.sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.UpdateOngoingWorkout(
                        sportId,
                        viewModel.getCurrentTimeStamp(),
                        2
                    )
                )
            } else if (viewModel.currentWorkoutState == 2) {
                val sportId = viewModel.workout?.ringId ?: -1

                viewModel.sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.UpdateOngoingWorkout(
                        sportId,
                        viewModel.getCurrentTimeStamp(),
                        3
                    )
                )
            }
        }

        binding.btnEndWorkout.setOnClickListener {

            if (!viewModel.isDeviceConnected()) {
                return@setOnClickListener
            }

            onCrossClicked()
        }

        binding.ivCross.setOnClickListener {
            onCrossClicked()
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
                    navigateUpSafe()
                }

                else -> {}
            }
        }

    }

    private fun setStateConnected() {
        binding.lytRingConnecting.root.gone()
        binding.imageConnecting.gone()
        binding.groupRingStatus.visible()
        binding.ivCross.visible()
        val batteryPercentage = viewModel.watchDataStore.getBatteryPercentRing()
        binding.batteryStatus.progress = batteryPercentage


        if (batteryPercentage <= 20) {
            binding.oreoStatus.loadImage(
                requireContext(),
                R.drawable.ic_ring_default_silver_new
            )
            binding.batteryStatus.setIndicatorColor(resources.getColor(R.color.color_error))
        } else {
            binding.oreoStatus.setBackgroundResource(R.drawable.back_modal_new_round)
            binding.batteryStatus.setIndicatorColor(resources.getColor(R.color.white))
        }
    }

    private fun setConnectingState() {
        binding.lytRingConnecting.root.visible()
        binding.imageConnecting.visible()
        binding.groupRingStatus.gone()
        binding.ivCross.invisible()
    }

    override fun subscribeObservers() {

        viewModel.showWorkoutStoppedByRingDialog.observe(viewLifecycleOwner) {
            it.getContent()?.let {

                setFragmentResultListener(
                    WORKOUT_STOP_KEY,
                ) { _, bundle ->

                    val allow = bundle.getBoolean("allow")
                    val delete = bundle.getBoolean("delete")

                    if (allow || delete) {

                        if (delete) {
                            viewModel.markedDeleted = true
                            viewModel.markForDelete(viewModel.sportStartTime)
                        }

                        stopWorkout()
                        viewModel.sessionManager.sendUserActivityAction(
                            UserActivityAction.SyncAutoSportsActivity()
                        )

                    }
                }
                viewModel.stopTimer()

                navigate(R.id.workoutStopRingBottomSheet)
            }
        }

        viewModel.sessionManager.showWorkoutDetails.observe(viewLifecycleOwner) {
            it.getContent()?.let { workoutId ->

                if (workoutId != null) {
                    if (viewModel.currentWorkoutState == 4) {
                        if (workoutId.equals("none")) {
                            navigateUpSafe()
                            return@observe
                        }
                        navigate(
                            RecordWorkoutFragmentDirections.actionRecordWorkoutFragmentToOWorkoutDetailsFragment(
                                workoutId,
                                -1
                            )
                        )
                    }
                }
            }
        }

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
                    /*is UpdateDeviceDataCallback.WorkoutEndFromRingState -> {
                        AppLogs.sendAppLogs("Workout failed from ring Reason: ${it.errorMessage}")
                        stopWorkout()
                    }*/

                    is UpdateDeviceDataCallback.WorkoutStartState -> {
                        if (it.success) {
                            startWorkout()
                        } else {
                            //context.showShortToast("Workout started : ${it.success}")
                        }
                        binding.progressBar.root.gone()
                    }

                    /*is UpdateDeviceDataCallback.OngoingWorkoutData -> {
                        setWorkoutData(it.data)
                    }*/

                    is UpdateDeviceDataCallback.WorkoutStopped -> {
                        binding.progressBar.root.gone()
                        if (it.success) {
                            stopWorkout()
                        } else {
                            //context.showShortToast("Workout Stopped : ${it.success}")
                        }
                    }

                    is UpdateDeviceDataCallback.WorkoutStoppedByRing -> {
                        binding.progressBar.root.gone()

                        viewModel.showWorkoutStoppedByRingDialog.postValue(Event(true))

                    }

                    is UpdateDeviceDataCallback.WorkoutPaused -> {
                        if (it.success) {
                            pauseWorkout()
                        } else {
                            //context.showShortToast("Workout Paused : ${it.success}")
                        }
                        binding.progressBar.root.gone()
                    }

                    is UpdateDeviceDataCallback.WorkoutResumed -> {
                        if (it.success) {
                            resumeWorkout()
                        } else {
                            //context.showShortToast("Workout Resumed : ${it.success}")
                        }
                        binding.progressBar.root.gone()
                    }


                    else -> {}
                }

            }

        }

    }
}