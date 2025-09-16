package com.oreo.ui.timelineScreen.addActivity.activities

import android.os.Bundle
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.moengage.core.internal.utils.showToast
import com.noisefit.data.remote.response.Watchface2
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentActivityListingBinding
import com.noisefit.ui.myDevice.DashboardWfFragment
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.timeline.addActivityTimelineModels.AddActivityListTimelineModel
import com.oreo.ui.timelineScreen.addActivity.ActivitiesListAdapter
import com.oreo.ui.timelineScreen.addActivity.AddActivityItemsEnum
import com.oreo.ui.timelineScreen.addActivity.AddActivityTimelineSharedViewModel
import kotlin.getValue


class ActivityListingFragment :
    BaseFragment<FragmentActivityListingBinding>(FragmentActivityListingBinding::inflate) {

    private val sharedViewModel: AddActivityTimelineSharedViewModel by activityViewModels()


    companion object {
        @JvmStatic
        fun newInstance() =
            ActivityListingFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    private val activitiesListAdapter by lazy {
        ActivitiesListAdapter() {
            when(it.type){
                AddActivityItemsEnum.SLEEP -> {
                    if(sharedViewModel.sessionManager.connectStateRing.value is ConnectState.ConnectSuccess){
                        sharedViewModel.loadFragmentByType(it.type)
                    }else{
                        showToast(requireContext(), getString(R.string.text_please_connect_your_ring_to_add_sleep))
                    }
                }

                AddActivityItemsEnum.WORKOUT -> {
                    if(sharedViewModel.sessionManager.connectStateRing.value is ConnectState.ConnectSuccess){
                        sharedViewModel.loadFragmentByType(it.type)
                    }else{
                        showToast(requireContext(), getString(R.string.text_please_connect_your_ring_to_add_a_workout))
                    }
                }

                else -> sharedViewModel.loadFragmentByType(it.type)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
    }


    private fun setRecycler() {
        binding.rvActivities.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = activitiesListAdapter
        }
        activitiesListAdapter.updateDataSet(sharedViewModel.getAllActivityListMap().apply {

        })
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}