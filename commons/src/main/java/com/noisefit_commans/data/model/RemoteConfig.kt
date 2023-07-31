package com.noisefit_commans.data.model

import com.google.gson.annotations.SerializedName

data class RemoteConfig(@SerializedName("isActivityBannerShow")
                        val isActivityBannerShow: Boolean = false,
                        @SerializedName("schedulerTimeInterval")
                        val schedulerTimeInterval: Int = 0,
                        @SerializedName("isContentBannerShow")
                        val isContentBannerShow: Boolean = false,
                        @SerializedName("contentText")
                        val contentText: String = "",
                        @SerializedName("activityText")
                        val activityText: String = "",
                        @SerializedName("contentBanner")
                        val contentBanner: String = "",
                        @SerializedName("activityBanner")
                        val activityBanner: String = "",
                        @SerializedName("contentHeader")
                        val contentHeader: String = "",
                        @SerializedName("isBannerShow")
                        val isBannerShow: Boolean = false,
                        @SerializedName("syncIntervalFrequency")
                        val syncIntervalFrequency: Int = 0)