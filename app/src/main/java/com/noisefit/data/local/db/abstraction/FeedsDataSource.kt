package com.noisefit.data.local.db.abstraction

import com.noisefit.data.local.db.database.FeedsType
import com.noisefit.data.model.FeedsDbValue


interface FeedsDataSource {

    suspend fun insertData(
        keyValue: FeedsDbValue
    )

    suspend fun getTimelineFeeds(): List<FeedsDbValue>?

    suspend fun getTimelineFeedById(postId:Long): FeedsDbValue?

    suspend fun removeTimelineFeeds()

    suspend fun removeAllFeeds()

    suspend fun removeDataByKey(key: String, type: FeedsType)

    suspend fun removeDataByType(type: FeedsType)

    suspend fun updateFeedById(postId: Long, value: String)
}