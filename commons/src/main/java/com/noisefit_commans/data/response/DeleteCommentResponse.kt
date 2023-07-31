package com.noisefit_commans.data.response

import com.google.gson.annotations.SerializedName

data class DeleteCommentResponse(
    @SerializedName("comment_count") var commentsCount: Int = 0,

    )
