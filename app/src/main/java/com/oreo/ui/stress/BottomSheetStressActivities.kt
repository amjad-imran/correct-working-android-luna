package com.oreo.ui.stress

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetDaytimeActivitiesBinding
import com.noisefit.oreo.BottomNavOption
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.showShortToast
import com.oreo.data.model.OStressActivitiesDataModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BottomSheetStressActivities :
    BaseBottomSheetWithTransparent<BottomSheetDaytimeActivitiesBinding>(
        BottomSheetDaytimeActivitiesBinding::inflate
    ) {

    private val viewModel: OreoMainViewModel by viewModels()

    private val dataList = ArrayList<OStressActivitiesDataModel>()
    val args: BottomSheetStressActivitiesArgs by navArgs()
    private val stressActivitiesAdapter by lazy {
        StressActivitiesAdapter(object :
            StressActivitiesAdapter.StressActivitiesInteractionListener {
            override fun onActivitiesSelected(data: OStressActivitiesDataModel) {
                if (data.type.equals("workout", true)) {
                    if (data.workoutData == null) return

                    if (data.workoutData.getDisplayVersionType() == 2) {
                        navigate(R.id.oWorkoutDetailsFragmentV2, Bundle().apply {
                            putString("workoutId", data.workoutData.id ?: "")
                            putInt("position", -1)
                        })
                    } else {
                        navigate(R.id.oWorkoutDetailsFragment, Bundle().apply {
                            putString("workoutName", data.workoutData.getTranslatedActivityName())
                            putString("workoutId", data.workoutData.id ?: "")
                            putInt("position", -1)
                        })
                    }
                } else if (data.type.equals("sleep", true)) {
                    //viewModel.navigateTo(BottomNavOption.SLEEP)
                    navigateUpSafe()
                } else if (data.type.equals("nap", true)) {
                    //viewModel.navigateTo(BottomNavOption.SLEEP)
                    data.id?.let {
                        navigate(R.id.napDetails, bundleOf("napId" to data.id))
                    }
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