package com.noisefit.ui.friends.profile.activity

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit_commans.data.model.ProfileActivitiesData
import com.noisefit_commans.data.model.history.StepsHistoryData
import com.noisefit_commans.data.model.history.StepsHistoryResponse
import com.noisefit.databinding.FragmentFriendActivityBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.dashboard.graphs.steps.GraphInterval
import com.noisefit.ui.dashboard.graphs.steps.StepsGraphViewModel
import com.noisefit.ui.friends.profile.FriendProfileSharedViewModel
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.enums.HealthOverViewHistoryType
import com.noisefit_commans.models.Units
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.DistanceUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class FriendActivityFragment :
    BaseFragment<FragmentFriendActivityBinding>(FragmentFriendActivityBinding::inflate) {

    private val sharedViewModel: FriendProfileSharedViewModel by activityViewModels()
    private val viewModel: StepsGraphViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
    }

    private fun setAdapter() {
        binding.rvActivity.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ProfileActivityAdapter()
        }
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

        sharedViewModel.stepsHistoryResponse.observe(this) {
            if (it == null) {
                binding.rvActivity.adapter =
                    ProfileActivityAdapter().apply { setDataSet(ArrayList<ProfileActivitiesData>()) }
            } else {
                binding.rvActivity.adapter =
                    ProfileActivityAdapter().apply { setDataSet(getData(it)) }

            }
        }

    }

    private fun getData(stepsHistoryResponse: StepsHistoryResponse): ArrayList<ProfileActivitiesData> {

        val activityData = stepsHistoryResponse.step_activities
        val dataList = ArrayList<ProfileActivitiesData>()

        val nameString =
            if (stepsHistoryResponse.userName.isNullOrEmpty()) "" else "${stepsHistoryResponse.userName}'s "

        val weeklyList = stepsHistoryResponse.step_activities?.let { getWeekly(it) } ?: ArrayList()
        dataList.add(
            ProfileActivitiesData(
                "Steps",
                ApplicationUtils.getNumberAsPerUnit(
                    "steps",
                    stepsHistoryResponse.total?.steps ?: 0.0,
                    Units.METRIC
                ),
                ApplicationUtils.getNumberAsPerUnit(
                    "steps",
                    stepsHistoryResponse.max?.maxSteps ?: 0.0,
                    Units.METRIC
                ),
                ApplicationUtils.getNumberAsPerUnit(
                    "steps",
                    stepsHistoryResponse.avg?.steps ?: 0.0,
                    Units.METRIC
                ),
                HealthOverViewHistoryType.Steps,
                sharedViewModel.activityMessage?.steps,
                "${nameString}Max Step Count",
                stepsHistoryResponse.max?.maxStepsDate ?: "",
                activityData?.let {
                    viewModel.parseGraph(
                        it,
                        GraphInterval.WEEK,
                        HealthOverViewHistoryType.Steps
                    )
                },
                weeklyList
            )
        )
        dataList.add(
            ProfileActivitiesData(
                "Distance",
                total = DistanceUtil.getDistanceFromMeters(
                    stepsHistoryResponse.total?.distance?.roundToInt() ?: 0,
                    Units.METRIC
                ).toString(),
                max = DistanceUtil.getDistanceFromMeters(
                    stepsHistoryResponse.max?.maxDistance?.roundToInt() ?: 0,
                    Units.METRIC
                ).toString(),
                avg = DistanceUtil.getDistanceFromMeters(
                    stepsHistoryResponse.avg?.distance?.roundToInt() ?: 0,
                    Units.METRIC
                ).toString(),
                HealthOverViewHistoryType.Distance,
                sharedViewModel.activityMessage?.distance,
                "${nameString}Max Distance Count",
                stepsHistoryResponse.max?.maxDistanceDate ?: "",
                activityData?.let {
                    viewModel.parseGraph(
                        it,
                        GraphInterval.WEEK,
                        HealthOverViewHistoryType.Distance
                    )
                },
                weeklyList
            )
        )
        dataList.add(
            ProfileActivitiesData(
                "Calories",
                ApplicationUtils.getNumberAsPerUnit(
                    "calories",
                    stepsHistoryResponse.total?.calories ?: 0.0,
                    Units.METRIC
                ),
                ApplicationUtils.getNumberAsPerUnit(
                    "calories",
                    stepsHistoryResponse.max?.maxCalories ?: 0.0,
                    Units.METRIC
                ),
                ApplicationUtils.getNumberAsPerUnit(
                    "calories",
                    stepsHistoryResponse.avg?.calories ?: 0.0,
                    Units.METRIC
                ),
                HealthOverViewHistoryType.Calories,
                sharedViewModel.activityMessage?.calories,
                "${nameString}Max Calories Count",
                stepsHistoryResponse.max?.maxCaloriesDate ?: "",
                activityData?.let {
                    viewModel.parseGraph(
                        it,
                        GraphInterval.WEEK,
                        HealthOverViewHistoryType.Calories
                    )
                },
                weeklyList
            )
        )
        return dataList
    }

    private fun getWeekly(stepsDataList: ArrayList<StepsHistoryData>): ArrayList<String> {
        val monthlyList = ArrayList<String>()
        stepsDataList.forEach { stepData ->
            if (stepData.date != null) {
                monthlyList.add(DateFormats.formatWeek2(stepData.date))
            }
        }
        return monthlyList
    }
}