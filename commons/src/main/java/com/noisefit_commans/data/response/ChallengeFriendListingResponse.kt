package com.noisefit_commans.data.response

data class ChallengeFriendListingResponse(
    val ongoingChallenges: List<ChallengeModel>?,
    val bestPerformedChallenges: List<ChallengeModel>?,
    val currentTime: String = ""
)