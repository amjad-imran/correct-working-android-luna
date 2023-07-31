package com.noisefit_commans.data.model

import com.noisefit_commans.data.model.history.ActivityMessage
import com.noisefit_commans.data.model.history.StepsHistoryResponse


data class FriendProfile(
    val name: String,
    var user_id: Long,
    val image_url: String? = null,
    val thumbnail_url: String? = null,
    val friends: Int? = 0,
    val post_count: Int? = 0,
    val isFriend: Boolean? = null,
    var canCompete: Boolean? = null,
    val commonFriends: CommonFriends? = null,
    val badges: Int? = null,
    val request_status: Int? = null,
    val description: String? = null,
    val isRequestReceived: Boolean? = null,
    val interests: List<FriendInterest>? = null,
    val location: FriendLocation? = null,
    val activity: StepsHistoryResponse? = null,
    val activityMessage: ActivityMessage? = null,
    var user_emoji: String? = null,
    var user_type: String? = null,
    var myEmoji: MyEmoji? = null,
    var imageArray: List<String>? = null,
) {
    fun hasLocationData(): Boolean {
        if (location == null) return false
        if (location.city.isNullOrEmpty()) return false
        return true
    }

    fun getThumbnail(): String? {
        if (thumbnail_url.isNullOrEmpty()) {
            return image_url
        }
        return thumbnail_url
    }
}

data class MyEmoji(val emojis_type: String, val count: Int)


data class FriendLocation(
    val state: String? = null,
    val city: String? = null
)

data class FriendInterest(
    val name: String,
    val isCommon: Boolean? = null
)

data class CommonFriends(val total: Int, val first_name: String, val image_url: List<String>)
