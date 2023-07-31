package com.noisefit.util

import com.noisefit.data.model.MentionUser


fun List<MentionUser>.findUser(startPos: Int): MentionUser? {
    return this.firstOrNull {
        ((it.start_pos ?: -1) + 1) == startPos
    }

}