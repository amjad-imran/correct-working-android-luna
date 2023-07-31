package com.noisefit_commans.data.response

import com.noisefit_commans.data.model.RewardAboutSubCategoryList


data class AboutStreakResponse(
    val about_streak: List<RewardAboutSubCategoryList>,
    val terms_and_condition: List<String>,
)