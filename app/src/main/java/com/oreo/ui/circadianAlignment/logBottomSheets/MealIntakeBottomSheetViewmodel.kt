package com.oreo.ui.circadianAlignment.logBottomSheets

import androidx.lifecycle.viewModelScope
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.addLogBottomSheetModels.AddLogBottomSheetDataModels
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MealIntakeBottomSheetViewmodel @Inject constructor(
    private val resourcesProvider: ResourcesProvider
): BaseViewModel() {

    val dataList: ArrayList<AddLogBottomSheetDataModels> = ArrayList()

    fun initMealsData(){
        viewModelScope.launch {
            val count = 1
            dataList.add(AddLogBottomSheetDataModels.MealIntakeAddLogModel(
                resourcesProvider.getString(
                    R.string.text_meal_val,
                    count
                )
            )
            )
            dataList.add(AddLogBottomSheetDataModels.AddNextItemLogBsModel(
                resourcesProvider.getString(
                    R.string.text_add_meal_val,
                    count+1
                )
            )
            )
        }
    }

    fun updateDataSet(function: () -> Unit) {
        function()
    }

}