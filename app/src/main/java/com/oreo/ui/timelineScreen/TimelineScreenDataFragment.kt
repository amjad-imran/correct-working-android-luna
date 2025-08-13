package com.oreo.ui.timelineScreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.databinding.FragmentTimelineScreenDataBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TimelineScreenDataFragment : BaseFragment<FragmentTimelineScreenDataBinding>(FragmentTimelineScreenDataBinding::inflate) {
    private val ARGS_DATE = "ARGS_DATE"

    private val viewModel : TimelineScreenDataViewmodel by viewModels()

    private val activityListAdapter by lazy {
        ActivitiesListTimelineAdapter()
    }

    companion object {
        @JvmStatic
        fun newInstance(date: String) = TimelineScreenDataFragment().apply {
            arguments = Bundle().apply {
                putString(ARGS_DATE, date)
            }
        }
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {
        viewModel.activityListData.observe(this){
            activityListAdapter.updateDataSet(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val date = arguments?.getString(ARGS_DATE)
        viewModel.date = date
        setRecycler()
    }

    private fun setRecycler() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = activityListAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        viewModel.date?.let {
            viewModel.getCurrDayActivities(it)
        }
    }

}