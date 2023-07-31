package com.noisefit_commans.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class TaskListData(
    @SerializedName("points")
    @Expose
    val points: Int? = null,
    @SerializedName("task_list")
    @Expose
    val taskList: List<TaskList>? = null,
)

data class TaskList(
    @SerializedName("transaction_id")
    val transactionId: Int? = null,
    @SerializedName("prediction_id")
    val prediction_id: Int? = null,
    @SerializedName("title")
    val title: String? = null,
    val date: String? = null,
    @SerializedName("points")
    val points: Int? = null,
    @SerializedName("status")
    var status: String? = null,
    @SerializedName("task_enum")
    val taskEnum: String? = null,

    @SerializedName("image_url")
    val imageUrl: String? = null,
    val message: String? = null,
    val type: String? = null,

    )