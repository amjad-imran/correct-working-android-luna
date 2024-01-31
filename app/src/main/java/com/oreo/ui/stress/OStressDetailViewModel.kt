package com.oreo.ui.stress

import com.noisefit_commans.common.maxWithInvalidMovementValues
import com.noisefit_commans.common.maxWithoutInvalidMovementValues
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OStressDetailViewModel @Inject
constructor() : BaseViewModel() {

    var date: String? = null


    fun getCombinedMovementData(
        originalList: List<Int>?,
        includeInvalid: Boolean = false
    ): List<Int> {
        if (originalList.isNullOrEmpty()) {
            return MutableList(96) { 255 }
        }
        val combinedList = ArrayList<Int>()
        for (i in originalList.indices step 3) {
            val endIndex = i + 3
            if (endIndex <= originalList.size) {
                val max = if (includeInvalid) {
                    originalList.subList(i, endIndex).maxWithInvalidMovementValues()
                } else {
                    originalList.subList(i, endIndex).maxWithoutInvalidMovementValues()
                }
                combinedList.add(max)
            }
        }
        return combinedList
    }


}