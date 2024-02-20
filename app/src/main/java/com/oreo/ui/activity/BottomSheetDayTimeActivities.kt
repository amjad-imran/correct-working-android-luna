package com.oreo.ui.activity

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetDaytimeActivitiesBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.oreo.data.model.ODayTimeActivitiesDataModel


class BottomSheetDayTimeActivities :
    BaseBottomSheetWithTransparent<BottomSheetDaytimeActivitiesBinding>(
        BottomSheetDaytimeActivitiesBinding::inflate
    ) {

    private val dataList = ArrayList<ODayTimeActivitiesDataModel>()
    val args: BottomSheetDayTimeActivitiesArgs by navArgs()
    private val stressActivitiesAdapter by lazy {
        ODayTimeActivitiesAdapter(object :
            ODayTimeActivitiesAdapter.DayTimeActivitiesInteractionListener {
            override fun onActivitiesSelected(data: ODayTimeActivitiesDataModel) {
                if (data.type.equals("workout", true)) {
                    if (data.workoutData == null) return

                    navigate(R.id.oWorkoutDetailsFragment, Bundle().apply {
                        putString("workoutName", data.workoutData.getFormattedActivityName())
                        putString("workoutId", data.workoutData.id ?: "")
                        putInt("position", -1)
                    })
                } else if (data.type.equals("sleep", true)) {
                    //viewModel.navigateTo(BottomNavOption.SLEEP)
                } else if (data.type.equals("nap", true)) {
                    //viewModel.navigateTo(BottomNavOption.SLEEP)
                }
            }
        })
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        args.let {
            dataList.addAll(args.dataList)
        }
        setRecycler()
    }

    private fun setRecycler() {
        binding.rvWorkouts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = stressActivitiesAdapter
        }
        stressActivitiesAdapter.setDataSet(dataList)
    }
}