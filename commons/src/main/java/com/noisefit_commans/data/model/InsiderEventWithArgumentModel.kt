package com.noisefit_commans.data.model


data class InsiderEventWithArgumentModel(var dataType: String="", var attributeName:String="", var value: Any?=null)

enum class InsiderDataType {
    String, Int, Double, Bool, Date, ArrayString
}
