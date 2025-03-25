package com.oreo.ui.notification

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentEditNotificationGoalBinding
import com.noisefit.ui.profile.ProfileEditViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.noisefit_commans.utils.WheelAdapter
import com.noisefit_commans.utils.WheelItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditNotificationGoalFragment :
    BaseFragment<FragmentEditNotificationGoalBinding>(FragmentEditNotificationGoalBinding::inflate) {

    private val viewModel: EditNotificationGoalViewModel by viewModels()
    private val profileViewModel: ProfileEditViewModel by viewModels()

    private val wheelAdapterHydrationImperial: WheelAdapter<String> by lazy {
        WheelAdapter()
    }

    private val wheelAdapterHydrationMetricLiter: WheelAdapter<String> by lazy {
        WheelAdapter()
    }

    private val wheelAdapterHydrationMetricMl: WheelAdapter<String> by lazy {
        WheelAdapter()
    }

    private val wheelAdapterStepsPicker: WheelAdapter<String> by lazy {
        WheelAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.tvTitle.text = getString(R.string.text_edit_daily_goals)
        val isMetric = viewModel.sessionManager.isMetric()
        initHydrationUi(viewModel.getHydrationGoalList(isMetric), isMetric)
        initStepsUi(viewModel.getStepsGoalsList())
        viewModel.getNotificationGoals()
    }

    override fun initListener() {

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.btnCancel.setOnClickListener {

            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.action_cancelled,
                HashMap<String, Any>().apply {
                    this["source"] = "goals"
                }
            )

            navigateUpSafe()
        }

        binding.btnSave.setOnClickListener {

//            viewModel.sessionManager.logMoEngageAppEvent(
//                MoEngageLunaAppEvents.goals_set,
//                HashMap<String, Any>().apply {viewModel.sessionManager.logMoEngageAppEvent(
//                MoEngageLunaAppEvents.goals_set,
//                HashMap<String, Any>().apply {
//                    this["goal"] = "goals"
//                }
//            )
//                    this["goal"] = "goals"
//                }
//            )

            val selectedHydrationGoal = viewModel.hydrationGoal
            val selectedStepsGoal = viewModel.stepsGoal

            if (selectedStepsGoal != null && selectedHydrationGoal != null) {

                if (selectedHydrationGoal == 0) {
                    return@setOnClickListener
                }

                profileViewModel.hydrationGoal = selectedHydrationGoal
                profileViewModel.stepsGoal = selectedStepsGoal

                profileViewModel.updateUserProfile()
            }
        }
    }

    override fun subscribeObservers() {
        profileViewModel.userDetailsUpdated.observe(this) {
            it.getContent()?.let {
                navigateUpSafe()
            }
        }

        viewModel.notificationGoalReceived.observe(this) {
            it.getContent()?.let {
                val selectedPosition = viewModel.getSelectedStepsPosition(it.steps_required ?: 3000)

                if (selectedPosition != -1) {
                    wheelAdapterStepsPicker.selectedItemPosition = selectedPosition
                }

                if (viewModel.sessionManager.isMetric()) {

                    val hydrationValue = (it.hydration_required ?: 3000)

                    val selectedPositionL =
                        viewModel.getSelectedHydrationMetricPositionL(hydrationValue)
                    if (selectedPositionL != -1) {
                        wheelAdapterHydrationMetricLiter.selectedItemPosition = selectedPositionL
                    }

                    val selectedPositionMl =
                        viewModel.getSelectedHydrationMetricPositionMl(hydrationValue)
                    if (selectedPositionMl != -1) {
                        wheelAdapterHydrationMetricMl.selectedItemPosition = selectedPositionMl
                    }

                } else {
                    val hydrationValue = (it.hydration_required ?: 3000)

                    val convertedValue =
                        viewModel.convertMlToOuncesRounded(hydrationValue.toDouble())

                    val selectedPositionHyImp =
                        viewModel.getSelectedHydrationImperialPosition(convertedValue)
                    if (selectedPositionHyImp != -1) {
                        wheelAdapterStepsPicker.selectedItemPosition = selectedPositionHyImp
                    }
                }
            }
        }

        /*viewModel.notificatioGoal.observe(this){
            initHydrationUi(viewModel.getHydrationGoalList(false), true)
            initStepsUi(viewModel.getStepsGoalsList())
        }*/


        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        profileViewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        profileViewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        profileViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
    }

    private fun initHydrationUi(
        selectionList: Pair<List<String>, List<String>>,
        isMetric: Boolean
    ) {

        if (isMetric) {
            binding.lytHydrationPicker.lytWheelPickerMetric.visible()
            binding.lytHydrationPicker.wheelPicker.gone()

            val listData = ArrayList<WheelItem<String>>()
            selectionList.first.forEach {
                listData.add(WheelItem(it))
            }
            binding.lytHydrationPicker.wheelPickerMetricLiter.visibleItemCount =
                5//it could not be less then 3
            wheelAdapterHydrationMetricLiter.data = listData
            wheelAdapterHydrationMetricLiter.setOnItemSelectedListener { item ->
                Log.d(
                    "TAG",
                    "onItemSelected: ${item.split(" ").get(0)}"
                )
                tryCatch {

                    val selectedLiter = item.split(" ").get(0).toIntOrNull()
                    val selectedMl =
                        selectionList.second[wheelAdapterHydrationMetricMl.selectedItemPosition].split(
                            " "
                        ).get(0).toIntOrNull()

                    val total = ((selectedLiter ?: 0) * 1000) + (selectedMl ?: 0)

                    viewModel.updateHydration(total, isMetric)
                }
            }
            wheelAdapterHydrationMetricLiter.bind(binding.lytHydrationPicker.wheelPickerMetricLiter)


            val listDataMl = ArrayList<WheelItem<String>>()
            selectionList.second.forEach {
                listDataMl.add(WheelItem(it))
            }
            binding.lytHydrationPicker.wheelPickerMetricMl.visibleItemCount =
                5//it could not be less then 3
            wheelAdapterHydrationMetricMl.data = listDataMl
            wheelAdapterHydrationMetricMl.setOnItemSelectedListener { item ->

                Log.d(
                    "TAG",
                    "onItemSelected: ${item.split(" ").get(0)}"
                )
                tryCatch {
                    val selectedLiter =
                        selectionList.first[wheelAdapterHydrationMetricLiter.selectedItemPosition].split(
                            " "
                        ).get(0).toIntOrNull()
                    val selectedMl = item.split(" ").get(0).toIntOrNull()

                    val total = ((selectedLiter ?: 0) * 1000) + (selectedMl ?: 0)

                    viewModel.updateHydration(total, isMetric)
                }
            }
            wheelAdapterHydrationMetricMl.bind(binding.lytHydrationPicker.wheelPickerMetricMl)

            //dummy init
            wheelAdapterHydrationImperial.data = arrayListOf(WheelItem("0"))
            wheelAdapterHydrationImperial.bind(binding.lytHydrationPicker.wheelPicker)


        } else {
            binding.lytHydrationPicker.lytWheelPickerMetric.gone()
            binding.lytHydrationPicker.wheelPicker.visible()
            val listData = ArrayList<WheelItem<String>>()
            selectionList.first.forEach {
                listData.add(WheelItem(it))
            }
            binding.lytHydrationPicker.wheelPicker.visibleItemCount = 5//it could not be less then 3
            wheelAdapterHydrationImperial.data = listData
            wheelAdapterHydrationImperial.setOnItemSelectedListener { item ->
                Log.d(
                    "TAG",
                    "onItemSelected: ${item.split(" ").get(0)}"
                )
                tryCatch {
                    val convertedValue = item.split(" ")[0].toIntOrNull()
                    if (convertedValue != null) {
                        viewModel.updateHydration(convertedValue, isMetric)
                    }
                }
            }
            wheelAdapterHydrationImperial.bind(binding.lytHydrationPicker.wheelPicker)

            //dummy init
            wheelAdapterHydrationMetricLiter.data = arrayListOf(WheelItem("0"))
            wheelAdapterHydrationMetricMl.data = arrayListOf(WheelItem("0"))
            wheelAdapterHydrationMetricMl.bind(binding.lytHydrationPicker.wheelPickerMetricMl)
            wheelAdapterHydrationMetricLiter.bind(binding.lytHydrationPicker.wheelPickerMetricLiter)

        }



        return

        /*   val listData = ArrayList<WheelItem<String>>()
           selectionList.first.forEach {
               listData.add(WheelItem(it))
           }
           binding.lytHydrationPicker.wheelPicker.visibleItemCount = 5//it could not be less then 3
           wheelAdapterHydrationImperial.data = listData
           wheelAdapterHydrationImperial.setOnItemSelectedListener { item ->
               Log.d(
                   "TAG",
                   "onItemSelected: ${item.split(" ").get(0)}"
               )
               tryCatch {
                   viewModel.updateHydration(item.split(" ")[0].toIntOrNull(), isMetric)
               }
               //mSelectedValue = mSelectionList?.get(wheelAdapterHydrationImperial.currentItemPosition) ?: ""
               //mSelectedPosition = wheelAdapterHydrationImperial.currentItemPosition
           }
           wheelAdapterHydrationImperial.bind(binding.lytHydrationPicker.wheelPicker)*/
        /*        mSelectedValue = if (selectedValue.isNullOrEmpty()) selectionList[0] else selectedValue
                mInitialSelectedValue = selectedValue
                mSelectedPosition = if (selectedValue.isNullOrEmpty()) {
                    0
                } else {
                    getSelectedPosition(selectedValue, selectionList)
                }*/
        //wheelAdapterHydrationImperial.selectedItemPosition = mSelectedPosition
    }

    private fun initStepsUi(selectionList: List<String>) {
        val listData = ArrayList<WheelItem<String>>()
        selectionList.forEach {
            listData.add(WheelItem(it))
        }
        binding.lytStepsPicker.wheelPicker.visibleItemCount = 5//it could not be less then 3
        wheelAdapterStepsPicker.data = listData
        wheelAdapterStepsPicker.setOnItemSelectedListener { item ->
            Log.d(
                "TAG",
                "onItemSelected: ${item.split(" ").get(0)}"
            )

            tryCatch {
                viewModel.stepsGoal = item.split(" ")[0].toIntOrNull()
            }
            //mSelectedValue = mSelectionList?.get(wheelAdapterHydrationImperial.currentItemPosition) ?: ""
            //mSelectedPosition = wheelAdapterHydrationImperial.currentItemPosition
        }
        wheelAdapterStepsPicker.bind(binding.lytStepsPicker.wheelPicker)
        /*        mSelectedValue = if (selectedValue.isNullOrEmpty()) selectionList[0] else selectedValue
                mInitialSelectedValue = selectedValue
                mSelectedPosition = if (selectedValue.isNullOrEmpty()) {
                    0
                } else {
                    getSelectedPosition(selectedValue, selectionList)
                }*/
        //wheelAdapterHydrationImperial.selectedItemPosition = mSelectedPosition
    }

    private fun getSelectedPosition(selectedValue: String, selectionList: Array<String>): Int {
        for ((index, item) in selectionList.withIndex()) {
            if (item.equals(selectedValue, true)) {
                return index
            }
        }
        return 0
    }

}