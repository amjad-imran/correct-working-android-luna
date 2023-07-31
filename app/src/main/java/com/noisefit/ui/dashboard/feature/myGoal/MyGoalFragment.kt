package com.noisefit.ui.dashboard.feature.myGoal

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentMyGoalBinding
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.data.enums.Device
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyGoalFragment :
    BaseFragment<FragmentMyGoalBinding>(FragmentMyGoalBinding::inflate) {

    private val viewModel: MyGoalViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val deviceType = viewModel.localDataStore.getPairDeviceType()
        if (deviceType == Device.RING) {
            val ringDevice = viewModel.ringDataStore.getRingDevice()
            if (ringDevice == null) {
                context.showShortToast(getString(R.string.text_no_device_paired))
                navigateUpSafe()
                return
            }
            binding.features = viewModel.ringDataStore.getDeviceFeatures()

        } else {
            val connectedDevice = viewModel.localDataStore.getConnectedDevice()
            if (connectedDevice == null) {
                context.showShortToast(getString(R.string.text_no_device_paired))
                navigateUpSafe()
                return
            }
            binding.features = viewModel.localDataStore.getDeviceFeatures()
        }


        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(com.noisefit_commans.R.string.text_my_goals)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }
        setData(false)
    }

    override fun initListener() {
        binding.tvEdit.setOnClickListener {
            if (binding.tvEdit.text == getString(R.string.text_edit)) {
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.GOALS_EDIT_CLICK)
                viewModel.setEditMode(true)
            } else {
                viewModel.setEditMode(false)
                saveUser()
                viewModel.updateUserProfile()

                viewModel.sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.SetUserInfo(
                        viewModel.userInfo!!,
                        viewModel.userGoals!!,
                        viewModel.user?.firstName
                    )
                )
            }
        }
//        binding.bContinue.setOnClickListener {
//            saveUser()
//            viewModel.updateUserProfile()
//
//            viewModel.sessionManager.sendUpdateQueryAction(
//                UpdateDeviceAction.SetUserInfo(
//                    viewModel.userInfo!!,
//                    viewModel.userGoals!!,
//                    viewModel.user?.firstName
//                )
//            )
//        }

        binding.llStepsGoal.setOnClickListener {
            if (viewModel.editMode.value == false) {
                return@setOnClickListener
            }
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let {
                    viewModel.userGoals!!.stepGoal = it.split(" ")[0].toInt()
                    setStepsGoal()
                    LOGS.i("$position | $selectedValue")

                }


            }
            navigate(R.id.valueSelectorBottomSheet,Bundle().apply {
                this.putString("selectedValue", "${viewModel.userGoals!!.stepGoal} steps")
                this.putString("title", getString(R.string.text_steps))
                this.putStringArray("selectionList", viewModel.stepsGoalList.toTypedArray())

            })
        }

        binding.llCaloriesGoal.setOnClickListener {
            if (viewModel.editMode.value == false) {
                return@setOnClickListener
            }
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let {
                    viewModel.userGoals!!.caloriesGoal = it.split(" ")[0].toInt()
                    setCaloriesGoal()
                    LOGS.i("$position | $selectedValue")
                }

            }
            navigate(
                R.id.valueSelectorBottomSheet, Bundle().apply {
                    this.putString("selectedValue", "${viewModel.userGoals!!.caloriesGoal} kcal")
                    this.putString("title", getString(R.string.text_calories))
                    this.putStringArray("selectionList", viewModel.caloriesGoalList.toTypedArray())
                }
            )
        }

        binding.llDistanceGoal.setOnClickListener {
            if (viewModel.editMode.value == false) {
                return@setOnClickListener
            }
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let {
                    viewModel.userGoals!!.distanceGoal =
                        viewModel.getDistanceInMeter(it.split(" ")[0].toInt())
                    setDistanceGoal()
                    LOGS.i("$position | $selectedValue")

                }


            }
            navigate(
                R.id.valueSelectorBottomSheet,
                Bundle().apply {
                    this.putString(
                        "selectedValue",
                        "${viewModel.getDistanceFromMeter(viewModel.userGoals!!.distanceGoal)} ${viewModel.distanceUnit}"
                    )
                    this.putString("title", getString(R.string.text_distance))
                    this.putStringArray("selectionList", viewModel.distanceGoalList.toTypedArray())
                }
            )
        }

        binding.llDurationGoal.setOnClickListener {
            if (viewModel.editMode.value == false) {
                return@setOnClickListener
            }
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let {
                    viewModel.userGoals?.durationInMin = it.split(" ")[0].toInt()
                    setDurationMinGoal()
                    LOGS.i("$position | $selectedValue")

                }


            }
            navigate(
                R.id.valueSelectorBottomSheet,
                Bundle().apply {
                    this.putString(
                        "selectedValue",
                        "${viewModel.userGoals?.durationInMin} min"
                    )
                    this.putString("title", getString(R.string.text_duration))
                    this.putStringArray("selectionList", viewModel.durationList.toTypedArray())
                }
            )
        }
    }

    private fun setStepsGoal() {
        val steps = "${viewModel.userGoals?.stepGoal} steps"
        binding.llStepsGoal.setStatus(viewModel.editMode.value!!, steps)
        viewModel.userGoals?.stepGoal?.let {
            viewModel.sessionManager.addUserAttributeToInsider(false, HashMap<String, Any>().apply {
                this["step_goal"] = it
            })
        }
    }

    private fun setCaloriesGoal() {
        val calories = "${viewModel.userGoals?.caloriesGoal} kcal"
        binding.llCaloriesGoal.setStatus(viewModel.editMode.value!!, calories)
        viewModel.userGoals?.caloriesGoal?.let {
            viewModel.sessionManager.addUserAttributeToInsider(false, HashMap<String, Any>().apply {
                this["calories_goal"] = it
            })
        }
    }

    /*private fun setStandHrGoal() {
        val data = "${viewModel.userGoals?.standingHr} hr"
        binding.llStandingGoal.setStatus(viewModel.editMode.value!!, data)
    }*/

    private fun setDurationMinGoal() {
        val data = "${viewModel.userGoals?.durationInMin} min"
        binding.llDurationGoal.setStatus(viewModel.editMode.value!!, data)
    }

    private fun setDistanceGoal() {
        val distance = "${
            viewModel.getDistanceFromMeter(viewModel.userGoals?.distanceGoal ?: 0)
        } ${viewModel.distanceUnit}"
        binding.llDistanceGoal.setStatus(viewModel.editMode.value!!, distance)

        viewModel.userGoals?.distanceGoal?.let {
            viewModel.sessionManager.addUserAttributeToInsider(false, HashMap<String, Any>().apply {
                this["distance_goal"] = it
            })
        }
    }

    private fun saveUser() {
        val user = viewModel.user
        user!!.userGoals = viewModel.userGoals
        viewModel.setUserGoal(user)
    }

    private fun setData(isEdit: Boolean) {
        if (isEdit) {
            binding.tvEdit.text = getString(R.string.text_save)
//            binding.bContinue.visible()
            binding.tvEdit.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.edit_profile_text_color
                )
            )
            binding.tvEdit.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.edit_profile_text_color
                )
            )

        } else {
            binding.tvEdit.text = getString(R.string.text_edit)
//            binding.bContinue.gone()
        }



        setCaloriesGoal()
        setStepsGoal()
        setDistanceGoal()
        //setStandHrGoal()
        setDurationMinGoal()

    }


    override fun subscribeObservers() {
        viewModel.editMode.observe(this) {
            if (it == true) {
                setData(true)
            } else {
                setData(false)
            }
        }

        viewModel.showDurationMinLayout.observe(this) {
            if (it == true) {
                binding.llDurationGoal.visible()
            } else {
                binding.llDurationGoal.gone()
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        viewModel.sessionManager.updateDeviceCallback.observe(this) {
            val event = it.getContent() ?: return@observe
            when (event) {
                is UpdateDeviceDataCallback.UserInfoUpdated -> {
                    viewModel.setLoading(false)
                    if (event.success) {
                        viewModel.setEditMode(false)
                        context.showShortToast(getString(R.string.text_setting_updated))
                    }
                }

                else -> {}
            }
        }
    }

}