package com.noisefit.data.local.db.abstraction

import com.noisefit_commans.data.model.KeyValue


interface KeyValueDataSource {

    suspend fun insertData(
        keyValue: KeyValue
    )

    suspend fun getData(key: String, type: KeyValueDataType): KeyValue?

    suspend fun removeDataByKey(key: String, type: KeyValueDataType)

    suspend fun removeDataByType(type: KeyValueDataType)
}

enum class KeyValueDataType {
    CHALLENGE_2, CHALLENGE_LEADERBOARD_2, CHALLENGE_BUDDIES_2, H_AND_S,
    FRIENDS_ACTIVITY, FRIENDS_COMPETITION, FRIENDS_PROFILE,
    USER_COUPON, USER_COUPON_LIST, DASH_STREAK_DATA, COINS_PROFILE_DATA, ALL_TASK_LIST, WATCH_FACE_2, WATCH_FACE_2_CATEGORY_LIST
}