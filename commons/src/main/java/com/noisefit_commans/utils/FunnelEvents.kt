package com.noisefit_commans.utils

object FunnelEvents {

    enum class WatchFaceEvents {
        WatchFace_Started,
        WatchFace_Success,
        WatchFace_Failed
    }

    enum class SyncEvents {
        Sync_Start,
        Sync_Success,
        Sync_Error,
        Syncing_Start,
        Syncing_Completed,

        Sync_Device_Disconnected,
        Sync_Completed,
        Sync_Completed_Without_Logged_In,
        Sync_Completed_Without_Internet,
        Sync_Completed_Without_Uploading,

        Sync_Start_Uploading_Data,
        Sync_Completed_Uploading_Data,
        Sync_Error_Uploading_Data,

        Sync_Error_Ota_Transfer

    }
}