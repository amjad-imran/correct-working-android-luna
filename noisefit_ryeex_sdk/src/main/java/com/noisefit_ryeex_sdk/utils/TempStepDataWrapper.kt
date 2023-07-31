package com.noisefit_ryeex_sdk.utils

import com.ryeex.watch.adapter.model.entity.LibHealthCalorieDomain
import com.ryeex.watch.adapter.model.entity.LibHealthDistanceDomain
import com.ryeex.watch.adapter.model.entity.LibHealthStepDomain

data class TempStepDataWrapper(
    var stepHealthDomain: LibHealthStepDomain? = null,
    var distanceHealthDomain: LibHealthDistanceDomain? = null,
    var calorieHealthDomain: LibHealthCalorieDomain? = null
)

data class TempStep24DataWrapper(
    var step: Int? = null,
    var distance: Int? = null,
    var calories: Int? = null,
)