package com.noisefit_commans.data.model.circadian

data class CircadianGraphData(
    val activityWindowGraph: ItemCircadianGraphData?,
    val caffeineWindowGraph: ItemCircadianGraphData?,
    val cortisolPeakWindowGraph: ItemCircadianGraphData?,
    val dlmoPhaseWindowGraph: ItemCircadianGraphData?,
    val firstFocusPeakWindowGraph: ItemCircadianGraphData?,
    val ghPulseWindowGraph: ItemCircadianGraphData?,
    val lightAnchoringPhaswWindowGraph: ItemCircadianGraphData?,
    val melatoninPrepPhaseWindowGraph: ItemCircadianGraphData?,
    val secondFocusPeakWindowGraph: ItemCircadianGraphData?,
    val sleepWindowOpensGraph: ItemCircadianGraphData?
)

data class ItemCircadianGraphData(
    val start_time: String,
    val end_time: String,
)