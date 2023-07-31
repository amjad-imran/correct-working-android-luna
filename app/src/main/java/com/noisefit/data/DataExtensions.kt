package com.noisefit.data

import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.data.model.ReactionsWrapper
import com.noisefit_commans.data.model.UserFriendData
import com.noisefit_commans.data.model.UserFriendReactions
import java.util.ArrayList

fun UserFriendReactions.handleReactionData(): ArrayList<ReactionsWrapper> {
    val reactionsWrapperList = ArrayList<ReactionsWrapper>()
    val allFriendList = ArrayList<UserFriendData>()

    val emojiList1 = this.emoji1?.userFriendData
    val emojiList2 = this.emoji2?.userFriendData
    val emojiList3 = this.emoji3?.userFriendData
    val emojiList4 = this.emoji4?.userFriendData

    emojiList1?.forEach { userFriendData ->
        userFriendData.emojiType = Emoji.EmojiHand.emoji
    }
    emojiList2?.forEach { userFriendData ->
        userFriendData.emojiType = Emoji.EmojiHeart.emoji
    }
    emojiList3?.forEach { userFriendData ->
        userFriendData.emojiType = Emoji.EmojiFire.emoji
    }
    emojiList4?.forEach { userFriendData ->
        userFriendData.emojiType = Emoji.Emoji100.emoji
    }

    emojiList1?.let { allFriendList.addAll(it) }
    emojiList2?.let { allFriendList.addAll(it) }
    emojiList3?.let { allFriendList.addAll(it) }
    emojiList4?.let { allFriendList.addAll(it) }
    reactionsWrapperList.add(ReactionsWrapper("All", 0, allFriendList))

    if (!this.emoji1?.userFriendData.isNullOrEmpty()) {
        reactionsWrapperList.add(
            ReactionsWrapper(
                Emoji.EmojiHand.emoji, this.emoji1?.count, emojiList1
            )
        )

    }
    if (!this.emoji2?.userFriendData.isNullOrEmpty()) {
        reactionsWrapperList.add(
            ReactionsWrapper(
                Emoji.EmojiHeart.emoji, this.emoji2?.count, emojiList2
            )
        )

    }
    if (!this.emoji3?.userFriendData.isNullOrEmpty()) {
        reactionsWrapperList.add(
            ReactionsWrapper(
                Emoji.EmojiFire.emoji, this.emoji3?.count, emojiList3
            )
        )

    }
    if (!this.emoji4?.userFriendData.isNullOrEmpty()) {
        reactionsWrapperList.add(
            ReactionsWrapper(
                Emoji.Emoji100.emoji, this.emoji4?.count, emojiList4
            )
        )

    }
    return reactionsWrapperList
}
