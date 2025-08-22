package com.oreo.ui.timelineScreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentTimelineScreenDataBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class TimelineScreenDataFragment :
    BaseFragment<FragmentTimelineScreenDataBinding>(FragmentTimelineScreenDataBinding::inflate) {
    private val ARGS_DATE = "ARGS_DATE"

    private val viewModel: TimelineScreenDataViewmodel by viewModels()

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
        viewModel.activityListData.observe(this) {
            if (it.isEmpty()) {
                binding.recyclerView.gone()
                binding.lytNoActivity.apply {
                    textView195.text = if (viewModel.date.equals(LocalDate.now().toString(), true))
                        getString(R.string.text_it_looks_like_you_have_not_logged_any_activities_yet)
                    else
                        getString(R.string.text_it_looks_like_you_have_not_logged_any_activities_for_this_day)
                    imageView102.setBackgroundResource(R.drawable.ic_no_activity_timeline)
                    root.visible()
                }
            } else {
                binding.lytNoActivity.root.gone()
                binding.recyclerView.visible()
                activityListAdapter.updateDataSet(it)
            }
        }

        //
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(viewLifecycleOwner) {
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