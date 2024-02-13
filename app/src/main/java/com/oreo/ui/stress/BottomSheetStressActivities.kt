package com.oreo.ui.stress

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetStressActivitiesBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.oreo.data.model.OStressActivitiesDataModel

class BottomSheetStressActivities :
    BaseBottomSheetWithTransparent<BottomSheetStressActivitiesBinding>(
        BottomSheetStressActivitiesBinding::inflate
    ) {

    private val dataList = ArrayList<OStressActivitiesDataModel>()
    val args: BottomSheetStressActivitiesArgs by navArgs()
    private val stressActivitiesAdapter by lazy {
        StressActivitiesAdapter(object :
            StressActivitiesAdapter.StressActivitiesInteractionListener {
            override fun onActivitiesSelected(data: OStressActivitiesDataModel) {
                if (data.type.equals("workout", true)) {
                    if (data.workoutData == null) return

                    navigate(R.id.oWorkoutDetailsFragment, Bundle().apply {
                        putString("workoutName", data.workoutData.getFormattedActivityName())
                        putString("workoutId", data.workoutData.id ?: "")
                        putInt("position", -1)
                    })
                } else if (data.type.equals("sleep", true)) {

                } else if (data.type.equals("nap", true)) {

                }
            }
        })
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


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}