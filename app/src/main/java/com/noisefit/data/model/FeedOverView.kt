package com.noisefit.data.model

import com.noisefit.data.model.timeline.TimelineData

sealed class FeedOverView {

    class Ads(
        var ads: ADSData
    ) : FeedOverView()

    class Post(
        var postData: TimelineData
    ) : FeedOverView()
}
