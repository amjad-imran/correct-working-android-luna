package com.oreo.ui.timelineScreen.addActivity

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddActivityTimelineBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.timeline.addActivityTimelineModels.AddActivityListTimelineModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddActivityTimelineFragment : BaseFragment<FragmentAddActivityTimelineBinding>(FragmentAddActivityTimelineBinding::inflate) {

    private val viewModel: AddActivityTimelineSharedViewModel by activityViewModels()

    private val activitiesListAdapter by lazy {
        ActivitiesListAdapter(){
            handleOnMainRecyclerClicked(it)
        }
    }

    private fun handleOnMainRecyclerClicked(item: AddActivityListTimelineModel) {
        binding.btnSave.isEnabled = true
        binding.rvActivities.gone()
        viewModel.selectedActivity.postValue(item.type)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSave.isEnabled = false
        viewModel.initActivityListData()
        setRecycler()
    }

    private fun setRecycler() {
        binding.rvActivities.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = activitiesListAdapter
        }
        activitiesListAdapter.updateDataSet(viewModel.allActivitiesList)
    }

    override fun initListener() {
        binding.ivClose.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
        viewModel.selectedActivity.observe(this){
            if(it == null) return@observe
            childFragmentManager.beginTransaction()
                .replace(R.id.childFragmentContainer, AddMealActivityTimelineFragment())
                .commit()

            binding.childFragmentContainer.visible()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearData()
    }

}