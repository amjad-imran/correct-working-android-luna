package com.oreo.ui.chatGpt.summary

import com.google.android.material.progressindicator.LinearProgressIndicator
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class AiSummaryViewModel @Inject constructor() : BaseViewModel() {
    var isPaused = false

    var currentStoryIndex = 0
    val storyDuration = 5000L
    val progressIndicators = ArrayList<LinearProgressIndicator>()


}