package com.noisefit_commans.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class TransHistoryData(
    @SerializedName("transaction_history")
    @Expose
    val transactionHistory: List<TransactionHistory>? = null,
)

data class TransactionHistory(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("points")
    val points: Int? = null,
    @SerializedName("status")
    val status: String? = null,
    val image_url: String? = null,
    @SerializedName("transaction_at")
    val transaction_at: Long? = null,

    )