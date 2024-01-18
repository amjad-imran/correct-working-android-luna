package com.oreo.data.model

data class SleepOverlayData(
    val hrBreakup: List<Int>,
    val stressBreakup: List<Int>,
    var respBreakup: List<Int>,
    val tempBreakup: List<Float>,
    var spo2Breakup: List<Int>
)

data class NapOverlayData(
    val hrBreakup: List<Int>,
    val hrvBreakup: List<Int>,
    var respBreakup: List<Int>,
    val tempBreakup: List<Float>,
    var spo2Breakup: List<Int>
)