package com.oreo.ui.timelineScreen

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentTimelineScreenDataBinding
import com.noisefit_commans.data.model.timeline.ItemTimelineResponseModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class TimelineScreenDataFragment :
    BaseFragment<FragmentTimelineScreenDataBinding>(FragmentTimelineScreenDataBinding::inflate) {
    private val ARGS_DATE = "ARGS_DATE"

    private val viewModel: TimelineScreenDataViewmodel by viewModels()

    private val activityListAdapter by lazy {
        ActivitiesListTimelineAdapter(){
            handleOnItemClick(it)
        }
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
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0 || dy < 0) {
                    viewModel.sessionManager.logMoEngageAppEvent(
                        MoEngageLunaAppEvents.page_scrolled,
                        HashMap<String, Any>().apply {
                            this["source"] = "timeline"
                        }
                    )
                }
            }

        })

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

    private fun handleOnItemClick(data: ItemTimelineResponseModel) {

        var canBeUpdated = false
        // TODO : Meal, Nap/Sleep
        when(data.event){
            "caffeine" -> {
                if(
                    viewModel.getDate(0).equals(viewModel.date)
                ){
                    data.canBeEditedOrDeleted = 3
                }
            }

            "caffeine" -> {
                if(
                    viewModel.getDate(0).equals(viewModel.date)
                ){
                    data.canBeEditedOrDeleted = 3
                }
            }

            "light-exposure" -> {
                if(
                    viewModel.getDate(0).equals(viewModel.date)
                ){
                    data.canBeEditedOrDeleted = 3
                }
            }

            "workout" -> {
                if(
                    viewModel.getDate(0).equals(viewModel.date)
                ){
                    data.canBeEditedOrDeleted = 2
                }
            }

            "sleep" -> {
                if(
                    viewModel.getDate(0).equals(viewModel.date) ||
                    viewModel.getDate(1).equals(viewModel.date)
                ){
                    canBeUpdated = true
                }
            }

            "nap" -> {
                if(
                    viewModel.getDate(0).equals(viewModel.date) ||
                    viewModel.getDate(1).equals(viewModel.date)
                ){
                    canBeUpdated = true
                }
            }

            "symptom" -> {
                if(
                    viewModel.getDate(0).equals(viewModel.date)
                ){
                    canBeUpdated = true
                }
            }

            "supplements" -> {
                if(
                    viewModel.getDate(0).equals(viewModel.date) ||
                    viewModel.getDate(1).equals(viewModel.date)
                ){
                    data.canBeEditedOrDeleted = 3
                }
            }

            "alcohol" -> {
                if(
                    viewModel.getDate(0).equals(viewModel.date) ||
                    viewModel.getDate(1).equals(viewModel.date)
                ){
                    data.canBeEditedOrDeleted = 3
                }
            }


            "recovery" -> {
                if(
                    viewModel.getDate(0).equals(viewModel.date) ||
                    viewModel.getDate(1).equals(viewModel.date)
                ){
                    data.canBeEditedOrDeleted = 3
                }
            }
        }

        navigate(
            R.id.addActivityTimelineFragment,
            bundleOf(
                "showTimeline" to false,
                "key" to data.event,
                "srcKey" to null,
                "editData" to data
            )
        )
    }

}