package com.noisefit.data.model

data class AlarmDataModel(var hour: Int = 0, var minute: Int = 0, var timeInMillis : Long, var state : String ="AM" )