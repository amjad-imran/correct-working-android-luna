package com.noisefit.data.local.db.implementation

import com.noisefit.data.local.db.abstraction.FeedsDataSource
import com.noisefit.data.local.db.database.FeedsDao
import com.noisefit.data.local.db.database.FeedsType
import com.noisefit.data.model.FeedsDbValue
import javax.inject.Inject

class FeedsDataSourceImpl
@Inject
constructor(
    private val keyValueDao: FeedsDao
) : FeedsDataSource {

    override suspend fun updateFeedById(postId: Long, value: String) {
        keyValueDao.updateFeedById(postId,value)
    }

    override suspend fun insertData(keyValue: FeedsDbValue) {
        keyValueDao.insert(keyValue)
    }

    override suspend fun getTimelineFeeds(): List<FeedsDbValue>? {
        return keyValueDao.getFeeds(FeedsType.TIMELINE.name)
    }

    override suspend fun getTimelineFeedById(postId:Long): FeedsDbValue? {
        return keyValueDao.getFeedsById(FeedsType.TIMELINE.name,postId)
    }

    override suspend fun removeTimelineFeeds() {
        keyValueDao.deleteByType(FeedsType.TIMELINE.name)
    }

    override suspend fun removeAllFeeds() {
        keyValueDao.deleteByType(FeedsType.TIMELINE.name)
    }

    override suspend fun removeDataByKey(key: String, type: FeedsType) {
        keyValueDao.deleteByKey(key, type.name)
    }

    override suspend fun removeDataByType(type: FeedsType) {
        keyValueDao.deleteByType(type.name)
    }
}