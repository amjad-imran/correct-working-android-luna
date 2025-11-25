package com.oreo.ui.custom.sleep.internal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class GraphDataModel(
    val value1: Float?=null,
    val value2: Float?=null,
    val date: LocalDate,
) : Parcelable