package com.oreo.ui.timelineScreen.addActivity

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.databinding.FragmentAddMealActivityTimelineBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddMealActivityTimelineFragment : BaseFragment<FragmentAddMealActivityTimelineBinding>(FragmentAddMealActivityTimelineBinding::inflate) {

    private val viewModel: AddActivityTimelineSharedViewModel by activityViewModels()

    private val rvMealsAdapter by lazy {
        MealIntakeRvAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUi()
        setRecycler()
    }

    private fun setUi() {
        LOGS.d("sjksalsj : ${viewModel.selectedActivity.value} , ${viewModel.allActivitiesList}")
        viewModel.selectedActivity.value?.let {
            viewModel.getAllActivityListMap()[it]?.let { currItem ->
                binding.tvSelected.apply {
                    text = currItem.name
                    setTextColor(currItem.titleColor)
                }
                viewModel.initSpecificActivityData(currItem.type)
            }
        }
    }

    private fun setRecycler() {
        binding.recylerView.layoutManager = LinearLayoutManager(this.context)
        binding.recylerView.adapter = rvMealsAdapter
    }

    override fun initListener() {
        binding.lytSelected.setOnClickListener {

        }

        rvMealsAdapter.itemClickListener = { type ->
            when(type){

                is AddLogItemsBottomSheetClickEnum.OnInputMealIntakeTimerLytClick -> {

                }
            }
        }
    }

    override fun subscribeObservers() {
        viewModel.selectedActivityInitData.observe(this){
            it?.let { rvMealsAdapter.updateDataSet(it) }
        }
    }

}