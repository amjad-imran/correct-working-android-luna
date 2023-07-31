package com.oreo.ui.profile

import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.WheelItem

class OAboutDeviceViewModel : BaseViewModel() {
    fun getDummyData(): ArrayList<WheelItem<String>> {
        val tempDataSet = ArrayList<WheelItem<String>>()
        tempDataSet.add(WheelItem("1000 steps"))
        tempDataSet.add(WheelItem("2000 steps"))
        tempDataSet.add(WheelItem("3000 steps"))
        tempDataSet.add(WheelItem("4000 steps"))
        tempDataSet.add(WheelItem("5000 steps"))
        tempDataSet.add(WheelItem("6000 steps"))
        tempDataSet.add(WheelItem("7000 steps"))
        tempDataSet.add(WheelItem("8000 steps"))
        tempDataSet.add(WheelItem("9000 steps"))
        tempDataSet.add(WheelItem("10000 steps"))

        return tempDataSet
    }
}