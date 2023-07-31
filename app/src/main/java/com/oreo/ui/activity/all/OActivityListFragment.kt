package com.oreo.ui.activity.all

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit.databinding.FragmentOActivityListBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.OActivityListModal
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OActivityListFragment :
    BaseFragment<FragmentOActivityListBinding>(FragmentOActivityListBinding::inflate),
    OActivityListInteraction {

    private val adapter: OActivityListAdapter by lazy {
        OActivityListAdapter(this, viewModel.dataUnitConverter)
    }

    private val viewModel: OActivityListViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
        if(viewModel.activities.value.isNullOrEmpty()){
            viewModel.fetchActivityFromServer()
        }

    }

    override fun initListener() {

        binding.lytToolbar.apply {
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
            tvTitle.text = getString(R.string.text_all_workouts)

        }

//        binding.srActivity.setOnRefreshListener {
//
//            Handler(Looper.getMainLooper()).postDelayed(dismissRunnable, 10000)
//
//        }

        binding.rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (viewModel.isActivitiesLoading || viewModel.isLastPage) {
                        return
                    }
                    nullableBinding?.let {
                        val visibleItemCount = it.rv.layoutManager?.childCount ?: 0
                        val totalItemCount = it.rv.layoutManager?.itemCount ?: 0
                        val firstVisibleItemPosition =
                            (it.rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                            // viewModel.fetchActivityFromServer()
                        }
                    }
                }
            }

        })
    }

    private fun setRecycler() {
        binding.rv.layoutManager = LinearLayoutManager(context)
        binding.rv.adapter = adapter
        adapter.connectedDevice = viewModel.sessionManager.connectedDevice.value?.deviceType ?: ""
        adapter.unitsSystem = viewModel.localDataStore.getUnit()
    }

    override fun subscribeObservers() {
        viewModel.activities.observe(this) {
            adapter.setDataSet(it)
//            if (it.isEmpty()) {
//
//                binding.tvNoActivities.visible()
//                binding.header.textViewTitle.visible()
//                binding.header.textViewTitle.text = getString(R.string.text_activity)
//            } else {
//                binding.tvNoActivities.gone()
//                /* binding.tvToolbarText.text = DateFormats.formatDateTime(
//                     viewModel.activitiesDates.first(),
//                     DateFormats.dateFormat2,
//                     DateFormats.dateFormat4
//                 )*/
//                binding.header.textViewTitle.visible()
//            }
        }



//        viewModel.getLoading().observe(this) {
//            if (it) {
//                if (!binding.srActivity.isRefreshing) {
//                    binding.srActivity.isRefreshing = true
//                }
//            } else {
//                binding.srActivity.isRefreshing = false
//            }
//        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }

//    private val dismissRunnable = Runnable {
//        try {
//            if (binding.srActivity.isRefreshing) {
//                binding.srActivity.isRefreshing = false
//            }
//        } catch (exp: Exception) {
//        }
//    }


    override fun onActivitySelected(activity: OActivityListModal) {
        navigate(R.id.oWorkoutDetailsFragment, Bundle().apply {
            putString("workoutId", activity.id)
            putString("workoutName", activity.getFormattedActivityName())
        })
    }


}