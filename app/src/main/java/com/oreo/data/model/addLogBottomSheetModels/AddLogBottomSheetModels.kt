package com.oreo.data.model.addLogBottomSheetModels

sealed class AddLogBottomSheetDataModels(){

    data class MealIntakeAddLogModel(
        val title: String
    ): AddLogBottomSheetDataModels()

    data class AddNextItemLogBsModel(
        val title: String
    ): AddLogBottomSheetDataModels()

}